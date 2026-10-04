# AntarStore

A Spring Boot + Thymeleaf + MySQL e-commerce platform, rebuilt and hardened from the original Viastastore codebase per the AntarStore Project Recovery Audit (15 Sep 2026). See `CHANGELOG.md` for the full list of bugs fixed.

## Tech stack
- Java 25, Spring Boot 4.1.0
- Spring MVC + Thymeleaf (server-rendered)
- Spring Data JPA + MySQL
- Razorpay (payments)
- BCrypt password hashing (`spring-security-crypto`)

## Setup

1. **Create a MySQL database.** The app will auto-create `antarstore_db` on first run (`createDatabaseIfNotExist=true`), so you only need a running MySQL server and a user with permission to create databases.

2. **Set environment variables.** Copy `.env.example` to `.env` (or export the same variables in your shell / IDE run configuration) and fill in real values:
   - `DB_USERNAME`, `DB_PASSWORD`
   - `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail app password, not your regular password)
   - `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` (use **test** keys — `rzp_test_...` — while developing)

   Never commit a real `.env` file. `.gitignore` already excludes it.

3. **Run it:**
   ```
   ./mvnw spring-boot:run
   ```
   The app starts on `http://localhost:8383`.

4. **Create an admin user.** There's no seeded admin account. After registering a normal account and verifying it, promote it to admin directly in the database:
   ```sql
   UPDATE users SET role = 'Admin', status = 'Verified' WHERE email = 'you@example.com';
   ```
   Log in again afterward — admin users are routed to `/Admin/Dashboard`.

## Customer flow
Home → Shop → Product → Add to Cart → Cart → Address → Razorpay (test mode) → Order confirmation → My Orders

## Admin flow
Login → Dashboard → Manage Users / Categories / Products / Orders / Enquiries (all routes under `/Admin/**` require an admin session)

## Known intentional limitations
These were out of scope for this recovery pass (see `CHANGELOG.md` for reasoning):
- Blog is static placeholder content; "Read more" links are inert since there's no article backend.
- Shipping/Returns/FAQ footer links point to the Contact page rather than dedicated policy pages, since none exist yet — write real policies before launch and give them their own pages.
- Money fields are still `double`, not `BigDecimal`. Fine for a portfolio/demo; before handling real transactions at scale, migrate pricing fields to `BigDecimal` with a defined rounding rule.
