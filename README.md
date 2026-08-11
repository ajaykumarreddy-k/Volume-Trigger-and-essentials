<p align="center">
  <img src="assets/Logo%20-%20favicon.png" width="120" alt="Vol-Trigger Logo" />
</p>

# Vol-Trigger 🔊

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Version-v1.1-orange?style=for-the-badge" alt="v1.1 Release" />
  <img src="https://img.shields.io/badge/RAM-0MB%20(Stateless)-brightgreen?style=for-the-badge" alt="0MB RAM" />
  <img src="https://img.shields.io/badge/Battery-0%25%20Drain-success?style=for-the-badge" alt="0% Battery Drain" />
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License" />
</p>

**Vol-Trigger** is an ultra-lightweight, zero-background-footprint Android application engineered for devices with broken or faulty physical volume buttons. It triggers system volume up/down controls via a stylized 4x4 Home Screen Widget and Quick Settings Notification Panel Tiles.

---

## 📸 Visual Showcase

| 4x4 Home Screen Widget | Quick Settings Notification Panel Tiles |
| :---: | :---: |
| <img src="assets/widgets%20preview.jpeg" width="380" alt="4x4 Home Screen Widget" /> | <img src="assets/notifical-panelicons.jpeg" width="380" alt="Quick Settings Notification Panel Tiles" /> |

---

## 🌟 Key Highlights

- **Zero Background Footprint**: Stateless architecture with zero background services, activities, or persistent threads. Memory is fully reclaimed immediately after volume adjustment.
- **Zero Battery Drain**: Built with `updatePeriodMillis="0"` to eliminate periodic system CPU wakeups.
- **Stylized 4x4 Resizable Widget**: Features the custom `vOLUME` logo card and solid white rounded square `+` and `-` button controls on a dark background container.
- **Notification Shade Tiles**: Quick Settings tiles with custom speaker sound wave icons (`volume-up` & `low-volume`) for instant **Vol +** and **Vol -** adjustments from any app or screen.
- **Synchronous Execution**: Direct invocation of `AudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI)`.

---

## 🚀 Quick Installation

### Install Pre-Built v1.1 Release APK via ADB

With USB Debugging enabled on your device, execute:

```bash
adb install -r VolTrigger-v1.1.apk
```

---

## 📖 How to Setup

### 1. Adding the 4x4 Home Screen Widget
1. Long-press any open area on your phone's **Home Screen**.
2. Tap **Widgets** and scroll to **Vol-Trigger**.
3. Drag the **4x4 Widget** onto your screen and adjust handles as desired.

### 2. Adding Notification Panel Tiles
1. Pull down your phone's **Notification Shade / Quick Settings**.
2. Tap the **Edit / Pencil icon**.
3. Locate **Vol +** and **Vol -** tiles and drag them into your active Quick Settings grid.

---

## 🛠️ Build from Source

#### Prerequisites
- Java JDK 17
- Android SDK (API 35, Build-Tools 35.0.0+)
- Gradle 8.x / 9.x

#### Build Command
```bash
# Clean release build with R8 minification
gradle clean assembleRelease

# Output APK path:
# app/build/outputs/apk/release/app-release.apk
```

---

## 📁 Repository Structure

```
.
├── assets/                       # Screenshot and logo assets
│   ├── Logo - favicon.png
│   ├── notifical-panelicons.jpeg
│   ├── widgets preview.jpeg
│   ├── low-volume.png
│   └── volume-up.png
├── app/                          # Android Application Source Code
├── VolTrigger-v1.1.apk           # Pre-built v1.1 Release Package
├── build.gradle.kts              # Build configuration
└── README.md                     # Project documentation
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
# Volume-Trigger-Andriod
