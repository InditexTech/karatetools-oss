#!/usr/bin/env bash
parse_karate() {

  # Common Variables
  local TYPE FOLDER REPORT_FILE LABEL RESULT ICON
  # Specific Variables
  local KARATE_FEATURES_PASSED KARATE_FEATURES_FAILED KARATE_FEATURES_SKIPPED KARATE_SCENARIOS_PASSED KARATE_SCENARIOS_FAILED KARATE_ELAPSED_TIME
  # Calculate Variables
  local KARATE_FEATURES_TOTAL KARATE_SCENARIOS_TOTAL KARATE_SUCCESS_RATE

  # Define Icons
  ICON="📕"

  # Input variables
  TYPE=$1
  FOLDER=$2

  # Define File
  REPORT_FILE=$FOLDER/karate-summary-json.txt
  if [[ -s "$REPORT_FILE" ]]; then

    # Extract Values
    KARATE_FEATURES_PASSED=$(jq '.featuresPassed' "$REPORT_FILE" 2>/dev/null)
    KARATE_FEATURES_FAILED=$(jq '.featuresFailed' "$REPORT_FILE" 2>/dev/null)
    KARATE_FEATURES_SKIPPED=$(jq '.featuresSkipped' "$REPORT_FILE" 2>/dev/null)
    KARATE_SCENARIOS_PASSED=$(jq '.scenariosPassed' "$REPORT_FILE" 2>/dev/null)
    KARATE_SCENARIOS_FAILED=$(jq '.scenariosfailed' "$REPORT_FILE" 2>/dev/null)
    KARATE_ELAPSED_TIME=$(jq '.elapsedTime' "$REPORT_FILE" 2>/dev/null)

    # Perform Calculations
    KARATE_FEATURES_TOTAL=$(echo "$KARATE_FEATURES_PASSED + $KARATE_FEATURES_FAILED + $KARATE_FEATURES_SKIPPED" | bc)
    KARATE_SCENARIOS_TOTAL=$(echo "$KARATE_SCENARIOS_PASSED + $KARATE_SCENARIOS_FAILED" | bc)
    KARATE_SUCCESS_RATE=0
    if [[ $KARATE_SCENARIOS_TOTAL -gt 0 ]]; then
      KARATE_SUCCESS_RATE=$(echo "scale=2; $KARATE_SCENARIOS_PASSED / $KARATE_SCENARIOS_TOTAL * 100" | bc)
    fi

    # Set GitHub Step Summary
    # shellcheck disable=SC2129
    echo "| Type | #️⃣ Features (Scenarios) | ✔️ Passed| ❌ Failed | 📊 Success Rate % | ⏱️ Time |" >> "$GITHUB_STEP_SUMMARY"
    echo "| --- | --- | --- | --- | --- | --- |" >> "$GITHUB_STEP_SUMMARY"
    echo "| $ICON $TYPE \
      | $KARATE_FEATURES_TOTAL ($KARATE_SCENARIOS_TOTAL) \
      | $KARATE_FEATURES_PASSED ($KARATE_SCENARIOS_PASSED) \
      | $KARATE_FEATURES_FAILED ($KARATE_SCENARIOS_FAILED) \
      | $KARATE_SUCCESS_RATE% \
      | $KARATE_ELAPSED_TIME |" >> "$GITHUB_STEP_SUMMARY"

    # Set Output Variables
    LABEL="📊 Success Rate %"
    RESULT=$(echo "$KARATE_SUCCESS_RATE" | tr -d '%')

    # shellcheck disable=SC2129
    echo "result-type=$ICON $TYPE" >> "$GITHUB_OUTPUT"
    echo "result-label=$LABEL" >> "$GITHUB_OUTPUT"
    echo "result-value=$RESULT" >> "$GITHUB_OUTPUT"

    echo ">> $ICON $TYPE $LABEL [$RESULT]"

  else
    echo "Results [$TYPE] - File not found [$REPORT_FILE]"
  fi
}
