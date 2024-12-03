@functional
@op.showItemById 

Feature: showItemByIdWithMultipleCodes

Background:

Scenario: showItemByIdWithMultipleCodes

# showItemById-200
Given def showItemByIdRequest = read('test-data/showItemById_200.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200

# showItemById-404
Given def showItemByIdRequest = read('test-data/showItemById_404.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 404
