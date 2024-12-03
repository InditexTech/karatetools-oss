@functional
@op.listItems 

Feature: listItems

Background:

Scenario: listItems

# listItems-200
Given def listItemsRequest = read('test-data/listItems_200.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 200
