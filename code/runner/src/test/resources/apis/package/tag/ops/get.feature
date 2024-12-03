@ignore
@op.get

Feature: GET Test Operation

Background:
* url urls.xxxApiRestStableUrl

Scenario: get
* def req = __arg

Given path '/get'
When method GET
