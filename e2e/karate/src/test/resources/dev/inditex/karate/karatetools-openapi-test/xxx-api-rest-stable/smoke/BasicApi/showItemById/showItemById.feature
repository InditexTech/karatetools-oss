@smoke
@op.showItemById

Feature: showItemById Smoke Tests

Background:

* callonce read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/reset.feature')

Scenario Outline: showItemById <status>
* def req = call utils.readTestData <testDataFile>
* def result = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') req
* match result.responseStatus == <status>
Examples:
| status  | testDataFile |
| 200     | 'test-data/showItemById_200.yml' |
| 404     | 'test-data/showItemById_404.yml' |
| 401     | 'test-data/showItemById_401.yml' |
