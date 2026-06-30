## Community Testing Request: Physical SIM Validation (RCS EAP-AKA)

The `com.google.android.ims` stub for EAP-AKA authentication has reached the hardware validation stage. Software-level tests are passing (HMAC-SHA1 cryptography and APDU SIM response parsing), but we need physical verification against real carrier networks before merging the final architecture.

### Who can help?
We are looking for volunteers who meet the following criteria:
1. Have a SIM card from a carrier with native RCS support (e.g., Giffgaff, EE, O2, Vodafone in the EU, or major US carriers).
2. Have a rooted Android device (Magisk) or are running a custom ROM (e.g., LineageOS for microG).

### How to test:
1. **Download the Stub**: Grab the latest `app-release.apk` from the [Releases](https://github.com/coaxon/microg-carrier-services/releases) page.
2. **System Installation**: Install the APK as a Privileged System App. 
   - **Via Magisk**: Create a module placing the APK in `/system/priv-app/CarrierServices/` and add the permission whitelist XML (`privapp-permissions-com.google.android.ims.xml` granting `android.permission.READ_PRIVILEGED_PHONE_STATE`) to `/system/etc/permissions/`.
3. **Run the Test**: Reboot, open the "EAP-AKA Test" app from your launcher, and trigger the handshake.

### What to report back:
Please reply to this issue with:
- Carrier name & Country
- ROM version & Device model
- Logcat output of the EAP-AKA Test app (specifically looking for the Base64 response packet or any `SecurityException` related to TelephonyManager).

Your help is crucial to validating the clean-room implementation of TS.43 EAP-AKA without relying on Google binaries. Thank you!
