@inditex-oss-karate
@http-log-masking

Feature: karate-http-log-masking

Background:

* callonce read('../auth/auth-background-feature.feature')
* call read('../auth/auth-background-scenario.feature')

* def authJS = read('classpath:karate-auth.js')

Scenario: karate-http-log-masking-basic

Given def req =
"""
{
  'auth': {
    'authMode': 'basic',
    'username': 'basic',
    'password': 'basic'
  },
  'headers' : {
    'token': 'test',
    'secret': 'test',
    'key': 'test',
    'username': 'test',
    'password': 'test',
  },
  'body': {
    'message': 'basic'
  }
}
"""

When def res = call read('classpath:apis/package/tag/ops/op-with-auth.feature') req

# Match response status
Then match res.responseStatus == 200
# Match header
Then def expectedAuthorization = 'Basic YmFzaWM6YmFzaWM='
Then match res.responseHeaders['authorization'][0] == expectedAuthorization

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match last logs - should contain masked Authorization header
Then def actualLogs = getLastLogs(1)
Then match actualLogs[0] contains 'authorization: Basic *****'

# Match last logs - should contain masked sensitive headers
Then match actualLogs[0] contains 'token: *****'
Then match actualLogs[0] contains 'secret: *****'
Then match actualLogs[0] contains 'key: *****'
Then match actualLogs[0] contains 'username: *****'
Then match actualLogs[0] contains 'password: *****'

Scenario: karate-http-log-masking-jwt
Given def req =
"""
{
  'auth': {
    'authMode': 'jwt',
    'username': 'jwt'
  },
  'headers' : {
    'token': 'test',
    'secret': 'test',
    'key': 'test',
    'username': 'test',
    'password': 'test',
  },
  'body': {
    'message': 'jwt'
  }
}
"""

When def res = call read('classpath:apis/package/tag/ops/op-with-auth.feature') req

# Match response status
Then match res.responseStatus == 200
# Match header
Then def expectedAuthorization = 'Bearer eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.eyJpZCI6IjEyMzQiLCJzdWIiOiJqd3QiLCJpc3MiOiJodHRwczovL3d3dy5pbmRpdGV4LmNvbS9qd3QtdG9rZW4iLCJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwMH0.S4ACftBoFfFhbLDN0kunk_oxcy5IAZfa74W3zxOExNA'
Then match res.responseHeaders['authorization'][0] == expectedAuthorization

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match last logs - should contain masked Authorization header
Then def actualLogs = getLastLogs(1)
Then match actualLogs[0] contains 'authorization: Bearer *****'

# Match last logs - should contain masked sensitive headers
Then match actualLogs[0] contains 'token: *****'
Then match actualLogs[0] contains 'secret: *****'
Then match actualLogs[0] contains 'key: *****'
Then match actualLogs[0] contains 'username: *****'
Then match actualLogs[0] contains 'password: *****'
