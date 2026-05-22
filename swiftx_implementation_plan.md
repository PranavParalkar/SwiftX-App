# SwiftX — Implementation Plan

## Overview
SwiftX is a FinTech Financial Inclusion Platform providing zero-fee remittances, secure digital wallets, and accessible investment tools for migrant workers and underserved populations.

## Tech Stack
| Layer | Technology |
|-------|-----------|
| **Frontend** | Native Android (Java) with XML Layouts |
| **Backend** | Java business logic within Android + Supabase as BaaS (REST API) |
| **Database** | PostgreSQL via Supabase (cloud-hosted) |
| **Networking** | Retrofit2 + OkHttp3 |
| **JSON** | Gson |
| **Charts** | MPAndroidChart |
| **Animations** | XML Animations + Android ViewPropertyAnimator |
| **Auth** | Supabase Auth (simulated via REST/Profiles) |
| **AI** | Anthropic Claude API (via Retrofit) |
| **Blockchain** | Polygon Amoy (via Web3j) |
| **FX Rates** | exchangerate.host API |

## Architecture Pattern
- **MVVM-lite**: Activities/Fragments → Repositories → Supabase API
- **Repository Pattern**: Clean data access layer
- **Hash Chain**: Local computation with server-side persistence
- **Offline Support**: SharedPreferences + local caching

## Phase Breakdown

### Phase 1: Foundation ✅ (Current)
- [x] Project scaffolding
- [x] Custom SwiftX theme (dark/premium)
- [x] Navigation infrastructure (Bottom Nav)
- [x] Base Activity/Fragment classes
- [x] Splash screen with branding
- [x] Dependency setup (Retrofit, Gson, Material)
- [x] Login & Onboarding (Native XML)

### Phase 2: Auth & Profiles
- [ ] Welcome / Onboarding screens
- [ ] Login Activity (email + OTP)
- [ ] Registration Activity
- [ ] KYC Status screens
- [ ] Profile Management
- [ ] Session management with tokens

### Phase 3: Wallet & Transactions
- [ ] Wallet Dashboard (balance cards, quick actions)
- [ ] Send Money flow
- [ ] Receive Money (QR code + share link)
- [ ] Transaction History (hash-chained ledger)
- [ ] Transaction Detail view

### Phase 4: Remittance & FX
- [ ] Zero-fee transfer flow
- [ ] FX rate display with real-time cache
- [ ] Multi-currency wallet support
- [ ] Recipient management (contacts)
- [ ] Transfer confirmation + receipt

### Phase 5: AML & Compliance
- [ ] AML rules engine (client-side pre-checks)
- [ ] Transaction flagging
- [ ] Admin review dashboard
- [ ] KYC document upload
- [ ] Compliance audit trail

### Phase 6: AI Insights
- [ ] Claude API integration
- [ ] Financial tips cards
- [ ] Savings recommendations
- [ ] Investment suggestions
- [ ] Spending analytics

### Phase 7: Blockchain Anchoring
- [ ] Hash chain verification
- [ ] Polygon anchor service
- [ ] Verification status UI
- [ ] Public audit trail

### Phase 8: Polish
- [ ] Lottie animations
- [ ] Multilingual support (i18n)
- [ ] Accessibility (a11y)
- [ ] Performance optimization
- [ ] Error handling & empty states

