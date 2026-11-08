# LOTFI VPN — ULTIMATE

Android Kotlin + Jetpack Compose foundation: Arabic RTL dashboard, local server profile manager, authorized single-host DNS/TCP diagnostics, GitHub release check, custom vector icon and CI debug APK workflow.

## Important status
This first version is **not yet a functioning VPN tunnel client**. It does not create a fake TUN connection. WireGuard/Xray engine integration is the next milestone and requires a real authorized server configuration and a verified engine adapter.

No carrier-billing bypass, hidden-host exploitation, unauthorized port sweeps or payload injection is included. Diagnostics are intended for systems you own or are explicitly authorized to test.

## Build
Requires JDK 17, Android SDK Platform 35 and Gradle 8.9.
Run: `gradle assembleDebug`
APK: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds the debug APK on push or manual workflow dispatch.
Repository: https://github.com/koreaone10-del/Lotfi-vpn
