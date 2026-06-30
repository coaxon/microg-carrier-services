# RCS Stage 2 Integration Plan (Post-AIDL Discovery)

This document outlines the exact code changes required in `microg/GmsCore` once the authentic `IRcsService.aidl` is acquired via dynamic analysis.

## 1. File Additions

### Add the AIDL File
Create the AIDL interface exactly as recovered from the system:
**File:** `play-services-rcs/src/main/aidl/com/google/android/ims/rcs/IRcsService.aidl`
*(Note: Package and class name must exactly match the original Google interface for Binder to resolve it)*

```aidl
package com.google.android.ims.rcs;

// Wait for real AIDL structure
interface IRcsService {
    // e.g., void authenticate(in String challenge, out IAuthCallback callback);
}
```

## 2. File Modifications

### Update `RcsService.java`
Currently, `RcsService.java` returns a `null` binder during the `onPostInitComplete` callback. This acts as a Stage 1 Mock to prevent immediate crashing.

**File:** `play-services-rcs/src/main/java/org/microg/gms/rcs/RcsService.java`

#### What to change:

1. **Import the generated AIDL Stub**:
   ```java
   import com.google.android.ims.rcs.IRcsService;
   ```

2. **Implement the Stub within the Service**:
   Add an inner class or instantiate an anonymous class implementing the Stub.
   ```java
   private IRcsService.Stub mRcsServiceStub = new IRcsService.Stub() {
       @Override
       public void authenticate(...) throws RemoteException {
           // TODO: Route this to our Ts43Orchestrator or EapAkaCrypto logic
       }
       // ... implement other required AIDL methods ...
   };
   ```

3. **Return the Stub in `handleServiceRequest`**:
   Replace the `null` binder in `callback.onPostInitComplete` with our real Stub.

   *Before:*
   ```java
   callback.onPostInitComplete(0, null, null);
   ```
   
   *After:*
   ```java
   callback.onPostInitComplete(0, mRcsServiceStub, null);
   ```

## 3. Dependency Injection
Once the `RcsService` can accept Binder calls, it will need a way to trigger the EAP-AKA logic. We will need to include the `microg-carrier-services` library (specifically `Ts43Orchestrator` and `EapAkaCrypto`) into the `play-services-rcs` build path (via `build.gradle`), or decouple it entirely by using an internal intent to trigger the standalone `carrier-services` stub.
