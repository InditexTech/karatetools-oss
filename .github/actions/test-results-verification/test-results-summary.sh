#!/usr/bin/env bash

# Type of results
INPUT_RESULTS_TYPE=""
# Results Path to process
INPUT_RESULTS_FOLDER=""

# Base Directory for the parsers
BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Function: Usage
usage() {
  echo "Usage: $0 [ -t RESULTS_TYPE ][ -r RESULTS_FOLDER ]" 1>&2
}

# Function: Exit with error.
exit_error() {
  usage
  exit 1
}

# Function: Parse Results
parse_results() {

  # Install xmllint
  sudo apt-get update -qq > /dev/null
  sudo apt-get -y -qq install libxml2-utils > /dev/null

  local RESULTS_TYPE RESULTS_FOLDER PARSER COMMAND

  RESULTS_TYPE=$1
  RESULTS_FOLDER=$2

  echo "parse_results type=[$RESULTS_TYPE]folder=[$RESULTS_FOLDER]"

  if [[ -n "$RESULTS_TYPE" && "$RESULTS_TYPE" != "null" ]]; then
    PARSER="$BASEDIR/parsers/$RESULTS_TYPE.sh"
    if [[ -s "$PARSER" ]]; then
      # Load parser
      # shellcheck disable=SC1090
      source "$PARSER"
      # Define command, for example parse_surefire surefire code/target/reports
      COMMAND="parse_$RESULTS_TYPE $RESULTS_TYPE $RESULTS_FOLDER"
      # Execute command
      echo "Executing       [$COMMAND]"
      eval "$COMMAND"
    else
      echo "Unsupported Type [$RESULTS_TYPE]"
    fi
  else
    echo "Ignoring        type=[$RESULTS_TYPE]folder=[$RESULTS_FOLDER]"
  fi
}

# Process Options
# -t $INPUT_RESULTS_TYPE    The type of results. For example: 'jacoco', 'karate', 'pitest', 'surefire', 'failsafe', ...
# -r $INPUT_RESULTS_FOLDER  The path of the results. For example: code/jacoco-report-aggregate/target/site/jacoco-aggregate.
while getopts ":t:r:" options; do
  case "${options}" in
    t)
      INPUT_RESULTS_TYPE=${OPTARG}
      ;;
    r)
      INPUT_RESULTS_FOLDER=${OPTARG}
      ;;
    *)
      exit_error
      ;;
  esac
done

if [[ -z "$INPUT_RESULTS_FOLDER" && -z "$INPUT_RESULTS_TYPE" ]]; then
  echo "Error: Invalid arguments provided."
  exit_error
else
  parse_results "$INPUT_RESULTS_TYPE" "$INPUT_RESULTS_FOLDER"
fi
