#!/usr/bin/env bash
parse_surefire() {

  # Common Variables
  local TYPE FOLDER REPORT_FILE LABEL RESULT ICON
  # Specific Variables
  local SUREFIRE_TESTS SUREFIRE_ERRORS SUREFIRE_FAILURES SUREFIRE_SKIPPED SUREFIRE_SUCCESS_RATE SUREFIRE_TIME
  # Calculate Variables
  local SUREFIRE_FAILED SUREFIRE_PASSED

  # Define Icons
  ICON="📗"

  # Input variables
  TYPE=$1
  FOLDER=$2

  # Define File
  REPORT_FILE=$FOLDER/surefire.html
  if [[ -s "$REPORT_FILE" ]]; then

    # Extract Values
    SUREFIRE_TESTS=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[1]/text()" "$REPORT_FILE" 2>/dev/null)
    SUREFIRE_ERRORS=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[2]/text()" "$REPORT_FILE" 2>/dev/null)
    SUREFIRE_FAILURES=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[3]/text()" "$REPORT_FILE" 2>/dev/null)
    SUREFIRE_SKIPPED=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[4]/text()" "$REPORT_FILE" 2>/dev/null)
    SUREFIRE_SUCCESS_RATE=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[5]/text()" "$REPORT_FILE" 2>/dev/null)
    SUREFIRE_TIME=$(xmllint --html --xpath "//a[@id='Summary']/../table//td[6]/text()" "$REPORT_FILE" 2>/dev/null)

    # Perform Calculations
    SUREFIRE_FAILED=$(echo "$SUREFIRE_ERRORS + $SUREFIRE_FAILURES" | bc)
    SUREFIRE_PASSED=$(echo "$SUREFIRE_TESTS - $SUREFIRE_FAILED - $SUREFIRE_SKIPPED" | bc)

    # Set GitHub Step Summary
    # shellcheck disable=SC2129
    echo "| Type | #️⃣ Tests | ✔️ Passed| ❌ Failed | ⏭️ Skipped | 📊 Success Rate % | ⏱️ Time |" >> "$GITHUB_STEP_SUMMARY"
    echo "|  --- | --- | --- | --- | --- |--- |--- |" >> "$GITHUB_STEP_SUMMARY"
    echo "| $ICON $TYPE \
      | $SUREFIRE_TESTS \
      | $SUREFIRE_PASSED \
      | $SUREFIRE_FAILED \
      | $SUREFIRE_SKIPPED \
      | $SUREFIRE_SUCCESS_RATE \
      | $SUREFIRE_TIME |" >> "$GITHUB_STEP_SUMMARY"

    # Set Output Variables
    LABEL="📊 Success Rate %"
    RESULT=$(echo "$SUREFIRE_SUCCESS_RATE" | tr -d '%')

    # shellcheck disable=SC2129
    echo "result-type=$ICON $TYPE" >> "$GITHUB_OUTPUT"
    echo "result-label=$LABEL" >> "$GITHUB_OUTPUT"
    echo "result-value=$RESULT" >> "$GITHUB_OUTPUT"

    echo ">> $ICON $TYPE $LABEL [$RESULT]"

  else
    echo "Results [$TYPE] - File not found [$REPORT_FILE]"
  fi
}
