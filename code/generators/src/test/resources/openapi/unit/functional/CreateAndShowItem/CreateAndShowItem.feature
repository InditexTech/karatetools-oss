@functional
@op.createItems @op.showItemById 

Feature: CreateAndShowItem

Background:

Scenario: CreateAndShowItem

# createItems-201
Given def createItemsRequest = read('test-data/createItems_201.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201

# showItemById-200
Given def showItemByIdRequest = read('test-data/showItemById_200.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200
