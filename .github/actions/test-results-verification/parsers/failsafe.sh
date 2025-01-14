#!/usr/bin/env bash
parse_failsafe() {

  # Common Variables
  local TYPE FOLDER REPORT_FILE LABEL RESULT ICON
  # Specific Variables
  local FAILSAFE_TESTS FAILSAFE_ERRORS FAILSAFE_FAILURES FAILSAFE_SKIPPED FAILSAFE_SUCCESS_RATE FAILSAFE_TIME
  # Calculate Variables
  local FAILSAFE_FAILED FAILSAFE_PASSED

  # Define Icons
  ICON="📘"

  # Input variables
  TYPE=$1
  FOLDER=$2

  # Define File
  REPORT_FILE=$FOLDER/failsafe.html
  if [[ -s "$REPORT_FILE" ]]; then

    # Extract Values
    FAILSAFE_TESTS=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[1]/text()" "$REPORT_FILE" 2>/dev/null)
    FAILSAFE_ERRORS=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[2]/text()" "$REPORT_FILE" 2>/dev/null)
    FAILSAFE_FAILURES=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[3]/text()" "$REPORT_FILE" 2>/dev/null)
    FAILSAFE_SKIPPED=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[4]/text()" "$REPORT_FILE" 2>/dev/null)
    FAILSAFE_SUCCESS_RATE=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[5]/text()" "$REPORT_FILE" 2>/dev/null)
    FAILSAFE_TIME=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[6]/text()" "$REPORT_FILE" 2>/dev/null)

    # Perform Calculations
    FAILSAFE_FAILED=$(echo "$FAILSAFE_ERRORS + $FAILSAFE_FAILURES" | bc)
    FAILSAFE_PASSED=$(echo "$FAILSAFE_TESTS - $FAILSAFE_FAILED - $FAILSAFE_SKIPPED" | bc)

    # Set GitHub Step Summary
    # shellcheck disable=SC2129
    echo "| Type | #️⃣ Tests | ✔️ Passed| ❌ Failed | ⏭️ Skipped | 📊 Success Rate % | ⏱️ Time |" >> "$GITHUB_STEP_SUMMARY"
    echo "|  --- | --- | --- | --- | --- |--- |--- |" >> "$GITHUB_STEP_SUMMARY"
    echo "| $ICON $TYPE \
      | $FAILSAFE_TESTS \
      | $FAILSAFE_PASSED \
      | $FAILSAFE_FAILED \
      | $FAILSAFE_SKIPPED \
      | $FAILSAFE_SUCCESS_RATE \
      | $FAILSAFE_TIME |" >> "$GITHUB_STEP_SUMMARY"

    # Set Output Variables
    LABEL="📊 Success Rate %"
    RESULT=$(echo "$FAILSAFE_SUCCESS_RATE" | tr -d '%')

    # shellcheck disable=SC2129
    echo "result-type=$ICON $TYPE" >> "$GITHUB_OUTPUT"
    echo "result-label=$LABEL" >> "$GITHUB_OUTPUT"
    echo "result-value=$RESULT" >> "$GITHUB_OUTPUT"

    echo ">> $ICON $TYPE $LABEL [$RESULT]"

  else
    echo "Results [$TYPE] - File not found [$REPORT_FILE]"
  fi
}
