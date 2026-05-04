Title: "🧪 Add test for deleteAllTransactions and fix dashboard unit tests"

Description:
🎯 **What:** The testing gap addressed: Added tests for `deleteAllTransactions` in `TransactionRepositoryImplTest.kt` to ensure this repository method correctly delegates to the DAO. Also updated broken unit tests in `DashboardViewModelTest.kt` related to required `UserPreferencesRepository`, `BudgetRepository`, and `GoalRepository` dependencies that were not being provided.
📊 **Coverage:** The test coverage for `TransactionRepositoryImpl` is now increased with `deleteAllTransactions` functionality verified. The `DashboardViewModel` unit test initializations are now using appropriate dummy mocks.
✨ **Result:** Improved test coverage and fixed the previously failing unit tests.
