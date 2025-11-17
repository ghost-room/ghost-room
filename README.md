# Ghost Room Android Suite

Welcome to your Ghost Room Android workspace! This is a comprehensive messaging application suite built with Firebase, Agora RTC, Realm, and modern Android development practices.


## Overview

**Ghost Room** is a feature-rich messaging application built with:
- **Kotlin & Java 17**: Modern Android development with Kotlin-first approach
- **Firebase**: Authentication, Realtime Database, Cloud Storage, Cloud Messaging, and Functions
- **Agora RTC SDK**: Voice and video calling with group call support (up to 11 participants)
- **Realm Database**: Local data persistence with automatic sync
- **Jetpack Libraries**: ViewBinding, Compose, Navigation, Lifecycle, WorkManager
- **Retrofit + RxJava**: Network layer with reactive programming
- **Evernote Android Job**: Background job scheduling
- **End-to-End Encryption**: Optional Virgil Security E3Kit integration

**Minimum SDK**: 26 (Android 8.0)  
**Target SDK**: 35 (Android 15)  
**Compile SDK**: 36

## Getting Started

### Prerequisites

- **Android Studio**: Jellyfish (2024.2.1) or newer
- **JDK**: 17 or higher
- **Android SDK**: API 35 with Build Tools 35.0.0
- **Gradle**: 8.13.0 (included via wrapper)
- **Kotlin**: 2.2.0

### Installation

1. **Clone or extract the repository**
   ```bash
   cd ghostroom
   ```

2. **Open in Android Studio**
   - File → Open → Select the `ghostroom` directory
   - Android Studio will automatically sync Gradle and download dependencies
   - Wait for the sync to complete (may take several minutes on first run)

3. **Configure Firebase** (if needed)
   - The `app/google-services.json` file is already included
   - For a new Firebase project, download your `google-services.json` and replace the existing one

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run on device/emulator**
   ```bash
   ./gradlew :app:installDebug
   ```

## Building For Production

### Debug Build

```bash
./gradlew assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

1. **Update version information** in `app/build.gradle`:
   ```gradle
   versionCode 1
   versionName "1.4.2"
   ```

2. **Build release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

3. **Find the signed APK** at: `app/release/Ghost Room.apk`

   **Note**: The release build uses the keystore included in `app/keystore.jks`. For production, replace this with your own keystore and update signing configs in `app/build.gradle`.

## Project Structure

### Repository Layout

```
ghostroom/
├── app/                          # Main Ghost Room application
│   ├── src/main/
│   │   ├── java/com/bbt/ghostroom/
│   │   │   ├── activities/       # All Activity classes
│   │   │   ├── adapters/         # RecyclerView adapters
│   │   │   ├── fragments/        # Fragment classes
│   │   │   ├── model/            # Data models (Realm objects)
│   │   │   ├── services/         # Background services
│   │   │   ├── utils/            # Utility classes
│   │   │   └── views/            # Custom views
│   │   ├── res/                  # Resources (layouts, drawables, values)
│   │   └── AndroidManifest.xml
│   ├── build.gradle              # App module build configuration
│   └── google-services.json      # Firebase configuration
│
├── cameraView/                   # Camera implementation library
├── imageeditengine/              # Image editing library
├── stories-progress-view/        # Status stories UI component
│
├── gradle/                       # Gradle wrapper and version catalog
├── build.gradle                  # Root build configuration
├── settings.gradle               # Project module includes
└── README.md                     # This file
```

### Module Dependencies

The main `app` module depends on:
- `:cameraView` - Camera functionality
- `:stories-progress-view` - Status stories UI
- `:imageeditengine` - Image editing capabilities

## Architecture

### Application Class

The app uses a custom `Application` class (`MyApp`) that initializes core components:

```kotlin
class MyApp : Application(), ActivityLifecycleCallbacks {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Realm database
        Realm.init(this)
        val realmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(MyMigration.SCHEMA_VERSION.toLong())
            .migration(MyMigration())
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
        
