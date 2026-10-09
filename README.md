# Haven — Hotel Management System

A runnable academic full-stack project using **Core Java 17, OOP, JDBC, MySQL, HTML, CSS and JavaScript**. The web interface is served by Java's built-in HTTP server. No frontend build tool is needed.

## Features

- Dashboard: checked-in occupancy, expected arrivals, and completed-stay billing.
- Six seeded rooms with categories, nightly rates and guest capacity.
- Date-based availability, room selection, reservation creation and searchable guest records.
- Check-in, cancellation, checkout with additional charges.
- Itemized invoices, exact decimal arithmetic and browser Print / Save as PDF.
- MySQL persistence with prepared statements and transactional room locking.
- Optional memory demo; demo data disappears when the server stops.

## Requirements

Install **JDK 17 or newer**. For persistent mode, also install MySQL 8.0+ and Maven 3.8+ (or provide a compatible MySQL Connector/J JAR manually). Run all commands from this project's root directory.

## Quick demo — no database required

```sh
java Build.java
java -cp target/classes hotel.Tests
java -cp target/classes hotel.Main --demo
```

Open **http://127.0.0.1:8080**. Stop with Ctrl+C. `Build.java` invokes the JDK compiler and copies the static interface into the classpath. The included tests are executable Java checks and require no testing framework.

## MySQL setup

1. Import the schema with an administrator account:

```sh
mysql -u root -p < database/schema.sql
```

2. In MySQL, create a local application account using your own password:

```sql
CREATE USER 'hotel_app'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT SELECT, INSERT, UPDATE ON hotel_management.* TO 'hotel_app'@'localhost';
```

3. Compile and fetch the JDBC driver:

```sh
mvn package dependency:copy-dependencies
```

4. Configure and start (macOS / Linux):

```sh
export DB_URL='jdbc:mysql://localhost:3306/hotel_management'
export DB_USER='hotel_app'
export DB_PASSWORD='your-password'
export TAX_PERCENT='0'
java -cp 'target/classes:target/dependency/*' hotel.Main
```

Windows PowerShell:

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/hotel_management'
$env:DB_USER='hotel_app'
$env:DB_PASSWORD='your-password'
$env:TAX_PERCENT='0'
java -cp 'target/classes;target/dependency/*' hotel.Main
```

Set `PORT` to change the port. Tax is an illustrative, configurable percentage; the default is zero. The project does not determine real tax obligations. Rate and tax snapshots preserve historical invoices if configuration changes. Restart the application after changing the room inventory directly in MySQL because static room metadata is cached.

## Demo walkthrough

1. Select arrival and departure dates in Room inventory.
2. Book room 101, enter guest details, and confirm the reservation.
3. Attempt another reservation for the same room and overlapping dates: the server rejects it.
4. Check in the first reservation, then check out with an extra charge.
5. Open the invoice and choose Print / Save PDF.
6. Search by guest, email, room number, or booking ID.
7. In MySQL mode, restart the app and verify reservations persist.

Checkout bills the originally reserved nights; early departure, extensions, refunds and payment settlement are future enhancements. The academic workflow permits manual check-in/check-out independent of the actual stay date; occupancy counts current checked-in status.

## Architecture and OOP

```text
Browser -> Java HTTP API -> Service -> Repository interface
                                      |-- MemoryRepository (demo)
                                      |-- JdbcRepository -> MySQL
```

- `Models`: immutable room and booking records, status enum, overlap logic, billing methods.
- `Service`: validates inputs, room capacity, stay duration and lifecycle rules.
- `Repository`: abstracts storage; concrete repositories provide interchangeable implementations.
- `JdbcRepository`: prepared SQL, generated booking IDs, commit/rollback and deterministic lock order.
- `Main`: same-origin REST-like endpoints and static resource serving.
- `Json`: response serialization; user input is HTML-escaped in the browser.

States: `RESERVED -> CHECKED_IN -> CHECKED_OUT`, or `RESERVED -> CANCELLED`. Terminal bookings cannot change state.

## Data structures and query design

A `HashMap<Integer, Room>` caches immutable room metadata for expected O(1) ID lookup. Demo booking IDs use a `LinkedHashMap<Long, Booking>` for expected O(1) lookup with stable iteration. SQL indexes support room/status/date filtering. The composite index narrows overlap candidates; not all range predicates necessarily use the entire index. Reservations are sorted by ID for display. Frontend searching currently scans loaded bookings in O(n); demo availability scans booking intervals in O(n).

No measured query latency improvement is claimed. Use MySQL `EXPLAIN` and realistic benchmarks before adding performance claims to a resume. Pagination and database-backed guest search are appropriate next steps for larger data sets.

## Transactions and integrity

Creating a reservation starts a transaction, locks its room row (`FOR UPDATE`), checks active date overlaps with a locking read, inserts the booking, and commits. Competing requests for the same room serialize on the same row. Status changes lock the room first and then the booking, matching the creation lock order. Errors roll back. All writes must use this application protocol; direct SQL writes can bypass date-overlap checks. Departures are exclusive: a new guest may arrive on the previous booking's departure date.

Money uses `BigDecimal`. Tax rounds to two decimal places with HALF_UP. Extras allow at most two decimals. Invoices are derived from stored booking values rather than a separate invoice table. Completed-stay billing does not mean money has been collected.

## API

| Method | Endpoint | Fields / result |
|---|---|---|
| GET | `/api/state` | Rooms, reservations, tax config, mode |
| POST | `/api/book` | `roomId`, `guest`, `email`, `guests`, `arrival`, `departure` |
| POST | `/api/action` | `id`, `status`, optional `extras` |

POST bodies are `application/x-www-form-urlencoded`. Dates use YYYY-MM-DD. Successful writes return the booking ID. Errors return JSON and HTTP 400 or 500. Unknown routes/methods return 405.

## Validation performed

The included tests cover overlap prevention, adjacent stays, invalid capacity/date/email, legal transitions, cancellation and rebooking, decimal invoice calculation, concurrent booking attempts in memory mode, and JSON escaping. See `VERIFICATION.md` for actual execution results and MySQL checks still required.

## Scope and deployment

This is a single-hotel academic front-desk portal. It binds to loopback only. It has no staff authentication, authorization, payment gateway or production deployment configuration. Add those before exposing it on a network. It uses a bounded HTTP thread pool and a new JDBC connection per repository call; a connection pool and pagination should be added for production-scale workloads.

## Project structure

- `src/main/java/hotel/` — domain, service, repository, HTTP API.
- `src/main/resources/public/` — interactive web interface.
- `src/test/java/hotel/Tests.java` — executable business tests.
- `database/schema.sql` — MySQL schema and seed rooms.
- `Build.java` — dependency-free JDK build for demo.
- `pom.xml` — Maven compilation and MySQL driver dependency.

Last updated: 2026-10-09 06:12:57 UTC
