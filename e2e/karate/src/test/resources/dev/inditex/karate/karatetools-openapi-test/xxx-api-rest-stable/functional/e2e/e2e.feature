@functional
@op.listItems @op.showItemById @op.createItems

Feature: e2e

Background:

Scenario: e2e - Success

# createItems-201
Given def createItemsRequest = read('test-data/createItems_201.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201
Then def response = createItemsResponse.response
Then match response.id == createItemsRequest.body.id
Then match response.name == createItemsRequest.body.name
Then match response.tag == createItemsRequest.body.tag
Then match response.id == 10
Then match response.name == 'Item10'
Then match response.tag == 'Tag10'

# listItems-200
Given def listItemsRequest = read('test-data/listItems_200.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 200
Then def response = listItemsResponse.response
Then match response == '#[4]'
## Pre-Created Items
Then match response[0].id == 1
Then match response[0].name == 'Item1'
Then match response[0].tag == 'Tag1'
Then match response[1].id == 2
Then match response[1].name == 'Item2'
Then match response[1].tag == 'Tag2'
Then match response[2].id == 3
Then match response[2].name == 'Item3'
Then match response[2].tag == 'Tag3'
## Created Item in previous steps
Then match response[3].id == 10
Then match response[3].name == 'Item10'
Then match response[3].tag == 'Tag10'

# showItemById-200
Given def showItemByIdRequest = read('test-data/showItemById_200.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200
Then def response = showItemByIdResponse.response
## Created Item in previous steps
Then match response.id == 10
Then match response.name == 'Item10'
Then match response.tag == 'Tag10'

Scenario: e2e - createItems - 401 Error with JwT

# createItems-401
Given def createItemsRequest = read('test-data/createItems_401.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 401
Then def response = createItemsResponse.response
Then match response.code == 401
Then match response.message == 'Unauthorized'
Then match response.stack == 'Unauthorized'

Scenario: e2e - listItems - 401 Error with JwT

# listItems-401
Given def listItemsRequest = read('test-data/listItems_401.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 401
Then def response = listItemsResponse.response
Then match response.code == 401
Then match response.message == 'Unauthorized'
Then match response.stack == 'Unauthorized'

Scenario: e2e - showItemById - 401 Error

# showItemById-401
Given def showItemByIdRequest = read('test-data/showItemById_401.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 401
Then def response = showItemByIdResponse.response
Then match response.code == 401
Then match response.message == 'Unauthorized'
Then match response.stack == 'Unauthorized'

Scenario: e2e - createItems - 400 Error

# createItems-400
Given def createItemsRequest = read('test-data/createItems_400.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 400
Then def response = createItemsResponse.response
Then match response.code == 400
Then match response.message == 'Bad Request'
Then match response.stack contains "Field error in object 'itemDTO' on field 'name'"
Then match response.stack contains 'must not be null'

Scenario: e2e - listItems - 400 Error

# listItems-400
Given def listItemsRequest = read('test-data/listItems_400.yml')
When def listItemsResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/listItems/listItems.feature') listItemsRequest
Then match listItemsResponse.responseStatus == 400
Then def response = listItemsResponse.response
Then match response.code == 400
Then match response.message == 'Bad Request'
Then match response.stack contains 'must be less than or equal to 100'

Scenario: e2e - showItemById - 404 Error

# showItemById-404
Given def showItemByIdRequest = read('test-data/showItemById_404.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/karatetools-openapi-test/xxx-api-rest-stable/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 404
Then def response = showItemByIdResponse.response
Then match response.code == 404
Then match response.message == 'Not Found'
Then match response.stack == 'Item with id 0 not found'
