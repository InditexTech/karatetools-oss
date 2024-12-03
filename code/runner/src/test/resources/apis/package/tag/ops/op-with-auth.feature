@ignore
@op.get

Feature: GET Test Operation

Background:
* url urls.xxxApiRestStableUrl

Scenario: get with auth
* def req = __arg
* def authHeader = call read('classpath:karate-auth.js') req.auth
* def headers = karate.merge(req.headers || {}, authHeader || {})

* karate.logger.debug('headers=', headers)

Given path '/get'
And headers headers
When method GET