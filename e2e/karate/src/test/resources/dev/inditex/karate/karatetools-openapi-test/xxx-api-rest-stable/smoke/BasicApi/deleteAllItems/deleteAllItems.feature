@smoke
@op.deleteAllItems

Feature: deleteAllItems Smoke Tests 

Background:

Scenario Outline: deleteAllItems <status>
* def req = call utils.readTestData <testDataFile>
* def result = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/deleteAllItems/deleteAllItems.feature') req
* match result.responseStatus == <status>
Examples:
| status  | testDataFile |
| 204     | 'test-data/deleteAllItems_204.yml' |
| 401     | 'test-data/deleteAllItems_401.yml' |
