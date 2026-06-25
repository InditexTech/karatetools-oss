@inditex-oss-karate
@karate-base-report

Feature: karate-base-report-options

Background:

Given def options = karate.properties['karate.options']
Given print 'options >>' + options

Given def reportOptions = karate.properties['karate.report.options']
Given print 'reportOptions >>' + reportOptions

@report-showlog-true
Scenario: report-option-showlog-true-parsing
Then match reportOptions contains '--showLog true'
Then match reportOptions contains '--showAllStepsOption true'
Then match reportOptions !contains '--showLog false'

@report-showallsteps-false
Scenario: report-option-showallsteps-false-parsing
Then match reportOptions contains '--showLog true'
Then match reportOptions contains '--showAllStepsOption false'
Then match reportOptions !contains '--showAllStepsOption true'
