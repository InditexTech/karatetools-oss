@functional
@op.showItemById 

Feature: showItemById

Background:

Scenario: showItemById

# showItemById-200
Given def showItemByIdRequest = read('test-data/showItemById_200.yml')
When def showItemByIdResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/showItemById/showItemById.feature') showItemByIdRequest
Then match showItemByIdResponse.responseStatus == 200
