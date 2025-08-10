# Digital Monitoring - App Usage Controller

A comprehensive Android application for monitoring and controlling app usage with time limits and detailed statistics.

## Features

- 📊 **Real-time App Usage Monitoring**: Track how much time you spend on each app
- ⏰ **Time Limit Management**: Set custom time limits for any app
- 🚨 **Smart Notifications**: Get notified when time limits are exceeded
- 📈 **Usage Statistics**: View detailed usage reports and trends
- 🎯 **Overlay Alerts**: Full-screen overlays when limits are reached
- 🔄 **Background Service**: Continuous monitoring even when app is closed

## Screenshots

[Add screenshots here after testing]

## Installation

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 23 or higher
- Kotlin 1.8.0 or higher

### Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/digital-monitoring.git
```

2. Open the project in Android Studio

3. Sync Gradle files and build the project

4. Run on device or emulator

## Permissions Required

The app requires the following permissions:

- **Usage Stats Permission**: To monitor app usage (requires manual enable in Settings)
- **Overlay Permission**: To display time limit notifications
- **Foreground Service**: To run background monitoring

## Usage

1. **First Launch**: Grant required permissions when prompted
2. **Set App Limits**: Navigate to "App Limits" and add time limits for apps
3. **Monitor Usage**: View statistics in the "Statistics" tab
4. **Get Notified**: Receive overlays when time limits are exceeded

## Technical Details

### Architecture

- **UI**: Jetpack Compose with Material Design
- **Navigation**: Navigation Compose
- **Data Storage**: SharedPreferences with Gson serialization
- **Background Service**: Foreground service with notification
- **Permissions**: Runtime permission handling

### Key Components

- `MainActivity`: Main UI and navigation
- `UsageMonitorService`: Background monitoring service
- `AppLimitStore`: Data persistence layer
- `OverlayManager`: Overlay display management
- `AppLimit`: Data model for app limits

## Building for Release

1. Create a keystore file:
```bash
keytool -genkey -v -keystore digital-monitoring.keystore -alias digital-monitoring -keyalg RSA -keysize 2048 -validity 10000
```

2. Update `app/build.gradle` with your keystore details:
```gradle
signingConfigs {
    release {
        storeFile file("digital-monitoring.keystore")
        storePassword "your-store-password"
        keyAlias "digital-monitoring"
        keyPassword "your-key-password"
    }
}
```

3. Build release APK:
```bash
./gradlew assembleRelease
```

## Play Store Publishing Checklist

- [ ] Create Google Play Console account
- [ ] Prepare app store listing (description, screenshots, etc.)
- [ ] Test on multiple devices
- [ ] Implement privacy policy
- [ ] Create app icon and feature graphic
- [ ] Prepare release notes
- [ ] Submit for review

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, email support@digitalmonitoring.com or create an issue on GitHub.

## Changelog

### Version 1.0.0
- Initial release
- App usage monitoring
- Time limit management
- Statistics dashboard
- Overlay notifications
