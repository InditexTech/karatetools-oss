@karate-base

Feature: karate-base

Background:

* print '---- ScenarioEngine.getAllVariablesAsMap()                ----'
* def variables = karate.getEngine().getAllVariablesAsMap()
* print variables
* print '--------------------------------------------------------------'
* print '---- ScenarioEngine.getConfig()                           ----'
* def config = karate.getEngine().getConfig()
* print karate.toJson(config)
* print '--------------------------------------------------------------'

@karate-base
@env=local
Scenario: karate-base-simple

Given def req =
"""
{
  "operationId": "createItems",
  "statusCode": 201,
  "params": null,
  "body": {
    "id": 1,
    "name": "Item1",
    "tag": "Tag1"
  },
  "matchResponse": true,
  "responseMatches": "#(read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/schema/createItems_201.schema.yml'))"
}
"""

When def res = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') req
Then match res.responseStatus == 201
