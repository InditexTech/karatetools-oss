@smoke
@op.listItems

Feature: listItems Smoke Tests 

Background:

Scenario Outline: listItems <status>
* def req = call utils.readTestData <testDataFile>
* def result = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature') req
* match result.responseStatus == <status>
Examples:
| status  | testDataFile |
| 200     | 'test-data/listItems_200.yml' |
| 400     | 'test-data/listItems_400.yml' |
| default | 'test-data/listItems_default.yml' |
