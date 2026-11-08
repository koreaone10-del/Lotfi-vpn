# Lotfi VPN — Android Starter

A clean, modular Android/Kotlin starter repository.

## Current scope
- Native Android Activity and XML interface.
- Clear package structure and resource naming.
- Gradle build configuration.
- GitHub Actions debug-build workflow.
- No server scanner, payload injection, credential collection, or traffic-tunneling implementation is included.

## Requirements
- Android Studio with JDK 17, or a compatible Android build environment.
- Android SDK 35.

## Build
```bash
gradle :app:assembleDebug
```

The debug APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## Project structure
```text
Lotfi-vpn/
├── .github/workflows/android-ci.yml
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/lotfi/vpn/
│       │   ├── MainActivity.kt
│       │   ├── data/ConnectionState.kt
│       │   └── ui/StatusFormatter.kt
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/{strings.xml,colors.xml,styles.xml}
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Notes
`local.properties` is intentionally excluded because its Android SDK path is machine-specific.
