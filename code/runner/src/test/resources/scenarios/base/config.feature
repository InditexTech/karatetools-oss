@inditex-oss-karate
@karate-base-config-default

Feature: karate-base-config

Background:

Given def options = karate.properties['karate.options']
Given print 'options >>' + options

Given print 'logMasker >>' + logMasker
Given match logMasker == '#notnull'

@env=local
@config-default-env
Scenario: config-default-env
Given match baseConfigMarker == 'default-config-loaded'
Then match karate.env == 'local'
Then match defaultAuthMode == 'jwt'
Then match defaultUsername == 'username100'
Then match urls.xxxApiRestStableUrl contains 'http://localhost:'
Then match credentials.username100 == 'username100p'

@env=dev
@config-alternative-env
Scenario: config-alternative-env
Given match baseConfigMarker == 'default-config-loaded'
Then match karate.env == 'dev'
Then match defaultAuthMode == 'basic'
Then match defaultUsername == 'devuser'
Then match urls.xxxApiRestStableUrl == 'http://dev.inditex.com/karate'
Then match credentials.userdev == 'userpass'
