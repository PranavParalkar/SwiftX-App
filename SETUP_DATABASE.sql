-- SwiftX Database Setup Script
-- Run this in your Supabase SQL Editor

-- Enable extensions
create extension if not exists "uuid-ossp";
create extension if not exists "pgcrypto";

-- ============================================================
-- DROP EXISTING POLICIES (so re-runs don't fail)
-- ============================================================
drop policy if exists "profiles_select_own" on profiles;
drop policy if exists "profiles_update_own"  on profiles;
drop policy if exists "profiles_insert_own"  on profiles;
drop policy if exists "wallets_own"           on wallets;
drop policy if exists "txn_own"               on transactions;
drop policy if exists "txn_insert"            on transactions;

-- ============================================================
-- DROP EXISTING TRIGGERS (so re-runs don't fail)
-- ============================================================
drop trigger if exists trg_profiles_updated_at  on profiles;
drop trigger if exists trg_wallets_updated_at   on wallets;
drop trigger if exists trg_transactions_updated_at on transactions;
drop trigger if exists trg_create_wallet        on profiles;
drop trigger if exists trg_generate_txn_ref     on transactions;

-- ============================================================
-- DROP EXISTING TABLES (clean slate — order matters for FK)
-- ============================================================
drop table if exists transactions cascade;
drop table if exists wallets      cascade;
drop table if exists profiles     cascade;

-- ============================================================
-- SEQUENCES
-- ============================================================
create sequence if not exists rm_id_seq  start 10001;
create sequence if not exists txn_seq    start 10001;

-- ============================================================
-- PROFILES TABLE
-- ============================================================
create table profiles (
  id            uuid primary key references auth.users(id) on delete cascade,
  rm_id         text unique not null default ('SX' || nextval('rm_id_seq')::text),
  full_name     text not null,
  email         text unique not null,
  phone         text unique,
  country       text default 'IN',
  kyc_status    text default 'pending' check (kyc_status in ('pending','submitted','verified','rejected')),
  role          text default 'user'    check (role in ('user','admin')),
  is_frozen     boolean default false,
  bank_fee_rate numeric(6,5) default round((0.0025 + random() * 0.0027)::numeric, 5),
  created_at    timestamptz default now(),
  updated_at    timestamptz default now()
);

-- ============================================================
-- WALLETS TABLE
-- ============================================================
create table wallets (
  id          uuid primary key default uuid_generate_v4(),
  user_id     uuid unique not null references profiles(id) on delete cascade,
  inr_balance numeric(18,2) default 0 check (inr_balance >= 0),
  usd_balance numeric(18,6) default 0 check (usd_balance >= 0),
  aed_balance numeric(18,6) default 0 check (aed_balance >= 0),
  created_at  timestamptz default now(),
  updated_at  timestamptz default now()
);

-- ============================================================
-- TRANSACTIONS TABLE
-- ============================================================
create table transactions (
  id               uuid primary key default uuid_generate_v4(),
  txn_ref          text unique not null,
  sender_id        uuid not null references profiles(id),
  receiver_id      uuid not null references profiles(id),
  source_currency  text not null,
  target_currency  text not null,
  source_amount    numeric(18,2) not null check (source_amount > 0),
  target_amount    numeric(18,6) not null,
  fx_rate          numeric(18,6) not null,
  fee_amount       numeric(18,2) default 0,
  fee_currency     text default 'INR',
  status           text default 'pending' check (status in ('pending','processing','completed','failed','reversed')),
  note             text,
  locked_rate_at   timestamptz,
  completed_at     timestamptz,
  created_at       timestamptz default now(),
  updated_at       timestamptz default now()
);

-- ============================================================
-- SHARED FUNCTIONS & TRIGGERS
-- ============================================================
create or replace function set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger trg_profiles_updated_at
  before update on profiles
  for each row execute function set_updated_at();

create trigger trg_wallets_updated_at
  before update on wallets
  for each row execute function set_updated_at();

create trigger trg_transactions_updated_at
  before update on transactions
  for each row execute function set_updated_at();

-- Auto-create wallet when profile is created
create or replace function create_wallet_for_profile()
returns trigger language plpgsql as $$
begin
  insert into wallets (user_id) values (new.id);
  return new;
end;
$$;

create trigger trg_create_wallet
  after insert on profiles
  for each row execute function create_wallet_for_profile();

-- Auto-generate TXN reference
create or replace function generate_txn_ref()
returns trigger language plpgsql as $$
begin
  if new.txn_ref is null or new.txn_ref = '' then
    new.txn_ref := 'TXN_' || nextval('txn_seq')::text;
  end if;
  return new;
end;
$$;

create trigger trg_generate_txn_ref
  before insert on transactions
  for each row execute function generate_txn_ref();

-- ============================================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================================
alter table profiles     enable row level security;
alter table wallets      enable row level security;
alter table transactions enable row level security;

-- Admin helper: SECURITY DEFINER + plpgsql avoids RLS recursion
-- and defers column resolution to runtime (not parse time).
create or replace function is_admin()
returns boolean
language plpgsql
security definer
stable
as $$
begin
  return exists (
    select 1 from profiles where id = auth.uid() and role = 'admin'
  );
end;
$$;

-- profiles
create policy "profiles_select_own" on profiles for select
  using (auth.uid() = id or is_admin());
create policy "profiles_update_own" on profiles for update
  using (auth.uid() = id);
create policy "profiles_insert_own" on profiles for insert
  with check (auth.uid() = id);

-- wallets
create policy "wallets_own" on wallets for all
  using (user_id = auth.uid() or is_admin());

-- transactions
create policy "txn_own" on transactions for select
  using (sender_id = auth.uid() or receiver_id = auth.uid() or is_admin());
create policy "txn_insert" on transactions for insert
  with check (sender_id = auth.uid());

-- ============================================================
-- TRANSFER FUNCTION (atomic, anti-double-spend)
-- ============================================================
create or replace function execute_transfer(
  p_sender_id       uuid,
  p_receiver_id     uuid,
  p_source_currency text,
  p_target_currency text,
  p_source_amount   numeric,
  p_target_amount   numeric,
  p_fx_rate         numeric,
  p_fee_amount      numeric,
  p_note            text default null
) returns uuid language plpgsql security definer as $$
declare
  v_txn_id uuid;
  v_total  numeric;
begin
  v_total := p_source_amount + p_fee_amount;

  -- Deduct from sender
  if p_source_currency = 'INR' then
    update wallets set inr_balance = inr_balance - v_total
      where user_id = p_sender_id and inr_balance >= v_total;
  elsif p_source_currency = 'USD' then
    update wallets set usd_balance = usd_balance - v_total
      where user_id = p_sender_id and usd_balance >= v_total;
  elsif p_source_currency = 'AED' then
    update wallets set aed_balance = aed_balance - v_total
      where user_id = p_sender_id and aed_balance >= v_total;
  end if;

  if not found then
    raise exception 'Insufficient balance';
  end if;

  -- Credit receiver
  if p_target_currency = 'INR' then
    update wallets set inr_balance = inr_balance + p_target_amount where user_id = p_receiver_id;
  elsif p_target_currency = 'USD' then
    update wallets set usd_balance = usd_balance + p_target_amount where user_id = p_receiver_id;
  elsif p_target_currency = 'AED' then
    update wallets set aed_balance = aed_balance + p_target_amount where user_id = p_receiver_id;
  end if;

  -- Record transaction
  insert into transactions (
    sender_id, receiver_id, source_currency, target_currency,
    source_amount, target_amount, fx_rate, fee_amount,
    status, note, locked_rate_at, completed_at
  ) values (
    p_sender_id, p_receiver_id, p_source_currency, p_target_currency,
    p_source_amount, p_target_amount, p_fx_rate, p_fee_amount,
    'completed', p_note, now(), now()
  ) returning id into v_txn_id;

  return v_txn_id;
end;
$$;

-- ============================================================
-- TEST DATA (Optional — uncomment to use)
-- ============================================================
/*
INSERT INTO profiles (id, email, full_name, role, kyc_status)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin@swiftx.ai', 'Administrator', 'admin', 'verified')
ON CONFLICT (id) DO NOTHING;
*/
