@functional
@op.createItems @op.showItemById @op.listItems 

Feature: CreateShowAndListItems

Background:

Scenario: CreateShowAndListItems

# createItems-201
Given def createItemsRequest = read('test-data/createItems_201.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201

# showItemById-200
Given def showItemByIdRequest = read('test-data/showItemById_200.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200

# listItems-200
Given def listItemsRequest = read('test-data/listItems_200.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 200
