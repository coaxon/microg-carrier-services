# Chimera/Binder Route Research Report

## 1. Publicly Available Intelligence
Based on community research (XDA, GitHub, Reddit):
- **AIDL Availability**: Google does not publish the `IRcsService` or `ICarrierMessagingService` AIDL files. However, decompiled `smali` from `Google Messages` and `Carrier Services` APKs reveal the method signatures. The `Binder` service identifier for RCS is typically registered with `ServiceManager` under internal Google names.
- **Frida Hooking**: Security researchers commonly use Frida to intercept Binder IPC.
  - Scripts typically attach to `libbinder.so` (using `Interceptor.attach`) to dump transaction codes and parcels.
  - Higher-level hooks use `Java.use()` on the generated Proxy classes (e.g., `com.google.android.ims.rcs.IRcsService$Stub$Proxy`) to intercept method calls like `getIccAuthentication` or SIP message transmission.

## 2. MicroG Clean-Room Methodology (e.g., SafetyNet / Play Integrity)
Historically, MicroG addresses undocumented Google proprietary APIs (like Play Integrity) using a strict clean-room approach:
1. **API Interception (Stubbing)**: MicroG implements the `Service` and `IBinder.Stub` with dummy methods just to prevent the client app (like Google Messages) from crashing when it binds to the service. (This is our current `RcsService.java` Stage 1).
2. **Traffic Analysis**: Without decompiling proprietary code, developers use `Xposed` or `Frida` on a genuine Google-equipped device to observe the arguments sent *into* the Binder and the responses returned.
3. **Re-implementation**: Once the structure (AIDL) and the expected JSON/Protobuf payloads are understood from dynamic analysis, a clean-room implementation is written in MicroG that replicates the expected behavior (e.g., fulfilling the Play Integrity token request or returning the EAP-AKA base64 response).
4. **Boundary Avoidance**: MicroG strictly avoids replicating Google's proprietary cryptographic algorithms or server-side keys. If an operation requires a hardware-backed key or Google server signature, MicroG delegates it to local open standards or requires user-provided tokens.

## 3. Clear Boundaries: What We Know vs. What Requires Hardware
- **What is known/public**: We know the overall architecture (Messages binds to Carrier Services), the 3GPP standards (TS.43 EAP-AKA), and the methods for hooking Android IPC (Frida). We also have the MicroG Stage 1 Service Stub in place.
- **What absolutely requires physical hardware / SIM**:
  1. The exact AIDL transaction codes (method IDs) and data parcel structure expected by Google Messages in the current version.
  2. The exact JSON payload structure sent by the specific EU carriers (e.g., Giffgaff) during the TS.43 HTTP flow.
  3. The real `getIccAuthentication` response bytes from the specific SIM cards to ensure the APDU parser handles carrier-specific variations correctly.

**Conclusion**: We have prepared the foundation and the methodology. The next step in the Chimera/Binder route *must* involve dynamic analysis (Frida) on a real device with a working SIM to capture the live AIDL transactions, completing the missing piece of the clean-room implementation.
