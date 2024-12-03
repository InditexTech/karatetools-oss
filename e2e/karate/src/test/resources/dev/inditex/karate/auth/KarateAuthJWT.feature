@karate-auth
@karate-auth-jwt

Feature: karate-auth-jwt

Background:
* call read('KarateAuthBackgroundScenario.feature')

* def authJS = read('classpath:karate-auth.js')

Scenario: karate-auth-jwt-invalid-mode
Given def req =
"""
{
  'auth': {
    'authMode': 'JWT'
  }
}
"""
When def authHeader = authJS(req.auth, false)
# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then detachLogWatcher()

# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> Trying to authenticate with no authMode or invalid authMode. auth.authMode:JWT"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-jwt-invalid-username
Given def req =
"""
{
  'auth': {
    'authMode': 'jwt'
  }
}
"""
When def authHeader = authJS(req.auth, false)
# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then detachLogWatcher()

# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> Trying to authenticate with no username. auth.authMode:jwt auth.username:null"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-jwt-local

Given def req =
"""
{
  'auth': {
    'authMode': 'jwt',
    'username': 'jwt'
  }
}
"""
When def authHeader = authJS(req.auth, false)
# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match header
Then def expectedAuthorization = 'Bearer eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.eyJpZCI6IjEyMzQiLCJzdWIiOiJqd3QiLCJpc3MiOiJodHRwczovL3d3dy5pbmRpdGV4LmNvbS9qd3QtdG9rZW4iLCJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwMH0.S4ACftBoFfFhbLDN0kunk_oxcy5IAZfa74W3zxOExNA'
Then match authHeader.Authorization == expectedAuthorization

Scenario: karate-auth-jwt-local-invalid-jwt-algorithm

Given def req =
"""
{
  'auth': {
    'authMode': 'jwt',
    'username': 'invalid'
  }
}
"""
When def authHeader = authJS(req.auth, false)
# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then detachLogWatcher()

# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> jwt >> failed to generate jwt >> exception >> Unrecognized JWS Digital Signature or MAC id: HS256B"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-basic-local-cache

Given def req =
"""
{
  'auth': {
    'authMode': 'jwt',
    'username': 'jwt'
  }
}
"""

When def authHeader = authJS(req.auth, false)
# Execute again for cached token
When def authHeader2 = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match header
Then def expectedAuthorization = 'Bearer eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1QifQ.eyJpZCI6IjEyMzQiLCJzdWIiOiJqd3QiLCJpc3MiOiJodHRwczovL3d3dy5pbmRpdGV4LmNvbS9qd3QtdG9rZW4iLCJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwMH0.S4ACftBoFfFhbLDN0kunk_oxcy5IAZfa74W3zxOExNA'
Then match authHeader.Authorization == expectedAuthorization
Then match authHeader2.Authorization == expectedAuthorization

# Match last logs
Then def actualLogs = getLastLogs(1)
Then match actualLogs contains any '>> auth >> jwt >> authHeader present in cache'
