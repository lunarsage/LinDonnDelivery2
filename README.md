# LinDonn Delivery 2

A Jetpack Compose Android app backed by Supabase (PostgREST + Auth). Users can browse restaurants, view menus, add to cart, apply promos, checkout, and track orders. Includes a bottom navigation bar and a Profile area with account details, order history, and loyalty points.

## Tech Stack
- Android, Kotlin, Jetpack Compose, Material 3
- Retrofit + OkHttp + Moshi
- Supabase (PostgREST, Auth)

## Features
- Login / Signup with Supabase Auth (Email + Password)
- Restaurants list with search
- Menu by categories (expandable sections)
- Cart with edit controls (+/−/Remove), promo codes (SAVE10 / LESS20), delivery fee
- Checkout (address input, payment toggle, Place Order)
- Order Tracking (status flow)
- Profile (Account, Orders, Loyalty)

## Project Structure
- `app/src/main/java/com/example/lindonndelivery2/MainActivity.kt`
  - App entry, Compose navigation state, bottom navigation bar via Material3 `Scaffold`.
- `ui/auth/LoginScreen.kt` — authentication UI and flows.
- `ui/restaurants/RestaurantsScreen.kt` — list with search.
- `ui/menu/MenuScreen.kt` — expandable category sections, add-to-cart.
- `ui/cart/CartScreen.kt` — cart details, edit lines, promos, totals.
- `ui/checkout/CheckoutScreen.kt` — address, payment, place order, error handling.
- `ui/tracking/TrackingScreen.kt` — order status UI.
- `ui/profile/ProfileScreen.kt` — tabs for Account, Orders, Loyalty.
- `data/cart/CartStore.kt` — in-memory cart state (reactive `SnapshotStateList`).
- `data/network/ApiClient.kt` — Retrofit instances and auth header handling.
- `data/network/*Service.kt` — Retrofit services for auth, users, restaurants, menu, orders.
- `data/SessionManager.kt` — holds access token and user id (derived from JWT `sub`).

## Supabase Setup (Summary)
Ensure the following tables exist (simplified):
- `public.users(id text primary key, email text not null, wallet_balance numeric default 0, points int default 0, default_address text)`
- `public.restaurants(...)`
- `public.menu(id text primary key, restaurant_id text references restaurants(id), name text, description text, price numeric, image_url text, category text)`
- `public.orders(id uuid primary key default gen_random_uuid(), uid text references users(id), items jsonb, total numeric, address text, status text check (status in ('Confirmed','Preparing','Out for Delivery','Delivered')), created_at timestamptz default now())`

RLS example policies (dev-friendly):
- `users`: allow authenticated `select, insert, update` (on own row if you store `auth.uid()`),
- `orders`: allow authenticated `insert, select`.

## Configuration
- Internet permission in `AndroidManifest.xml`.
- Supabase keys in `BuildConfig` (e.g., `SUPABASE_URL`, `SUPABASE_ANON_KEY`).
- `ApiClient` sets `apikey` and `Authorization: Bearer <token or anon>` headers.

## Build & Run
```powershell
cd C:\Users\RC_Student_Lab\AndroidStudioProjects\LinDonnDelivery2
.\u200cgradlew assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Auth Notes
- On login/signup, we set session and upsert `public.users` with the current `id` and `email` to satisfy the FK referenced by `orders.uid`.
- We decode the user id (`sub`) from the JWT locally to avoid extra auth round-trips.

## Promos
- `SAVE10` → 10% off subtotal
- `LESS20` → R20 off subtotal (capped to not go below zero)

## Troubleshooting
- HTTP 429 (rate limit): Wait and retry; app minimizes auth calls and adds slight backoff.
- HTTP 400 on signin: invalid credentials, short password, or email confirmation required.
- HTTP 409 on order: ensure `public.users` has the current uid row; ensure `orders.items` is `jsonb`, `id` has default `gen_random_uuid()`, and policies allow insert.

## Contributing
- Use feature branches and PRs. Keep UI consistent with Material 3.

## License
MIT (add your license of choice).
