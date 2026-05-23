# SwiftX Android App - Setup Guide

## 🚀 Quick Start

This guide will help you set up and run the SwiftX Android application.

## 📋 Prerequisites

- **Android Studio** (Arctic Fox or later)
- **JDK 11** or higher
- **Android SDK** (API 24+, Target API 35)
- **Supabase Account** (for backend services)

## 🔧 Configuration Steps

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd SwiftX-App
```

### 2. Configure Supabase

#### A. Get Your Supabase Credentials

1. Go to [supabase.com](https://supabase.com) and create a project
2. Navigate to **Settings** → **API**
3. Copy your:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **Anon/Public Key** (starts with `eyJ...`)

#### B. Set Up Database Schema

1. In your Supabase project, go to **SQL Editor**
2. Copy the schema from `referenceproject/app/supabase/schema.sql`
3. Run the SQL script to create all tables, functions, and policies

#### C. Update Constants.java

Open `app/src/main/java/com/example/swift_app/utils/Constants.java` and update:

```java
public static final String SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
public static final String SUPABASE_ANON_KEY = "YOUR_ANON_KEY_HERE";
```

### 3. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect an Android device or start an emulator
4. Click **Run** (or press Shift+F10)

## 📱 Features Implemented

### ✅ Core Features
- **Authentication**: Login and Registration with Supabase Auth
- **Multi-Currency Wallet**: USD, INR, AED support
- **Send Money**: Real-time FX rates, atomic transfers
- **Add Funds**: Deposit with AML checks
- **Currency Exchange**: Live exchange rates
- **Transaction History**: Complete audit trail
- **KYC Submission**: Document upload capability
- **Profile Management**: User settings and preferences

### ✅ Technical Features
- **Real-time FX Rates**: Integration with exchangerate-api.com
- **Atomic Transactions**: Database-level transfer function
- **Row Level Security**: Supabase RLS policies
- **AML Compliance**: Transaction limits and velocity checks
- **Unified Wallet Model**: Multi-currency in single wallet
- **Proper Error Handling**: Network and validation errors

## 🔐 Security Notes

### Production Checklist

- [ ] Replace placeholder Supabase keys with production keys
- [ ] Enable ProGuard for release builds
- [ ] Set up proper signing configuration
- [ ] Use EncryptedSharedPreferences for tokens
- [ ] Implement certificate pinning for API calls
- [ ] Add biometric authentication
- [ ] Enable network security config

### Current Security Status

⚠️ **Development Mode**: The app currently uses:
- Tokens stored in SharedPreferences (not encrypted)
- Cleartext traffic disabled
- No certificate pinning

## 🧪 Testing

### Test User Creation

1. Launch the app
2. Click "Create an account"
3. Fill in the registration form
4. A wallet will be automatically created

### Test Transactions

1. Create two test users
2. Add funds to User A
3. Send money from User A to User B using their email or RM ID
4. Check transaction history in both accounts

### Admin Access

To create an admin user:
1. Register normally with email `admin@swiftx.ai`
2. In Supabase, manually update the `role` field to `'admin'`
3. Login to access Admin Dashboard

## 🐛 Troubleshooting

### Build Errors

**Issue**: Gradle sync fails
```bash
# Solution: Clean and rebuild
./gradlew clean
./gradlew build
```

**Issue**: Dependency resolution errors
```bash
# Solution: Invalidate caches
File → Invalidate Caches → Invalidate and Restart
```

### Runtime Errors

**Issue**: "Network error" on login
- Check your internet connection
- Verify Supabase URL and keys in Constants.java
- Check Supabase project status

**Issue**: "Profile not found" after login
- Ensure database schema is properly set up
- Check that the `profiles` table trigger is working
- Verify RLS policies are enabled

**Issue**: "Insufficient balance" on transfer
- Add funds first using the Add Funds feature
- Check wallet balance in the Wallet tab

### API Issues

**Issue**: FX rates not loading
- The app uses exchangerate-api.com (free tier)
- Falls back to cached rates if API fails
- Check internet connectivity

**Issue**: Transactions failing
- Verify the `execute_transfer` function exists in Supabase
- Check sender has sufficient balance
- Ensure recipient exists in the system

## 📚 Project Structure

```
app/src/main/java/com/example/swift_app/
├── activities/          # All activity screens
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── MainActivity.java
│   ├── SendMoneyActivity.java
│   ├── AddFundsActivity.java
│   └── ...
├── fragments/           # Bottom nav fragments
│   ├── HomeFragment.java
│   ├── ActivityFragment.java
│   ├── WalletFragment.java
│   └── ...
├── models/             # Data models
│   ├── User.java
│   ├── Wallet.java
│   ├── Transaction.java
│   └── ...
├── network/            # API interfaces
│   ├── ApiClient.java
│   ├── SupabaseApi.java
│   └── FxApi.java
├── repositories/       # Data repositories
│   └── WalletRepository.java
├── services/           # Business logic
│   ├── AmlRulesEngine.java
│   ├── AiService.java
│   └── ...
├── adapters/           # RecyclerView adapters
└── utils/              # Utilities
    ├── Constants.java
    ├── SessionManager.java
    └── ...