## File Structure
```
app/src/main/
├── java/com/example/swift_app/
│   ├── activities/
│   │   ├── SplashActivity.java
│   │   ├── OnboardingActivity.java
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   ├── MainActivity.java
│   │   ├── SendMoneyActivity.java
│   │   ├── ReceiveMoneyActivity.java
│   │   ├── TransactionDetailActivity.java
│   │   ├── ProfileActivity.java
│   │   ├── KycActivity.java
│   │   └── InsightsActivity.java
│   ├── fragments/
│   │   ├── HomeFragment.java
│   │   ├── WalletFragment.java
│   │   ├── TransactionsFragment.java
│   │   ├── InsightsFragment.java
│   │   └── ProfileFragment.java
│   ├── adapters/
│   │   ├── TransactionAdapter.java
│   │   ├── CurrencyAdapter.java
│   │   ├── InsightCardAdapter.java
│   │   └── OnboardingPagerAdapter.java
│   ├── models/
│   │   ├── User.java
│   │   ├── Wallet.java
│   │   ├── Transaction.java
│   │   ├── FxRate.java
│   │   ├── AmlFlag.java
│   │   ├── Insight.java
│   │   └── PolygonAnchor.java
│   ├── network/
│   │   ├── ApiClient.java
│   │   ├── SupabaseApi.java
│   │   ├── FxApi.java
│   │   ├── ClaudeApi.java
│   │   └── PolygonApi.java
│   ├── repositories/
│   │   ├── AuthRepository.java
│   │   ├── WalletRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── FxRepository.java
│   │   ├── InsightsRepository.java
│   │   └── AmlRepository.java
│   ├── services/
│   │   ├── HashChainService.java
│   │   ├── AmlRulesEngine.java
│   │   └── PolygonAnchorService.java
│   └── utils/
│       ├── Constants.java
│       ├── SessionManager.java
│       ├── CurrencyFormatter.java
│       └── AnimationUtils.java
├── res/
│   ├── layout/
│   ├── drawable/
│   ├── values/
│   ├── anim/
│   ├── font/
│   └── raw/ (Lottie JSON files)
└── AndroidManifest.xml
```

## Database Schema (Supabase PostgreSQL)
```sql
-- profiles
CREATE TABLE profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  full_name TEXT,
  phone TEXT,
  country TEXT,
  preferred_currency TEXT DEFAULT 'USD',
  kyc_status TEXT DEFAULT 'pending',
  created_at TIMESTAMPTZ DEFAULT now()
);

-- wallets
CREATE TABLE wallets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES profiles(id),
  currency TEXT NOT NULL,
  balance DECIMAL(18,2) DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, currency)
);

-- transactions
CREATE TABLE transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sender_id UUID REFERENCES profiles(id),
  recipient_id UUID REFERENCES profiles(id),
  amount DECIMAL(18,2) NOT NULL,
  currency TEXT NOT NULL,
  converted_amount DECIMAL(18,2),
  target_currency TEXT,
  fx_rate DECIMAL(12,6),
  status TEXT DEFAULT 'pending',
  type TEXT NOT NULL, -- 'transfer', 'deposit', 'withdrawal'
  prev_hash TEXT,
  current_hash TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- fx_rates_cache
CREATE TABLE fx_rates_cache (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  base_currency TEXT NOT NULL,
  target_currency TEXT NOT NULL,
  rate DECIMAL(12,6) NOT NULL,
  fetched_at TIMESTAMPTZ DEFAULT now()
);

-- aml_flags
CREATE TABLE aml_flags (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id UUID REFERENCES transactions(id),
  user_id UUID REFERENCES profiles(id),
  rule_triggered TEXT NOT NULL,
  severity TEXT DEFAULT 'low',
  reviewed BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- polygon_anchors
CREATE TABLE polygon_anchors (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chain_root_hash TEXT NOT NULL,
  polygon_tx_hash TEXT NOT NULL,
  block_number BIGINT,
  anchored_at TIMESTAMPTZ DEFAULT now()
);
```

## Key Design Tokens
| Token | Value |
|-------|-------|
| Primary | `#00C9A7` (SwiftX Teal) |
| Primary Dark | `#00A88A` |
| Secondary | `#845EF7` (Purple accent) |
| Background | `#0A0E27` (Deep navy) |
| Surface | `#141937` (Card dark) |
| Text Primary | `#FFFFFF` |
| Text Secondary | `#8892B0` |
| Success | `#00E676` |
| Warning | `#FFB74D` |
| Error | `#FF5252` |
| Border Radius | `16dp` |
| Font Family | Inter / Roboto |
