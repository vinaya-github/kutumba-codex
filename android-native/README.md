# Kutumba Native Android

This is the clean-slate native Android app for Kutumba.

It is intentionally separate from the recovered legacy Android WebView wrapper.
This app uses native Android views and local device storage only.

## Included

- Household dashboard
- Vinaya and Divya as owner/co-owner
- Trusted steward setup
- Continuity record creation
- Per-record visibility
- Emergency kit preview
- Vault document placeholders
- Local persistence through SharedPreferences
- Prototype lock/unlock

## Open

Open this folder in Android Studio:

```text
clean-slate/android-native
```

Then run the `app` configuration on an Android emulator or device.

Expected local tooling:

- Android Studio with bundled JDK
- Android SDK 33 or newer
- Gradle sync enabled

This Codex workspace did not have `java`, `gradle`, or `adb` available on PATH, so APK generation was not run here.

## Notes

This first native build does not use:

- WebView
- Cloud sync
- Auth
- Bank integrations
- Real document uploads
- Password storage
