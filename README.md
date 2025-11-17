# Ghost Room

**Serverless • Encrypted • Offline Mesh Messaging Suite**

Ghost Room is a next-generation communication platform engineered for secure, fast, and censorship-resistant messaging. Designed with a hybrid privacy architecture — combining cloud-grade reliability with true offline device-to-device communication — Ghost Room enables users to stay connected anytime, anywhere, with or without Internet access.

## Private by Design • Engineered for Zero Exposure

Unlike traditional messengers that route all communication through centralized servers, Ghost Room minimizes metadata exposure by using:

- **Direct P2P Offline Mesh Mode (Bluetooth)**
- **Serverless communication paths when offline**
- **End-to-End encrypted chats**
- **Locally stored conversation states**
- **Secure call signalling using Agora RTC**
- **Firebase services layered with client-side privacy controls**

Ghost Room protects:
- Who you talk to
- When you talk
- Where you are
- How often you communicate
- Your online/offline patterns

Because communication doesn't depend solely on Internet servers.

## The Problem We Solve

Most messaging apps — even encrypted ones — still expose your:
- Identity
- IP address
- Communication patterns
- Contact graph
- Online presence
- Metadata trail

Ghost Room breaks this dependency by enabling true serverless offline messaging, so communication can happen even:
- ✔ Without SIM card
- ✔ Without Internet
- ✔ Without any central servers
- ✔ Without revealing metadata

## What Makes Ghost Room Different

### 1. Offline Mesh Mode — Device-to-Device Bluetooth Messaging

A signature Ghost Room innovation. When there is no Internet, users can still communicate using encrypted peer-to-peer Bluetooth messaging:

- One-to-one chats
- Broadcast messaging
- Local mesh networking
- High-resilience device hopping
- No servers, no SIM, no IP address

The message never touches the Internet. Never touches a server. Never reveals metadata. This is true serverless communication.

### 2. End-to-End Encryption

Ghost Room uses modern encryption for:
- Chats
- Status updates
- Calls
- Offline mesh messages

Cryptographic keys are stored securely on-device, ensuring zero knowledge exposure.

### 3. Hybrid Privacy Architecture

Ghost Room blends the best of both worlds:

**Online Mode (Cloud-Assisted Security)**
Powered by:
- Firebase Authentication
- Realtime Database
- Firestore/Storage
- Cloud Functions

Optimized for speed, scalability, and reliability — while keeping sensitive metadata minimized.

**Offline Mode (True Serverless Communication)**
Powered by:
- Peer-to-peer encrypted Bluetooth
- Local Realm database
- Local message queues
- No cloud dependency

### 4. Secure Calling via Agora RTC

- 1-on-1 and group calls (up to 11 participants)
- Encrypted audio/video streams
- Optimized for low latency
- Works in low bandwidth environments

### 5. Modern, Scalable, Production-Ready Tech Stack

- Kotlin + Java 17
- Firebase / FCM / Firestore
- Agora RTC SDK
- Realm Database
- Jetpack Compose / Navigation / Lifecycle
- Retrofit + RxJava
- WorkManager for background tasks

## Ghost Mode Features

### Core Features

- ✓ End-to-End Encrypted Messaging
- ✓ Offline Mesh Mode (Bluetooth P2P)
- ✓ Broadcast Messaging Offline
- ✓ Secure Group & 1-on-1 Calls (Agora)
- ✓ Message Queueing During Offline Periods
- ✓ Realm-Based Local Encryption
- ✓ Firebase Cloud Integration

### Smart Features

- ✓ Auto Mode Switching (Offline ↔ Online)
- ✓ Local Message Backup
- ✓ Stealth Presence Mode
- ✓ Attachment Support (online mode)
- ✓ Status updates

### Power Tools

- ✓ Multi-Device Architecture (in roadmap)
- ✓ Multi-hop mesh networking (planned)
- ✓ LAN-based offline mode (future)
- ✓ Desktop client (future)

## Use Cases

Ghost Room is engineered for environments where communication must remain resilient and private:

- ** Journalists & Sources** — Stay connected even during network blackouts
- ** Public Events / Crowds** — Chat when mobile networks are overloaded
- ** Remote Areas** — Work offline for long durations
- ** Universities & Hostels** — Local mesh communication without Internet
- ** Censored Environments** — Messaging continues even under network blocks

## Technology Highlights

### Ghost Mesh Protocol (Offline Layer)

A Bluetooth LE–powered encrypted communication layer optimized for:
- Low-latency P2P chats
- Broadcast distribution
- Mesh hopping (planned)
- Energy-efficient scanning
- Zero-server messaging

