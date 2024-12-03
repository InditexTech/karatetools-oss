#!/usr/bin/env bash
parse_pitest() {

  # Common Variables
  local TYPE FOLDER REPORT_FILE LABEL RESULT ICON
  # Specific Variables
  local PITEST_NUMBER_CLASSES PITEST_LINE_COVERAGE PITEST_MUTATION_COVERAGE PITEST_TEST_STRENGTH
  # Calculate Variables
  # NA

  # Define Icons
  ICON="📒"

  # Input variables
  TYPE=$1
  FOLDER=$2

  # Define File
  REPORT_FILE=$FOLDER/index.html
  if [[ -s "$REPORT_FILE" ]]; then

    # Extract Values
    PITEST_NUMBER_CLASSES=$(xmllint --html --xpath "//table[1]/tbody/tr/td[1]/text()" "$REPORT_FILE" 2>/dev/null)
    PITEST_LINE_COVERAGE=$(xmllint --html --xpath "//table[1]/tbody/tr/td[2]/text()" "$REPORT_FILE" 2>/dev/null)
    PITEST_MUTATION_COVERAGE=$(xmllint --html --xpath "//table[1]/tbody/tr/td[3]/text()" "$REPORT_FILE" 2>/dev/null)
    PITEST_TEST_STRENGTH=$(xmllint --html --xpath "//table[1]/tbody/tr/td[4]/text()" "$REPORT_FILE" 2>/dev/null)

    # Perform Calculations
    # No calculations needed

    # Set GitHub Step Summary
    # shellcheck disable=SC2129
    echo "| Type | #️⃣ Classes | 📊 Mutation Coverage % | 📑 Line Coverage % | 💪 Test Strength % |" >> "$GITHUB_STEP_SUMMARY"
    echo "|  --- | --- | --- | --- | --- |" >> "$GITHUB_STEP_SUMMARY"
    echo "| $ICON $TYPE \
      | $PITEST_NUMBER_CLASSES \
      | $PITEST_MUTATION_COVERAGE \
      | $PITEST_LINE_COVERAGE \
      | $PITEST_TEST_STRENGTH |" >> "$GITHUB_STEP_SUMMARY"

    # Set Output Variables
    LABEL="📊 Mutation Coverage %"
    RESULT=$(echo "$PITEST_MUTATION_COVERAGE" | tr -d '%')

    # shellcheck disable=SC2129
    echo "result-type=$ICON $TYPE" >> "$GITHUB_OUTPUT"
    echo "result-label=$LABEL" >> "$GITHUB_OUTPUT"
    echo "result-value=$RESULT" >> "$GITHUB_OUTPUT"

    echo ">> $ICON $TYPE $LABEL [$RESULT]"

  else
    echo "Results [$TYPE] - File not found [$REPORT_FILE]"
  fi
}
