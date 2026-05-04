## 2024-05-04 - SimpleDateFormat caching
**Learning:** Instantiating `SimpleDateFormat` inside a Composable function (or any frequently called function like `CurrencyUtils.formatAmount`) is a significant performance bottleneck because it performs expensive resource lookups and locale handling each time.
**Action:** Cache these instances. For Composables, use `remember { SimpleDateFormat(...) }` so it's created once per composition tree instance. For utility singletons used heavily during recomposition (like `CurrencyUtils`), use a `ThreadLocal` or object-level map if thread safety is required, to reuse the expensive `NumberFormat` objects.

## 2024-05-04 - Release keystore missing locally / in CI
**Learning:** CI builds may fail when attempting `assembleRelease` if the `build.gradle.kts` configuration expects a signing key but the file isn't present in the environment (e.g. GitHub Actions without secrets injected into files).
**Action:** Wrap `storeFile` and `signingConfig` configurations within an `if (file("path/to/keystore").exists())` check.
