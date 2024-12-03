@inditex-oss-karate
@karate-auth
@karate-auth-basic

Feature: karate-auth-basic

Background:
* callonce read('auth-background-feature.feature')
* call read('auth-background-scenario.feature')

* def authJS = read('classpath:karate-auth.js')

* def req =
"""
{
  'auth': {
    'authMode': 'basic',
    'username': 'basic',
    'password': 'basic'
  }
}
"""

Scenario: karate-auth-basic-invalid-mode

Given req.auth.authMode = 'BASIC'

When def authHeader = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then detachLogWatcher()
# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> Trying to authenticate with no authMode or invalid authMode. auth.authMode:BASIC"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-basic-invalid-username

Given req.auth.username = null

When def authHeader = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then detachLogWatcher()
# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> Trying to authenticate with no username. auth.authMode:basic auth.username:null"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-basic-invalid-password

Given req.auth.password = null

When def authHeader = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')
# Match header
Then match authHeader == null
# Match last log
Then def actualLogs = getLastLogs(1)
Then def expectedLog = ">> auth >> Trying to authenticate with no password. auth.authMode:basic auth.username:basic auth.password:null"
Then match actualLogs contains any expectedLog

Scenario: karate-auth-basic-local

When def authHeader = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')
# Match header
Then def expectedAuthorization = 'Basic YmFzaWM6YmFzaWM='
Then match authHeader.Authorization == expectedAuthorization

Scenario: karate-auth-basic-local-injected-password

Given karate.properties['injected.credentials.password'] = 'basic'
Given def req =
"""
{
  'auth': {
    'authMode': 'basic',
    'username': 'basic',
    'password': '#(karate.properties["injected.credentials.password"])'
  }
}
"""

When def authHeader = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match header
Then def expectedAuthorization = 'Basic YmFzaWM6YmFzaWM='
Then match authHeader.Authorization == expectedAuthorization

Scenario: karate-auth-basic-local-cache

When def authHeader = authJS(req.auth, false)
# Execute again for cached token
When def authHeader2 = authJS(req.auth, false)

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match header
Then def expectedAuthorization = 'Basic YmFzaWM6YmFzaWM='
Then match authHeader.Authorization == expectedAuthorization
Then match authHeader2.Authorization == expectedAuthorization

# Match last logs
Then def actualLogs = getLastLogs(1)
Then match actualLogs contains any '>> auth >> basic >> authHeader present in cache'

Scenario: karate-auth-basic-local-operation

When def res = call read('classpath:apis/package/tag/ops/op-with-auth.feature') req

# Detach logWatcher appender to be able to print captured logs and not enter into recursive mode
Then logger.detachAppender('logWatcher')

# Match response status
Then match res.responseStatus == 200
# Match header
Then def expectedAuthorization = 'Basic YmFzaWM6YmFzaWM='
Then match res.responseHeaders['authorization'][0] == expectedAuthorization
