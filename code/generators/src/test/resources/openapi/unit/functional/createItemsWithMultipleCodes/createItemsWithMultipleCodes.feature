@functional
@op.createItems 

Feature: createItemsWithMultipleCodes

Background:

Scenario: createItemsWithMultipleCodes

# createItems-201
Given def createItemsRequest = read('test-data/createItems_201.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201

# createItems-400
Given def createItemsRequest = read('test-data/createItems_400.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 400
