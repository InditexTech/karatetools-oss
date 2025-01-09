@ignore
@op.deleteAllItems

Feature: Delete all items
Delete all items

Background:
* url urls.xxxApiRestStableUrl

Scenario: deleteAllItems
* def req = __arg
* def authHeader = call read('classpath:karate-auth.js') req.auth
* def headers = karate.merge(req.headers || {}, authHeader || {})

Given path '/items'

And headers headers

When method DELETE

* def expectedStatusCode = req.statusCode || responseStatus
* match responseStatus == expectedStatusCode

# match response schema in 'test-data' or any object
* def responseContains = req.matchResponse === true && req.responseMatches? req.responseMatches : responseType == 'json'? {} : ''
* match response contains responseContains
