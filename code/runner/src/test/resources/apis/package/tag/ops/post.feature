@ignore
@op.post

Feature: POST Test Operation

Background:
* url urls.xxxApiRestStableUrl

Scenario: post
* def req = __arg

Given path '/post'
And request req
When method POST
