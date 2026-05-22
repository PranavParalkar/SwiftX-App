# SwiftX — Project Context & Progress Log

> This file tracks all work done on the SwiftX FinTech Financial Inclusion Platform, including what was implemented, how it was done, and key decisions made.

## Project Overview
- **Name**: SwiftX
- **Type**: FinTech Financial Inclusion Platform
- **Target Users**: Migrant workers, unbanked/underbanked populations
- **Platform**: Android (Native Java)
- **Database**: PostgreSQL via Supabase
- **Architecture**: MVVM-lite with Repository pattern

## Session 1 — 2026-05-22: Foundation & Core Setup

### What was done:
1. **Implementation Plan Created** — Full architecture doc with phases, file structure, DB schema, design tokens
2. **Dependencies Added** — Retrofit2, OkHttp3, Gson, Material Design 3, Lottie, MPAndroidChart, ViewPager2, CardView, RecyclerView
3. **Custom Theme** — Premium dark theme with SwiftX branding (teal/purple gradient palette)
4. **Color System** — Full design token palette: SwiftX Teal (#00C9A7), Purple accent (#845EF7), Deep navy background (#0A0E27)
5. **Splash Activity** — Animated splash screen with branding and fade transition
6. **Onboarding Activity** — 3-screen ViewPager onboarding with dot indicators and animations
7. **Login Activity** — Premium login with email/OTP, gradient header, social login buttons
8. **Register Activity** — Full registration form with name, email, phone, country, currency selection
9. **MainActivity + Navigation** — Bottom navigation with 4 tabs (Home, Wallet, Activity, Profile)
10. **Home Fragment** — Dashboard with balance card, quick actions grid, recent transactions, AI insights card
11. **Wallet Fragment** — Multi-currency wallet display with send/receive actions
12. **Transactions Fragment** — Hash-chained transaction list with search/filter
13. **Profile Fragment** — User profile with KYC status, settings, security options
14. **Send Money Activity** — Full remittance flow with recipient, amount, FX rate, review & confirm
15. **Model Classes** — User, Wallet, Transaction, FxRate, AmlFlag, Insight, PolygonAnchor
16. **Network Layer** — ApiClient (Retrofit), SupabaseApi interface
17. **Utility Classes** — Constants, SessionManager, CurrencyFormatter
18. **Adapters** — TransactionAdapter, OnboardingPagerAdapter
19. **Services** — HashChainService for transaction integrity
20. **Animations** — Fade in/out, slide transitions, scale animations
21. **Drawables** — Gradient backgrounds, rounded cards, custom button styles, icons
22. **AndroidManifest** — All activities registered with proper intent filters and themes

## Session 2 — 2026-05-22: Migration to Native Android (Android VM)

### What was done:
1. **React Native Removed** — Deleted `frontend/` directory and React dependencies.
2. **Native XML Layouts** — Created premium dark mode XML layouts for Splash, Onboarding, Login, and Main Activity.
3. **Java Activities** — Implemented `SplashActivity`, `OnboardingActivity`, `LoginActivity`, and `MainActivity`.
4. **Fragments & Adapters** — Created `HomeFragment` and `TransactionAdapter`.
5. **Network Integration** — Wired native Java models and Retrofit clients to the UI.
6. **Build System** — Validated native Android build using `./gradlew`.


### Architecture Decisions:
- **Why Supabase?** — Provides PostgreSQL + Auth + RLS out of box, no backend server needed
- **Why MVVM-lite?** — Simpler than full MVVM for this project, Activities handle both View and ViewModel concerns
- **Why hash chain in client?** — Enables offline verification of transaction integrity
- **Why Material Design 3?** — Latest Android design language, premium feel with dark theme support

### Key File Paths:
```
app/src/main/java/com/example/swift_app/
├── activities/ — All Activity classes
├── fragments/ — Bottom nav tab fragments
├── adapters/ — RecyclerView adapters
├── models/ — Data model POJOs
├── network/ — Retrofit API interfaces
├── services/ — Business logic services
└── utils/ — Utility/helper classes

app/src/main/res/
├── layout/ — All XML layouts
├── drawable/ — Shapes, gradients, icons
├── anim/ — Animation resources
├── values/ — Colors, strings, themes, dimens
└── font/ — Custom fonts (Inter)
```

### What's Next (Phase 2+):
- Supabase integration with real auth flow
- Receive Money with QR generation
- FX rate service with exchangerate.host
- AI Insights with Claude API
- AML rules engine
- Polygon blockchain anchoring
- Multilingual support
- Lottie animation assets
