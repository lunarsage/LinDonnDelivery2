# LinDonn Delivery

![App Logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

youtube link: https://youtube.com/shorts/L8MPLA2sWZs?si=nGX8QPZE6wNsD7Ko

A modern food delivery Android application built with Jetpack Compose, featuring offline-first architecture, real-time push notifications, multi-language support, and seamless integration with Supabase backend.

## 📱 App Purpose

LinDonn Delivery is a comprehensive food delivery platform designed to connect users with local restaurants in South Africa. The app provides a seamless experience for browsing restaurants, ordering food, tracking deliveries, and managing user preferences—all while working offline and syncing when connectivity is restored.

### Key Objectives
- **Accessibility**: Multi-language support (English, Afrikaans, isiZulu) for South African users
- **Reliability**: Offline-first architecture ensures app functionality without internet connection
- **Real-time Updates**: Push notifications for order status updates
- **User Experience**: Modern Material 3 design with intuitive navigation

## 🎨 Design Considerations

### Architecture
- **Offline-First**: Room Database stores data locally, enabling full functionality offline
- **Reactive UI**: Jetpack Compose with state management for responsive interfaces
- **Background Sync**: WorkManager automatically synchronizes data every 15 minutes
- **Scalable Backend**: Supabase provides REST API, authentication, and real-time capabilities

### User Experience
- **Material 3 Design**: Modern, clean interface following Google's latest design guidelines
- **Bottom Navigation**: Easy access to main features (Restaurants, Cart, Tracking, Profile)
- **Intuitive Flow**: Clear progression from browsing → ordering → tracking
- **Multi-language**: Seamless language switching with persistent preferences

### Technical Design
- **Separation of Concerns**: Clean architecture with separate data, UI, and domain layers
- **Type Safety**: Kotlin with proper null safety and type inference
- **Network Resilience**: Automatic retry and error handling
- **Security**: Encrypted password storage, secure token management

## 🚀 Tech Stack

- **Frontend**: Android, Kotlin, Jetpack Compose, Material 3
- **Networking**: Retrofit + OkHttp + Moshi for REST API
- **Backend**: Supabase (PostgREST, Auth, Edge Functions)
- **Local Storage**: Room Database for offline data
- **Notifications**: Firebase Cloud Messaging (FCM)
- **Authentication**: Supabase Auth + Google Sign-In (SSO)
- **Background Tasks**: WorkManager for periodic sync
- **Dependency Injection**: Manual dependency management

## ✨ Features

### Core Features
- ✅ **User Authentication** - Secure login/signup with encrypted passwords via Supabase Auth
- ✅ **Single Sign-On (SSO)** - Google Sign-In integration for quick authentication
- ✅ **User Settings** - Language selection, notification preferences, account management
- ✅ **Backend REST API** - Full integration with Supabase REST API
- ✅ **Offline Mode** - Complete offline functionality with Room Database
- ✅ **Automatic Synchronization** - Background sync when connectivity is restored
- ✅ **Real-time Push Notifications** - Firebase Cloud Messaging for order updates
- ✅ **Multi-language Support** - English, Afrikaans, and isiZulu

### App Features
- **Restaurant Browsing**: Search and filter restaurants by cuisine
- **Menu Display**: Expandable category sections with detailed menu items
- **Shopping Cart**: Add, edit, and remove items with quantity controls
- **Promo Codes**: Apply discount codes (SAVE10, LESS20)
- **Checkout**: Address input, payment method selection, order placement
- **Order Tracking**: Real-time order status updates with visual progress
- **Profile Management**: Account details, order history, loyalty points
- **Settings**: Language, notifications, marketing preferences

## 📁 Project Structure

```
app/src/main/java/com/example/lindonndelivery2/
├── MainActivity.kt                 # App entry, navigation, locale handling
├── LinDonnApplication.kt          # Application class, WorkManager initialization
├── ui/
│   ├── auth/
│   │   └── LoginScreen.kt         # Authentication UI with Google Sign-In
│   ├── restaurants/
│   │   └── RestaurantsScreen.kt   # Restaurant list with search
│   ├── menu/
│   │   └── MenuScreen.kt          # Menu with expandable categories
│   ├── cart/
│   │   └── CartScreen.kt          # Shopping cart with promo codes
│   ├── checkout/
│   │   └── CheckoutScreen.kt      # Checkout flow
│   ├── tracking/
│   │   └── TrackingScreen.kt      # Order tracking
│   ├── profile/
│   │   └── ProfileScreen.kt       # Profile with tabs
│   └── settings/
│       └── SettingsScreen.kt      # User settings
├── data/
│   ├── network/
│   │   ├── ApiClient.kt           # Retrofit configuration
│   │   ├── AuthService.kt         # Authentication API
│   │   ├── UsersService.kt        # User management API
│   │   ├── RestaurantsService.kt  # Restaurant API
│   │   ├── MenuService.kt         # Menu API
│   │   └── OrdersService.kt       # Orders API
│   ├── local/
│   │   ├── entity/                # Room entities
│   │   ├── dao/                   # Room DAOs
│   │   └── AppDatabase.kt         # Room database
│   ├── sync/
│   │   ├── SyncRepository.kt      # Sync logic
│   │   └── SyncWorker.kt          # WorkManager worker
│   ├── notifications/
│   │   ├── FirebaseMessagingService.kt  # FCM service
│   │   └── FcmTokenManager.kt     # FCM token management
│   ├── auth/
│   │   └── GoogleSignInHelper.kt  # Google Sign-In helper
│   ├── SessionManager.kt          # Session management
│   └── cart/
│       └── CartStore.kt           # In-memory cart state
└── util/
    └── LocaleHelper.kt            # Language/locale management
```

## 🔧 Setup & Configuration

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK (API 33+)
- Supabase account
- Firebase project

### Backend Setup
1. **Supabase Configuration**: See [BACKEND_SETUP_GUIDE.md](BACKEND_SETUP_GUIDE.md)
   - Database schema setup
   - Row Level Security (RLS) policies
   - Edge Functions deployment
   - Database triggers for notifications

2. **Firebase Configuration**:
   - Create Firebase project
   - Add Android app
   - Download `google-services.json`
   - Enable Cloud Messaging

3. **Google Sign-In**:
   - Create OAuth 2.0 credentials
   - Configure consent screen
   - Add SHA-1 fingerprint
   - See [GOOGLE_OAUTH_CONSENT_SCREEN.md](GOOGLE_OAUTH_CONSENT_SCREEN.md)

### App Configuration
1. **Build Configuration**:
   - Update `app/build.gradle.kts` with Supabase URL and keys
   - Add `google-services.json` to `app/` directory
   - Configure package name in `AndroidManifest.xml`

2. **Environment Variables**:
   - `SUPABASE_URL`: Your Supabase project URL
   - `SUPABASE_ANON_KEY`: Your Supabase anon key
   - `default_web_client_id`: Google OAuth client ID

### Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run tests
./gradlew test
```

## 🎯 Innovative Features

### 1. Offline-First Architecture
**Innovation**: Complete offline functionality with automatic synchronization
- Room Database stores all critical data locally
- App works fully offline without internet connection
- Automatic background sync every 15 minutes
- Manual sync option for immediate updates
- Conflict resolution for data consistency

### 2. Multi-Language Support
**Innovation**: Native South African language support
- Three languages: English, Afrikaans, isiZulu
- Persistent language preferences
- App restart with selected language
- Comprehensive translations for all UI elements

### 3. Real-Time Push Notifications
**Innovation**: Automatic order status notifications
- Database triggers automatically send notifications
- Firebase Cloud Messaging integration
- Order status updates in real-time
- Background notification handling

### 4. Intelligent Cart Management
**Innovation**: Advanced cart features with promo codes
- Quantity controls with validation
- Promo code system (SAVE10, LESS20)
- Delivery fee calculation
- Subtotal and total calculations
- Persistent cart state

### 5. Secure Authentication
**Innovation**: Multiple authentication methods
- Email/password with encrypted storage
- Google Sign-In (SSO) integration
- Secure token management
- Session persistence

## 📋 Release Notes

### Version 2.0.0 - Current Release

#### New Features
- ✨ **Offline Mode**: Complete offline functionality with Room Database
- ✨ **Multi-language Support**: English, Afrikaans, and isiZulu
- ✨ **Push Notifications**: Real-time order status updates via FCM
- ✨ **Google Sign-In**: SSO authentication integration
- ✨ **User Settings**: Comprehensive settings screen with preferences
- ✨ **Background Sync**: Automatic data synchronization with WorkManager
- ✨ **FCM Token Management**: Automatic token storage and updates

#### Improvements
- 🔄 **Synchronization**: Automatic sync every 15 minutes
- 🔄 **Error Handling**: Improved error messages and retry logic
- 🔄 **UI/UX**: Material 3 design implementation
- 🔄 **Performance**: Optimized database queries and network calls
- 🔄 **Security**: Enhanced authentication and token management

#### Technical Updates
- 📦 **Room Database**: Local storage for restaurants, menu, orders, cart
- 📦 **WorkManager**: Background task scheduling
- 📦 **Firebase Cloud Messaging**: Push notification infrastructure
- 📦 **Google Sign-In SDK**: SSO authentication
- 📦 **LocaleHelper**: Language management utility

#### Bug Fixes
- 🐛 Fixed cart persistence issues
- 🐛 Fixed order synchronization conflicts
- 🐛 Fixed language switching behavior
- 🐛 Fixed notification channel creation

### Version 1.0.0 - Prototype

#### Initial Features
- Basic restaurant browsing
- Menu display
- Shopping cart
- Checkout flow
- Order tracking
- User profile
- Supabase integration

## 🔒 Security

- **Password Encryption**: Supabase Auth handles password encryption
- **Token Management**: Secure JWT token storage and validation
- **API Security**: Row Level Security (RLS) policies in Supabase
- **Network Security**: HTTPS for all API calls
- **Data Privacy**: Local data encryption with Room Database

## 🧪 Testing

### Automated Testing (CI/CD)
The project uses GitHub Actions for continuous integration:

- **Unit Tests**: Runs on every push and pull request
- **Build Verification**: Builds debug and release APKs
- **Code Linting**: Checks code quality and style
- **Instrumentation Tests**: Runs UI tests on Android emulator

View test results and build artifacts in the [Actions](https://github.com/yourusername/LinDonnDelivery2/actions) tab.

#### Running Tests Locally
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### Manual Testing
1. **Authentication**: Test login, signup, password recovery
2. **Offline Mode**: Test app functionality without internet
3. **Synchronization**: Test data sync when connectivity is restored
4. **Notifications**: Test push notifications for order updates
5. **Multi-language**: Test language switching and persistence
6. **Cart**: Test add, edit, remove items, promo codes
7. **Checkout**: Test order placement and tracking

### Test Checklist
- [ ] User authentication (email/password)
- [ ] Google Sign-In (if configured)
- [ ] Restaurant browsing (online/offline)
- [ ] Menu display and filtering
- [ ] Cart operations
- [ ] Checkout flow
- [ ] Order tracking
- [ ] Push notifications
- [ ] Language switching
- [ ] Settings management
- [ ] Offline synchronization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent formatting

## 👥 Authors

- LinDonn Delivery Team

## 🙏 Acknowledgments

- Supabase for backend infrastructure
- Firebase for push notifications
- Google for authentication services
- Jetpack Compose team for UI framework
- Material Design team for design guidelines

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing documentation
- Review backend setup guide


