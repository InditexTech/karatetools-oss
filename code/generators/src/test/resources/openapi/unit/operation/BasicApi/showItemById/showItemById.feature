@ignore
@op.showItemById

Feature: Info for a specific item
Info for a specific item

Background:
* url urls.testUrl

Scenario: showItemById
* def req = __arg
* def authHeader = call read('classpath:karate-auth.js') req.auth
* def headers = karate.merge(req.headers || {}, authHeader || {})

Given path '/items/', req.params.itemId

And headers headers

When method GET

* def expectedStatusCode = req.statusCode || responseStatus
* match responseStatus == expectedStatusCode

# match response schema in 'test-data' or any object
* def responseContains = req.matchResponse === true && req.responseMatches? req.responseMatches : responseType == 'json'? {} : ''
* match response contains responseContains