```

## 🔄 Database Schema

The app uses the following main tables:

- **profiles**: User accounts with RM IDs
- **wallets**: Multi-currency balances (INR, USD, AED)
- **transactions**: Transfer records with FX rates
- **exchange_rates**: Cached FX rates
- **beneficiaries**: Saved recipients
- **notifications**: User notifications
- **audit_logs**: Admin audit trail

## 🌐 API Endpoints

### Supabase REST API
- Base URL: `{SUPABASE_URL}/rest/v1/`
- Authentication: Bearer token + apikey header
- All requests use RLS policies

### Supabase Auth API
- Base URL: `{SUPABASE_URL}/auth/v1/`
- Endpoints: `/signup`, `/token?grant_type=password`

### Exchange Rate API
- Base URL: `https://api.exchangerate-api.com/v4/`
- Endpoint: `/latest/{currency}`
- No API key required (free tier)

## 📝 Environment Variables

For production, consider using BuildConfig fields:

```gradle
// In app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${System.getenv("SUPABASE_KEY")}\"")
    }
}
```

## 🚀 Deployment

### Release Build

1. Configure signing in `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("your-keystore.jks")
            storePassword = "your-password"
            keyAlias = "your-alias"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(...)
        }
    }
}
```

2. Build release APK:
```bash
./gradlew assembleRelease
```

3. Find APK at: `app/build/outputs/apk/release/app-release.apk`

## 📞 Support

For issues or questions:
1. Check this guide first
2. Review the PWA reference implementation in `referenceproject/`
3. Check Supabase logs for backend errors
4. Review Android Logcat for app errors

## 🎯 Next Steps

### Recommended Enhancements

1. **Payment Gateway Integration**
   - Integrate Razorpay/Stripe for real deposits
   - Add payment method management

2. **Biometric Authentication**
   - Add fingerprint/face unlock
   - Secure transaction confirmation

3. **Push Notifications**
   - Integrate Firebase Cloud Messaging
   - Send transaction alerts

4. **Offline Support**
   - Cache wallet balances
   - Queue transactions for later

5. **Advanced Features**
   - Recurring transfers
   - Bill payments
   - QR code payments
   - Transaction receipts (PDF)

## ✅ Production Readiness Checklist

- [ ] Replace all placeholder API keys
- [ ] Enable ProGuard/R8 minification
- [ ] Set up proper error tracking (Firebase Crashlytics)
- [ ] Implement analytics (Firebase Analytics)
- [ ] Add proper logging (Timber)
- [ ] Set up CI/CD pipeline
- [ ] Perform security audit
- [ ] Test on multiple devices/Android versions
- [ ] Optimize APK size
- [ ] Add app signing
- [ ] Configure backup rules
- [ ] Test with production Supabase instance

---

**Version**: 1.0.0  
**Last Updated**: 2026-05-23  
**Minimum Android Version**: 7.0 (API 24)  
**Target Android Version**: 14 (API 35)
