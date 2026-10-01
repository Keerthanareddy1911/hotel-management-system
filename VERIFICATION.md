# Verification record

Executed on 1 October 2026 with OpenJDK 17.

## Passed

- Compiled every Java main and test source with `java Build.java`.
- Passed all 14 Java checks with `java -cp target/classes hotel.Tests`.
- Validated JavaScript syntax with `node --check src/main/resources/public/app.js`.
- HTTP smoke tests passed for state loading, static resources, booking creation, overlapping booking rejection, invalid and valid state changes, checkout totals, and two concurrent requests competing for one room.
- A guest name containing angle brackets and an ampersand round-tripped correctly through JSON. The frontend escapes dynamic text before rendering.

Run the repeatable HTTP checks from the project root with `python3 scripts/smoke_test.py`. It compiles the app, runs the Java tests, starts a temporary demo server on port 8080, tests the endpoints, and stops the server. Requires Python 3 and JDK 17+; stop any existing server on that port first. Test data stays in memory.

## Not verified in this environment

- MySQL execution, persistence after restart, JDBC driver download, and concurrent transactions against a real MySQL server. MySQL and Maven were unavailable.
- Browser visual layout, click workflows, and PDF printing. The Playwright library was present but its browser executable was unavailable. The UI includes responsive CSS; manual desktop/mobile verification is still required.

## MySQL acceptance checklist

1. Import schema, configure the application account, and launch persistent mode.
2. Create a booking, restart the server, and confirm the booking remains.
3. Submit two overlapping reservations for the same room concurrently; expect one successful write and one rejection.
4. Confirm adjacent dates work and cancellation releases availability.
5. Check in, check out with 500.25 in extras, and verify the generated invoice against stored rate and tax snapshots.
6. Repeat a terminal status change and verify rejection with unchanged database data.
7. Verify the application account has only SELECT, INSERT and UPDATE access to this database.

The SQL locking strategy is implemented, but the passing in-memory concurrency test does not independently prove MySQL behavior.