        // Initialize Firebase services
        SharedPreferencesManager.init(this)
        
        // Initialize background job scheduler
        JobManager.create(this).addJobCreator(FireJobCreator())
        
        // Initialize Agora RTC engine for calling
        createRtcEngine()
        
        // Initialize E2E encryption if enabled
        if (isE2E && SharedPreferencesManager.isUserInfoSaved()) {
            GlobalScope.launch(IO) {
                EthreeInstance.initialize(this).await()
            }
        }
    }
}
```

### Firebase Integration

#### Authentication

The app uses Firebase Phone Authentication for user login:

```kotlin
// In AuthenticationViewModel.kt
private val auth = FirebaseAuth.getInstance()

fun verifyPhoneNumber(phoneNumber: String) {
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            login(credential)
        }
        
        override fun onVerificationFailed(e: FirebaseException) {
            _showMessage.value = e.message
        }
        
        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // Show code input screen
        }
    }
    
    PhoneAuthProvider.verifyPhoneNumber(
        PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setCallbacks(callbacks)
            .build()
    )
}
```

#### Realtime Database

Firebase Realtime Database is used for:
- User profiles and presence
- Chat messages and metadata
- Group information
- Status updates
- Call state management

**Database Structure**:
```
/users/{uid}/
  - name, phone, photo, status, etc.
  
/chats/{chatId}/
  - messages/{messageId}/
    - text, timestamp, type, fromId, etc.
    
/groups/{groupId}/
  - name, description, members, admins, etc.
  
/status/{uid}/
  - statuses/{statusId}/
    - url, timestamp, type, etc.
```

#### Cloud Storage

Firebase Storage is used for:
- Profile pictures
- Chat media (images, videos, audio)
- Status media
- Document files

**Storage Paths**:
```
/profile_images/{uid}.jpg
/chat_media/{chatId}/{messageId}.{ext}
/status_media/{uid}/{statusId}.{ext}
```

### Realm Database

Realm is used for local data persistence and offline support:

#### Initialization

```kotlin
// In MyApp.kt
val realmConfiguration = RealmConfiguration.Builder()
    .schemaVersion(MyMigration.SCHEMA_VERSION.toLong())
    .allowQueriesOnUiThread(true)
    .allowWritesOnUiThread(true)
    .migration(MyMigration())
    .build()
Realm.setDefaultConfiguration(realmConfiguration)
```

#### Using Realm

```kotlin
// Get Realm instance
val realm = Realm.getDefaultInstance()

// Query users
val users = realm.where(User::class.java)
    .equalTo("isGroup", false)
    .findAll()

// Write transaction
realm.executeTransaction { realm ->
    val user = realm.createObject(User::class.java, uid)
    user.name = "John Doe"
    user.phone = "+1234567890"
}

// Don't forget to close
realm.close()
```

#### Realm Models

Key Realm models include:
- `User` - User profiles and contacts
- `Message` - Chat messages
- `Group` - Group chat information
- `Status` - Status updates
- `QuotedMessage` - Message replies/quotes

#### Migrations

When modifying Realm models, increment `SCHEMA_VERSION` in `MyMigration.kt`:

```kotlin
object MyMigration : RealmMigration {
    const val SCHEMA_VERSION = 1  // Increment this
    
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        
        if (oldVersion < 1) {
            // Migration logic here
        }
    }
}
```

### Agora RTC Integration

Agora RTC SDK is used for voice and video calling:

#### Initialization

```kotlin
// In MyApp.kt
private fun createRtcEngine() {
    val appId = context.getString(R.string.agora_app_id)
    mRtcEngine = RtcEngine.create(context, appId, mEventHandler)
    mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
}
```

#### Starting a Call

```kotlin
// In CallingActivity.kt
val rtcEngine = MyApp.app().rtcEngine()

// Join channel
rtcEngine?.joinChannel(
    token,           // Agora token (optional for testing)
    channelName,     // Channel name
    uid,             // User ID
    ChannelMediaOptions()
)

