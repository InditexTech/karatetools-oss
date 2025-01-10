@smoke
@op.createItems

Feature: createItems Smoke Tests

Background:

* callonce read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/reset.feature')

Scenario Outline: createItems <status>
* def req = call utils.readTestData <testDataFile>
* def result = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req
* match result.responseStatus == <status>
Examples:
| status  | testDataFile |
| 201     | 'test-data/createItems_201.yml' |
| 400     | 'test-data/createItems_400.yml' |
| 401     | 'test-data/createItems_401.yml' |
