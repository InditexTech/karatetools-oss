@inditex-oss-karate
@generate-reports
@generate-reports-failure

Feature: reports-example-2
Scenario: reports-scenario-2-1
* print 'karate.info is: ', karate.info
* karate.fail(karate.info)

Scenario Outline: reports-scenario-2-2 <example>
* print 'karate.info is: ', karate.info
Examples:
  | example |
  | 2-2-1   |
  | 2-2-2   |
