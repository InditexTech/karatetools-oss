@functional
@op.listItems @op.showItemById @op.createItems @op.deleteAllItems
@ignore

Feature: reset

Background:

Scenario: reset

Given def itemId = 1

# deleteAllItems-204
Given def deleteAllItemsRequest = read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/test-data/deleteAllItems_204.yml')
When def deleteAllItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/deleteAllItems/deleteAllItems.feature') deleteAllItemsRequest
Then match deleteAllItemsResponse.responseStatus == 204

# createItems-201
Given def createItemsRequest = read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/test-data/createItems_201.yml')
Given createItemsRequest.body.id = itemId
Given createItemsRequest.body.name = 'Item' + itemId
Given createItemsRequest.body.tag = 'Tag' + itemId
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201
Then def response = createItemsResponse.response
Then match response.id == createItemsRequest.body.id
Then match response.name == createItemsRequest.body.name
Then match response.tag == createItemsRequest.body.tag

# listItems-200
Given def listItemsRequest = read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/test-data/listItems_200.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 200
Then def response = listItemsResponse.response
Then match response == '#[1]'
## Created Item in previous step
Then match response[0].id == createItemsRequest.body.id
Then match response[0].name == createItemsRequest.body.name
Then match response[0].tag == createItemsRequest.body.tag

# showItemById-200
Given def showItemByIdRequest = read('classpath:dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/common/reset/test-data/showItemById_200.yml')
Given showItemByIdRequest.params.itemId = itemId
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200
Then def response = showItemByIdResponse.response
## Created Item in previous step
Then match response.id == createItemsRequest.body.id
Then match response.name == createItemsRequest.body.name
Then match response.tag == createItemsRequest.body.tag