### Hybrid Sync System

Ghost Room intelligently switches between modes:
1. Try Offline Mesh
2. If unavailable → Use Firebase for delivery
3. Re-sync using Realm local database

### Secure Calling Architecture

- Agora RTC for low-latency AV
- Encrypted signalling
- Custom call session management

## Architecture Overview

### Application Architecture
Ghost Room follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (Activities, Fragments, Views)    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         ViewModel Layer             │
│  (Business Logic, State Management) │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Domain Layer                 │
│  (Use Cases, Repositories)           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Data Layer                   │
│  (Firebase, Realm, Local Storage)    │
└─────────────────────────────────────┘
```

### Key Components

**Data Persistence**
- **Realm Database**: Local data storage with automatic sync
- **Firebase Realtime Database**: Cloud synchronization
- **SharedPreferences**: App settings and preferences

**Network Layer**
- **Firebase Services**: Authentication, Database, Storage, FCM
- **Agora RTC SDK**: Voice and video calling
- **Retrofit + RxJava**: REST API communication

**Offline Mesh Layer**
- **Bluetooth Low Energy (BLE)**: P2P communication
- **Message Queue**: Offline message buffering
- **Mesh Routing**: Device discovery and message forwarding

**Security Layer**
- **E2E Encryption**: End-to-end message encryption
- **Key Management**: Secure key storage and exchange
- **Certificate Pinning**: Secure API communication

## Developer Workspace

Ghost Room Android Workspace includes:
- Complete modularized Android codebase
- Ready-to-compile Firebase configuration
- Integrated Agora calling stack
- Offline mesh stack with real-time event listeners
- Clean architecture + MVVM
- Gradle 8.13 + Kotlin 2.2
- Target SDK 35 (Android 15)

## Requirements

### System Requirements
- **Android**: 8.0 (API 26) or higher
- **RAM**: Minimum 2GB recommended
- **Storage**: 50MB+ for app installation
- **Bluetooth**: Bluetooth Low Energy (BLE) support for offline mesh mode
- **Permissions**: Location (for Bluetooth scanning), Storage, Camera, Microphone

### Development Requirements
- **Android Studio**: Jellyfish (2024.2.1) or newer
- **JDK**: 17 or higher
- **Android SDK**: API 35 with Build Tools 35.0.0
- **Gradle**: 8.13.0 (included via wrapper)
- **Kotlin**: 2.2.0

## Getting Started

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/ghost-room/ghost-room.git
   cd ghostroom
   ```

2. **Open in Android Studio**
   - File → Open → Select the `ghostroom` directory
   - Android Studio will automatically sync Gradle and download dependencies
   - Wait for the sync to complete (may take several minutes on first run)

3. **Configure Firebase**
   - Replace `app/google-services.json` with your Firebase project configuration
   - Enable Phone Authentication in Firebase Console
   - Configure Realtime Database rules
   - Set up Cloud Storage buckets

