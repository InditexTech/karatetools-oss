@functional
@op.listItems 

Feature: listItemsWithMultipleCodes

Background:

Scenario: listItemsWithMultipleCodes

# listItems-200
Given def listItemsRequest = read('test-data/listItems_200.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 200

# listItems-400
Given def listItemsRequest = read('test-data/listItems_400.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 400