// Enable local video
rtcEngine?.enableLocalVideo(true)
rtcEngine?.startPreview()

// Enable local audio
rtcEngine?.enableLocalAudio(true)
```

#### Handling Call Events

```kotlin
// Implement AGEventHandler
class MyCallHandler : AGEventHandler {
    override fun onUserJoined(uid: Int) {
        // Remote user joined
        setupRemoteVideo(uid)
    }
    
    override fun onUserOffline(uid: Int, reason: Int) {
        // Remote user left
        removeRemoteVideo(uid)
    }
    
    override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int) {
        // Remote video state changed
    }
}

// Register handler
MyApp.app().addEventHandler(myCallHandler)
```

### Encryption

The app supports three encryption modes (configured in `app/build.gradle`):

1. **NONE** (default): No encryption
2. **AES**: Symmetric encryption
3. **E2E**: End-to-end encryption using Virgil Security E3Kit

#### Enabling E2E Encryption

1. Update `app/build.gradle`:
   ```gradle
   resValue 'string', "encryption_type", "E2E"
   ```

2. Initialize E3Kit (handled automatically in `MyApp`):
   ```kotlin
   if (isE2E && SharedPreferencesManager.isUserInfoSaved()) {
       GlobalScope.launch(IO) {
           EthreeInstance.initialize(this).await()
       }
   }
   ```

3. Register user during authentication:
   ```kotlin
   // In AuthenticationViewModel.kt
   if (isE2E()) {
       val ethree = EthreeInstance.initialize(this, uid).await()
       EthreeRegistration.registerEthree(ethree, this)
   }
   ```

## UI/UX Patterns

### Activities

The app uses a traditional Activity-based architecture with ViewBinding:

#### MainActivity

The main activity hosts three tabs (Chats, Status, Calls) using ViewPager:

```kotlin
class MainActivity : BaseActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Setup ViewPager with fragments
        adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}
