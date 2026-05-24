# SwiftX Registration Troubleshooting

## 🚨 Problem: "User already exists" but user wasn't created

### What's Happening
1. **Auth user IS created** in Supabase Auth
2. **Profile creation FAILS** (database issue)
3. Result: Zombie auth user without profile

### Solution Steps

## Step 1: Check Database Schema

**Run this SQL in your Supabase SQL Editor:**

```sql
-- Check if tables exist
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('profiles', 'wallets', 'transactions');

-- Check profiles table structure
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'profiles' ORDER BY ordinal_position;
```

**Expected output:**
- `profiles` table should exist
- Should have columns: `id`, `email`, `full_name`, `role`, `kyc_status`, etc.

## Step 2: Set Up Database (If Missing)

**Copy and run the entire SQL from:**
- `SETUP_DATABASE.sql` (in this folder)
- OR `referenceproject/app/supabase/schema.sql`

## Step 3: Clean Up Zombie Users

**In Supabase SQL Editor, run:**

```sql
-- List all auth users
SELECT * FROM auth.users WHERE email = 'your-email@example.com';

-- Delete zombie auth user (if needed)
-- WARNING: This requires admin privileges
-- DELETE FROM auth.users WHERE email = 'your-email@example.com';
```

## Step 4: Test Registration Flow

### With Logging Enabled
The app now has detailed logging. Check Logcat for:

```
D/RegisterActivity: Creating profile for user: email@example.com (ID: uuid)
D/RegisterActivity: Profile data: Name, Country, Phone
E/RegisterActivity: Profile creation failed: 400 - Bad Request
E/RegisterActivity: Error body: {"message":"column \"full_name\" does not exist"}
```

### Common Error Messages & Fixes

| Error Message | Solution |
|---------------|----------|
| `"column \"full_name\" does not exist"` | Run database setup script |
| `"relation \"profiles\" does not exist"` | Table doesn't exist - run setup |
| `"permission denied for table profiles"` | Enable RLS or check permissions |
| `"duplicate key value violates unique constraint"` | User already exists - clean up |

## Step 5: Manual Registration Test

### Option A: Use Supabase Dashboard
1. Go to **Authentication** → **Users**
2. Click **"Invite User"**
3. Enter email and password
4. User receives invite email

### Option B: Direct SQL Insert
```sql
-- First get the auth user ID
SELECT id FROM auth.users WHERE email = 'test@example.com';

-- Then insert profile (replace UUID with actual ID)
INSERT INTO profiles (id, email, full_name, role, kyc_status) 
VALUES ('user-uuid-here', 'test@example.com', 'Test User', 'user', 'pending');
```

## Step 6: Verify Setup

### Check Tables Exist
```sql
-- Should return 3 rows
SELECT COUNT(*) as table_count FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('profiles', 'wallets', 'transactions');
```

### Check RLS Policies
```sql
-- Should return policies
SELECT schemaname, tablename, policyname, permissive, roles, cmd 
FROM pg_policies 
WHERE tablename IN ('profiles', 'wallets', 'transactions');
```

## 🛠️ Quick Fix Script

If you want to reset everything:

```sql
-- WARNING: This will delete all data!
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS wallets CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP SEQUENCE IF EXISTS rm_id_seq;
DROP SEQUENCE IF EXISTS txn_seq;
DROP FUNCTION IF EXISTS set_updated_at() CASCADE;
DROP FUNCTION IF EXISTS create_wallet_for_profile() CASCADE;
DROP FUNCTION IF EXISTS generate_txn_ref() CASCADE;
DROP FUNCTION IF EXISTS execute_transfer CASCADE;

-- Then run SETUP_DATABASE.sql
```

## 📱 App-Side Debugging

### Enable Network Logging
The app already logs all network requests. Check for:

```
I/okhttp.OkHttpClient: --> POST https://.../auth/v1/signup
I/okhttp.OkHttpClient: {"password":"...","email":"..."}
I/okhttp.OkHttpClient: <-- 401 https://.../auth/v1/signup (271ms)
I/okhttp.OkHttpClient: {"message":"Invalid API key"}
```

### Test API Key
```bash
# Test your Supabase API key
curl -X POST 'https://YOUR_PROJECT.supabase.co/auth/v1/signup' \
  -H 'apikey: YOUR_ANON_KEY' \
  -H 'Content-Type: application/json' \
  -d '{"email":"test@example.com","password":"password123"}'
```

## 🔧 Alternative: Simplified Registration

If database setup is too complex, use this temporary fix:

1. **Modify `RegisterActivity.java`**:
```java
// After auth user creation, skip profile creation
sessionManager.saveSession(token, userId, email, name);
startActivity(new Intent(RegisterActivity.this, MainActivity.class));
finish();
```

2. **Create profile manually later** via Supabase dashboard

## 📞 Support

### If Still Having Issues:

1. **Share Logcat Output** (filter by "RegisterActivity" or "okhttp")
2. **Check Supabase Logs** in your project dashboard
3. **Verify API Key** is correct in Constants.java
4. **Test with Postman/curl** to isolate the issue

### Useful Links:
- [Supabase Auth Docs](https://supabase.com/docs/guides/auth)
- [Supabase RLS Guide](https://supabase.com/docs/guides/auth/row-level-security)
- [PostgreSQL Schema Guide](https://www.postgresql.org/docs/current/ddl.html)

---

**Remember:** The issue is that auth succeeds but database operations fail. Focus on database setup first! 🎯
