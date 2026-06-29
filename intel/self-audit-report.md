# CoAxon Labs Self-Audit Report: microg-carrier-services

## Overview
This report documents the security and architectural self-audit of the `microg-carrier-services` TS.43 EAP-AKA protocol layer. The audit utilizes CodeQL static analysis combined with manual review to verify data flow, cryptographic correctness, and component boundaries.

## Audit Findings

### 1. Sensitive Data Flow to Logs (Taint Tracking)
**Objective**: Ensure that `getIccAuthentication` return values, and derived vectors (`CK`, `IK`, `RES`) do not leak into application logs.  
**Methodology**: Taint tracking analysis on variables matching `ck|ik|res|response|rootKey` and `getIccAuthentication` returns against `android.util.Log` sinks.  
**Result**: **PASS**  
**Details**: 
Analysis confirmed that sensitive cryptographic material is properly contained. The only logging related to these vectors occurs in `SimResponseParser.java`, where only the **lengths** of the byte arrays are logged for debugging purposes, not their actual contents:
```java
Log.d(TAG, "SIM auth success: RES=" + result.res.length + "B, CK=" + result.ck.length + "B, IK=" + result.ik.length + "B");
```
No paths exist where `getIccAuthentication` payload or unencrypted vectors are passed to a logging sink.

### 2. Cryptographic Implementation Correctness
**Objective**: Verify HMAC-SHA1 initialization parameter order and FIPS 186-2 PRF constant hardcoding.  
**Methodology**: Static analysis of `javax.crypto.Mac` initialization calls and integer constant verification in `EapAkaCrypto.java`.  
**Result**: **PASS**  
**Details**:
- **HMAC-SHA1**: Analysis confirms that `calculateMac` correctly initializes the MAC instance. The authentication key (`k_aut`) is correctly passed into `SecretKeySpec` and used in `mac.init()`, while the `packet` data is correctly passed to `mac.doFinal()`. There is no key/data parameter mix-up.
- **FIPS 186-2 PRF Constants**: The FIPS 186-2 Change Notice 1 PRF (Appendix 3.1) implementation in `fips186Prf()` correctly hardcodes the SHA-1 initialization hashes (`0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0`) and the corresponding round constants (`0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xCA62C1D6`). 

### 3. Cross-Module Dependency Boundaries
**Objective**: Confirm that the `com.google.android.ims.ts43` package remains cleanly decoupled from heavy Android system APIs to ensure robust and isolated unit testing.  
**Methodology**: Dependency graph analysis mapping references from the `ts43` namespace to `android.*` components.  
**Result**: **PASS**  
**Details**:
The module exhibits a clean architectural boundary. The only dependencies on the `android.*` namespace are standard utilities:
- `android.util.Base64`
- `android.util.Log`

No context-heavy components, system services, or unexpected Android OS APIs are imported. This ensures that `Ts43Orchestrator` and its components can be reliably unit-tested in a pure JVM environment, successfully achieving the hardware-decoupled design goal.

## Conclusion
The `microg-carrier-services` TS.43 EAP-AKA protocol layer meets CoAxon Labs' strict internal standards for cryptographic handling, data privacy, and architectural isolation. The codebase is clean, secure, and ready for further hardware validation.
