# AntarStore Recovery Changelog

Rebuilt from Viastastore per the AntarStore Project Recovery Audit (15 Sep 2026). Every finding ID below (F-01 etc.) refers to that audit.

## Security (P0)
- **F-01** — Removed hardcoded DB password, Gmail app password, and a **live** Razorpay key/secret from source. All four now come from environment variables (`DB_PASSWORD`, `MAIL_PASSWORD`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`). **You must rotate the exposed Razorpay live key and Gmail app password outside of this codebase** — they were sitting in plaintext in the original zip.
- **F-09** — Passwords are now hashed with BCrypt (`spring-security-crypto`, `PasswordEncoder` bean) instead of stored/compared in plaintext.
- **F-11** — Added `AdminAuthInterceptor` protecting every `/Admin/**` route centrally. Previously `UpdateUserStatus` had no admin check at all.
- **F-08** — Cart increase/decrease/remove now verify the cart row belongs to the logged-in user before mutating it (was an IDOR: any logged-in user could modify/delete another user's cart items by guessing IDs). `remove` is now POST-only since it mutates state.
- Payment verification logging no longer prints raw payment IDs/signatures to the console.

## Checkout & orders (P0)
- **F-02** — Fixed the session-key typo (`LoggedInUser` vs `loggedInUser`) that made `user` always resolve to `null` during payment verification.
- **F-03** — `Orders.orderId` no longer has a `unique=true` constraint. The old code created one `Orders` row per cart line, all sharing the same Razorpay order ID, which crashed on any multi-item checkout.
- **F-04** — Added a stock check across the *entire* cart before writing any order rows (previously stock was checked mid-loop, so a shortfall partway through left already-created orders committed even though checkout was reported as failed). Also added an idempotency guard so a retried payment callback can't create duplicate orders.
- **F-07** — Cart totals (subtotal/shipping/discount/grand total) are now computed once in `CartService.computeSummary()` and used by both the cart page and payment creation, so what the customer sees always matches what they're charged. Previously the cart template referenced `grandTotal`/`shipping`/`discount` that the controller never supplied.

## Address flow (P0)
- **F-05** — The address flow was broken in three separate ways: `GET /Address` returned a view named `Address` while the actual file was `SaveAddress.html`; the form posted to `/saveaddress`, which matched no mapping; and the save method built a `SavedAddress` object and never called `.save()` on it, so nothing was ever persisted. On top of that, the template's `th:object`/`th:field` bindings referenced the wrong model attribute and wrong Thymeleaf syntax, so the form would have failed to render at all. Replaced with a single working `GET/POST /address` pair, a corrected `address.html` template, and real persistence (updates the existing active address instead of leaving orphaned rows).
- Checkout now blocks with a clear message if the customer has no saved address, instead of the old flow which would NPE deep inside payment verification.

## Cart & routing (P0/P1)
- **F-06** — Removed the duplicate `/cart` (broken) vs `/Cart` (working) routes; there is now one canonical `GET /cart`.
- Fixed a shop category filter bug not in the original findings list: the category links built a URL like `/shop/3?category=Name`, but the controller only maps `/shop?id=`, so every category click 404'd. Fixed to `@{/shop(id=...)}`.
- **F-14** — Shop listing (both "all products" and by-category) now filters to `visibility=true` products only.

## Registration / OTP (P0/P1)
- **F-10** — Registration now actually sends the OTP email (was commented out — customers only ever saw the OTP in the server console). OTP expiry now consistently uses the stored `expiryTime` field for both initial registration and resend, instead of two different ad-hoc calculations. Added a 30-second resend throttle.
- Fixed a text bug in the OTP email where the greeting ran directly into "Welcome" with no space/punctuation ("Hello JohnWelcome to...").

## Admin (P0/P1)
- **F-12** — Product image upload validation (2–5 images) now actually stops the upload on failure; previously it set a flash message but continued regardless.
- **F-13** — Uploaded images are now named with a UUID + the original file extension. Previously `MultipartFile.getName()` was used, which returns the constant form-field name, not the file's name.
- Added file-type and size (5MB) validation on product image uploads.
- Implemented **Manage Orders** (was a placeholder page reading "This is manage orders page") with a real order list and status-update control.
- Implemented a working **Dashboard** with real counts, order-status breakdown, recent enquiries, and a 6-month orders chart — the template already expected all of this data; the controller just never supplied it. Also fixed `enq.enquiryDate`, a template reference to a field that doesn't exist on `Enquiry` (`enquiryAt`), which would have thrown once the enquiries list was ever non-empty.
- Category creation now checks for duplicates case-insensitively.
- **F-17** — Added `/MyOrders` and `/Profile` pages for customers (the nav already linked to both; neither existed).

## Currency & UI consistency
- Replaced `$` with `₹` throughout (cart, shop, product pages) to match the rest of the site and the actual currency used at checkout.

## Rebranding
- Package renamed `com.project.Viastastore` → `com.project.antarstore`; Maven artifact renamed to `antarstore`; database renamed to `antarstore_db`.
- Extracted the supplied Antar script logo, converted it to a transparent PNG, and replaced the old Viasta logo everywhere (nav, footer, favicon).
- Replaced all customer-facing "Viasta"/"VIASTA"/"Viastastore" text across templates and the OTP email with original AntarStore copy.
- Added `antar-theme.css`: a gold-on-charcoal accent palette (matching the tone already used on the existing cart page), Playfair Display + Inter typography, and a fix for the logo being squeezed into a circular avatar crop that cut off the script tails.

## Intentionally not done in this pass
- Blog articles / "Read more" links: no article backend exists, so these are left as static placeholder content rather than fabricating fake pages.
- Shipping/Returns/FAQ footer links point at the Contact page rather than dedicated policy pages, since no such policies exist yet to publish.
- Money fields remain `double` rather than migrating to `BigDecimal` — a larger, cross-cutting change the original audit itself files under future/P2 hardening rather than a P0 bug.
- Full BigDecimal currency migration, deeper layered-architecture refactor, and automated test coverage (audit's Phase 5+ items) — flagged as next steps, not attempted here.