```

#### ChatActivity

The chat screen displays messages in a RecyclerView:

```kotlin
class ChatActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessagesAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Load messages from Realm
        loadMessages()
    }
}
```

### Fragments

Key fragments include:
- `ChatsFragment` - Chat list
- `StatusFragment` - Status updates
- `CallsFragment` - Call history

### Custom Views

The app includes several custom views:
- `RecordView` - Voice message recording UI
- `CircularStatusView` - Status stories progress
- `StickerView` - Sticker picker
- `EmojiView` - Emoji keyboard

### Jetpack Compose

Jetpack Compose is enabled for future work, although the primary UI currently relies on traditional Views and Fragments. Compose components can be introduced gradually as needed.

## Configuration

### Build Configuration

Key configuration values in `app/build.gradle`:

```gradle
defaultConfig {
    applicationId "com.bbt.ghostroom"
    minSdkVersion 26
    targetSdkVersion 35
    versionCode 1
    versionName "1.4.2"
    
    // Agora App ID
    resValue 'string', "agora_app_id", "YOUR_AGORA_APP_ID"
    
    // Maps API Key
    resValue 'string', "maps_api_key", "YOUR_MAPS_API_KEY"
    
    // Encryption type: NONE, AES, or E2E
    resValue 'string', "encryption_type", "NONE"
    
    // Feature flags
    resValue 'bool', "are_ads_enabled", "false"
    resValue 'integer', "max_group_users_count", "50"
    resValue 'integer', "max_status_video_time", "30"
}
```

### Firebase Configuration

1. **Authentication**: Enable Phone Authentication in Firebase Console
2. **Realtime Database**: Set up security rules
3. **Storage**: Configure storage rules and paths
4. **Cloud Messaging**: Set up FCM for push notifications
5. **Functions**: Deploy Cloud Functions if using server-side logic

### Agora Configuration

1. Create an Agora account at https://www.agora.io/
2. Create a new project
3. Copy the App ID to `app/build.gradle`
4. (Optional) Set up token server for production

### Feature Flags

Control app features via `resValue` in `app/build.gradle`:

- **Ads**: `are_ads_enabled`, `is_calls_ad_enabled`, etc.
- **Limits**: `max_group_users_count`, `max_broadcast_users_count`, `max_status_video_time`
- **Battery Optimization**: `ignore_battery_optimizations_dialog`
- **Encryption**: `encryption_type`

## Services & Background Tasks

### Firebase Cloud Messaging

FCM handles push notifications:

```kotlin
// In MyFCMService.kt
class MyFCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle notification
        sendNotification(remoteMessage)
    }
}
```

### Background Jobs

Evernote Android Job handles scheduled tasks:

```kotlin
// In FireJobCreator.kt
class FireJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            SaveTokenJob.TAG -> SaveTokenJob()
            SetLastSeenJob.TAG -> SetLastSeenJob()
            DailyBackupJob.TAG -> DailyBackupJob()
            else -> null
        }
    }
}
```

### Foreground Services

- `CallingService` - Manages active calls
- `AudioService` - Plays audio messages
- `BackupService` - Handles chat backups
- `CompleteSetupService` - Initial setup tasks

## Testing

### Unit Tests

Limited unit test coverage. To add tests:

```kotlin
// Example test
class FireManagerTest {
    @Test
    fun testIsLoggedIn() {
        // Test authentication state
    }
}
```

### UI Tests

Manual testing is primary. For automated UI tests:

```kotlin
@RunWith(AndroidJUnit4::class)
class ChatActivityTest {
    @Test
    fun testSendMessage() {
        // UI test logic
    }
}
```

### Linting

Run lint checks:

```bash
./gradlew lintVitalRelease
```

## Deployment

### Pre-Release Checklist

1. ✅ Update `versionCode` and `versionName` in `app/build.gradle`
2. ✅ Replace test API keys with production keys
3. ✅ Replace keystore with production keystore
4. ✅ Update `google-services.json` if using different Firebase project
5. ✅ Test on multiple devices and Android versions
6. ✅ Verify all features work correctly
7. ✅ Run lint and fix critical issues
8. ✅ Update changelog/documentation

### Release Build Steps

1. **Clean build**:
   ```bash
   ./gradlew clean
   ```

2. **Build release**:
   ```bash
   ./gradlew assembleRelease
   ```

3. **Verify APK**:
   - Check APK at `app/release/Ghost Room.apk`
   - Install on test device
   - Verify signing

4. **Upload to Play Store**:
   - Use Google Play Console
   - Upload APK or AAB
   - Fill in store listing
   - Submit for review

## Security Considerations

### ⚠️ Important Security Notes

1. **API Keys**: The repository contains hardcoded API keys for development. **Replace all keys before production release**:
   - Agora App ID
   - Google Maps API Key
   - Foursquare Client ID/Secret
   - Firebase configuration (if using different project)

2. **Keystore**: The release keystore is included with default passwords. **Replace with secure credentials**.

3. **Encryption**: E2E encryption is optional. For maximum security, enable E2E mode and ensure proper key management.

4. **Permissions**: Review all permissions in `AndroidManifest.xml` and request only what's necessary.

## Learn More

### Documentation

- **Firebase**: https://firebase.google.com/docs
- **Agora RTC**: https://docs.agora.io/en/
- **Realm**: https://realm.io/docs/java/latest/
- **Jetpack Compose**: https://developer.android.com/jetpack/compose

### Project-Specific Docs

- `COLOR_SCHEME_UPDATE.md` - Color scheme changes
- `TEXT_VISIBILITY_FIX.md` - Text visibility fixes

### Support

For issues, questions, or contributions:
- Review existing documentation
- Check GitHub issues (if repository is public)
- Contact the development team

---

**Note**: This project is actively maintained. Before making significant changes to core functionality (authentication, calling, database schema), please review the codebase thoroughly and test on multiple devices.
