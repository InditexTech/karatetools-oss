@smoke
@op.createItems

Feature: createItems Smoke Tests 

Background:

Scenario Outline: createItems <status>
* def req = call utils.readTestData <testDataFile>
* def result = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') req
* match result.responseStatus == <status>
Examples:
| status  | testDataFile |
| 201     | 'test-data/createItems_201.yml' |
| 400     | 'test-data/createItems_400.yml' |
| default | 'test-data/createItems_default.yml' |
