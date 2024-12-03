#!/usr/bin/env bash
parse_jacoco() {

  # Common Variables
  local TYPE FOLDER REPORT_FILE LABEL RESULT ICON
  # Specific Variables
  local JACOCO_MISSED_INSTRUCTIONS JACOCO_INSTRUCTIONS_COVERAGE
  # Calculate Variables
  local JACOCO_MISSED_INSTRUCTIONS_ARRAY JACOCO_INSTRUCTIONS_TOTAL JACOCO_INSTRUCTIONS_COVERED JACOCO_INSTRUCTIONS_MISSED

  # Define Icons
  ICON="📙"

  # Input variables
  TYPE=$1
  FOLDER=$2

  # Define File
  REPORT_FILE=$FOLDER/index.html
  if [[ -s "$REPORT_FILE" ]]; then

    # Extract Values
    JACOCO_MISSED_INSTRUCTIONS=$(xmllint --html --xpath "//table[@id='coveragetable']/tfoot/tr/td[2]/text()" "$REPORT_FILE" 2>/dev/null)
    JACOCO_INSTRUCTIONS_COVERAGE=$(xmllint --html --xpath "//table[@id='coveragetable']/tfoot/tr/td[3]/text()" "$REPORT_FILE" 2>/dev/null)

    # Perform Calculations
    # shellcheck disable=SC2206
    JACOCO_MISSED_INSTRUCTIONS_ARRAY=($JACOCO_MISSED_INSTRUCTIONS)
    JACOCO_INSTRUCTIONS_MISSED=$(echo "${JACOCO_MISSED_INSTRUCTIONS_ARRAY[0]}" | tr -d ',')
    JACOCO_INSTRUCTIONS_TOTAL=$(echo "${JACOCO_MISSED_INSTRUCTIONS_ARRAY[2]}" | tr -d ',')
    JACOCO_INSTRUCTIONS_COVERED=$(echo "$JACOCO_INSTRUCTIONS_TOTAL - $JACOCO_INSTRUCTIONS_MISSED" | bc)

    # Set GitHub Step Summary
    # shellcheck disable=SC2129
    echo "| Type | #️⃣ Instructions | ✔️ Covered | ❌ Missed | 📊 Coverage % |" >> "$GITHUB_STEP_SUMMARY"
    echo "|  --- | --- | --- | --- | --- |" >> "$GITHUB_STEP_SUMMARY"
    echo "| $ICON $TYPE \
      | $JACOCO_INSTRUCTIONS_TOTAL \
      | $JACOCO_INSTRUCTIONS_COVERED \
      | $JACOCO_INSTRUCTIONS_MISSED \
      | $JACOCO_INSTRUCTIONS_COVERAGE |" >> "$GITHUB_STEP_SUMMARY"

    # Set Output Variables
    LABEL="📊 Coverage %"
    RESULT=$(echo "$JACOCO_INSTRUCTIONS_COVERAGE" | tr -d '%')

    # shellcheck disable=SC2129
    echo "result-type=$ICON $TYPE" >> "$GITHUB_OUTPUT"
    echo "result-label=$LABEL" >> "$GITHUB_OUTPUT"
    echo "result-value=$RESULT" >> "$GITHUB_OUTPUT"

    echo ">> $ICON $TYPE $LABEL [$RESULT]"

  else
    echo "Results [$TYPE] - File not found [$REPORT_FILE]"
  fi
}