4. **Configure Agora RTC**
   - Create an account at [Agora.io](https://www.agora.io/)
   - Create a new project and get your App ID
   - Update `agora_app_id` in `app/build.gradle`

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew :app:installDebug
   ```

## Building the Project

### Debug Build
```bash
./gradlew assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build
```bash
./gradlew assembleRelease
```
The signed APK will be at: `app/release/Ghost Room.apk`

**Note**: For production releases, replace the default keystore with your own and update signing configurations in `app/build.gradle`.

### Clean Build
```bash
./gradlew clean
./gradlew build
```

## Configuration

### Firebase Setup
1. **Authentication**: Enable Phone Authentication in Firebase Console
2. **Realtime Database**: Configure security rules for your use case
3. **Storage**: Set up storage buckets and access rules
4. **Cloud Messaging**: Configure FCM for push notifications
5. **Cloud Functions**: Deploy server-side functions if needed

### Agora RTC Setup
1. Create an Agora account and project
2. Copy your App ID to `app/build.gradle`:
   ```gradle
   resValue 'string', "agora_app_id", "YOUR_AGORA_APP_ID"
   ```
3. (Optional) Set up token server for production environments

### Build Configuration
Key configuration values in `app/build.gradle`:
- `applicationId`: Your app package name
- `versionCode`: Increment for each release
- `versionName`: Semantic version string
- `agora_app_id`: Agora RTC App ID
- `maps_api_key`: Google Maps API key (for location features)
- `encryption_type`: NONE, AES, or E2E

## Roadmap

### Completed
- ✓ Offline Mesh Mode
- ✓ Secure Calls
- ✓ Chat + Status
- ✓ Realm Integration
- ✓ Auto Mode Switching

### In Progress
- ▢ Enhanced encryption
- ▢ Multi-device identity
- ▢ Media compression engine
- ▢ Desktop client

### Planned
- ▢ Full mesh multi-hop
- ▢ LAN/WiFi-Direct mode
- ▢ Open-source SDK
- ▢ iOS version

## Security & Privacy Details

### Encryption Standards
- **End-to-End Encryption**: All messages are encrypted before transmission
- **Key Exchange**: Secure key exchange protocol for establishing encrypted channels
- **On-Device Key Storage**: Cryptographic keys never leave the device
- **Forward Secrecy**: Keys are rotated to prevent historical message decryption

### Privacy Protections
- **Minimal Metadata**: Only essential metadata is collected
- **No IP Logging**: IP addresses are not stored or logged
- **Local-First**: Messages are stored locally first, synced only when necessary
- **Offline Mode**: Complete functionality without Internet connectivity
- **No Contact Sync**: Contact information stays on your device

### Data Handling
- **Local Storage**: All sensitive data stored in encrypted Realm database
- **No Cloud Backup**: Messages are not automatically backed up to cloud
- **User Control**: Users can export/delete their data at any time
- **Transparency**: Open-source code allows full security audit

### Security Best Practices
- Regular security audits
- Dependency updates for known vulnerabilities
- Secure coding practices
- No hardcoded secrets in source code
- Certificate pinning for API calls

## Frequently Asked Questions (FAQ)

**1. Is Ghost Room completely free?**  
Yes. It's open-source and free for everyone.

**2. Does Ghost Room collect any personal data?**  
No. We avoid all unnecessary data collection. Only the absolute minimum required to function is used — and never stored long-term.

**3. Can I verify the security of the code myself?**  
Absolutely. The entire source code is public, reviewable, and open to audits.

**4. Is Ghost Room suitable for team or business communication?**  
Yes — it works for individuals, small teams, and anyone who values privacy over convenience.

**5. How does offline mesh mode work?**  
When devices are within Bluetooth range and have no Internet, they automatically switch to encrypted Bluetooth P2P communication. Messages are delivered directly between devices without any server involvement.

**6. Is my data safe if I lose my device?**  
Yes. All data is encrypted with keys stored on-device. Without your device and authentication, data cannot be accessed. You can also enable additional security features like biometric locks.

**7. Can I use Ghost Room internationally?**  
Yes. Ghost Room works anywhere, with or without Internet. The offline mesh mode is particularly useful in areas with poor connectivity or network restrictions.

## Contributing

We welcome contributions! Ghost Room is an open-source project, and we appreciate any help you can provide.

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow the existing code style
   - Write clear commit messages
   - Add tests if applicable
4. **Test thoroughly**
   - Test on multiple Android versions
   - Verify offline mode functionality
   - Check encryption/decryption flows
5. **Submit a pull request**
   - Provide a clear description of changes
   - Reference any related issues
   - Ensure all tests pass

### Code Style Guidelines
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and small
- Write unit tests for new features

### Reporting Issues
- Use GitHub Issues to report bugs
- Provide detailed reproduction steps
- Include device information and Android version
- Attach relevant logs if possible

## Support

### Getting Help
- **GitHub Issues**: Report bugs or request features
- **Documentation**: Check project documentation for technical details
- **Community**: Join discussions and share experiences

### Contact
- **Website**: [Visit our website](http://example.com/)
- **Email**: Contact us via email (if available)
- **Twitter**: Follow updates (if available)

## License

This project is open-source and available under the [MIT License](LICENSE) (or your preferred license).

### License Summary
- ✅ Commercial use
- ✅ Modification
- ✅ Distribution
- ✅ Private use
- ❌ Liability
- ❌ Warranty

## Acknowledgments

- **Firebase**: For cloud infrastructure and services
- **Agora**: For real-time communication SDK
- **Realm**: For local database solution
- **Open Source Community**: For various libraries and tools
- **Contributors**: Everyone who has contributed to this project

## Additional Resources

### Documentation
- [Firebase Documentation](https://firebase.google.com/docs)
- [Agora RTC Documentation](https://docs.agora.io/en/)
- [Realm Database Docs](https://realm.io/docs/java/latest/)
- [Android Developer Guide](https://developer.android.com/)

### Related Projects
- Check out similar privacy-focused messaging solutions
- Explore offline-first communication protocols
- Learn about mesh networking technologies

---

**Note**: This project is actively maintained. For technical documentation, build instructions, and contribution guidelines, please refer to the project documentation.

**Version**: 2.2.4 | **Last Updated**: 2025

