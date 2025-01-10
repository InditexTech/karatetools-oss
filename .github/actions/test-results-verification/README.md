# Test Results Verification

This action generates test summary and verifies threshold for a given set of results.

For example:

| Type     | #️⃣ Instructions | ✔️ Covered | ❌ Missed | 📊 Coverage % |
|----------|-----------------|-------------|-----------|----------------|
|📙 jacoco |8693             |7022         | 1671      |80%             |

The action will fail if the restuls is below the threshold or if it is not a number.

* 📙 jacoco: ✔️ Passed - 📊 Coverage % [96] is above or equal to threshold [90]
* 📙 jacoco: ❌ Failed - 📊 Coverage % [80] is below threshold [90]
* 📙 jacoco: ❌ Failed - 📊 Coverage % [] is not a number

## Inputs

This action accepts as parameters:

* **type**: The type of results.
  * Supported types: `surefire`, `failsafe`, `jacoco`, `karate` and `pitest`.
* **results_folder**: The folder of the results. For example:
  * `surefire/failsafe`: `code/target/reports`
  * `jacoco`:
    * `unit`: `code/jacoco-report-aggregate/target/site/jacoco-aggregate`
    * `integration`: `code/jacoco-report-aggregate/target/site/jacoco-aggregate-it`
    * `karate`: `code/target/jacoco-e2e`
  * `pitest`: `code/target/pit-reports`
  * `karate`: `e2e/karate/target/karate-reports`
* **threshold**: The threshold to verify,
  * `Coverage %` for `jacoco` and `pitest`
  * `Success rate %` for `surefire`, `failsafe` and `karate`

## Supported Test Results Verification

The supported results summary annotations are:

  * [surefire](#surefire)
  * [failsafe](#failsafe)
  * [jacoco](#jacoco)
  * [pitest](#pitest)
  * [karate](#karate)

### surefire

|Type        | #️⃣ Tests | ✔️ Passed | ⏭️ Skipped | ❌ Failed | 📊 Success Rate % | ⏱️ Time |
|------------|----------|------------|-------------|-----------|-------------------|---------|
|📗 surefire |546       |546         |0            |0          |100%               | 150.7 s |

### failsafe

|Type        | #️⃣ Tests | ✔️ Passed | ⏭️ Skipped | ❌ Failed | 📊 Success Rate % | ⏱️ Time |
|------------|----------|------------|-------------|-----------|-------------------|---------|
|📘 failsafe |50        |50          |0            |0          |100%               | 2125 s  |

### jacoco

| Type     | #️⃣ Instructions | ✔️ Covered | ❌ Missed | 📊 Coverage % |
|----------|-----------------|-------------|-----------|----------------|
|📙 jacoco |8693             |7022         | 1671      |80%             |

### pitest

| Type     | #️⃣ Classes | 📊 Mutation Coverage % | 📑 Line Coverage % | 💪 Test Strength % |
|----------|-------------|------------------------|--------------------|---------------------|
|📒 pitest |39           |94%                     |94%                 |96%                  |

### karate

| Type     | #️⃣ Features (Scenarios) | ✔️ Passed | ❌ Failed | 📊 Success Rate % | ⏱️ Time |
|----------|-------------------------|------------|-----------|------------------|---------|
|📕 karate  |14 (20)                  |14 (20)    |0 (0)      |100%               | 1200 s  |
