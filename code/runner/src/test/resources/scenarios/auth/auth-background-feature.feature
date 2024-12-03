@ignore
Feature: karate-auth-background-feature

Background:

Scenario: karate-auth-background-feature

# Functions
* def configureMocks =
"""
function() {
  if (karate.env == 'local') {
	  var mock = 'classpath:' + getParentFolder() + '/mocks/mock-auth.feature'
	  var cert = 'mocks/karate-mock-server-cert.pem'
	  var key  = 'mocks/karate-mock-server-key.pem'
	  var ssl  = true
	
	  var mockServer = karate.start( { 'mock': mock, 'ssl': ssl, 'cert': cert, 'key': key } )
	  urls.xxxApiRestStableUrl = 'https://localhost:' + mockServer.port + '/service'
	  return mockServer
  }
}
"""

* def getParentFolder =
"""
function() {
  var path = karate.feature.prefixedPath;
  var parent = Java.type("java.nio.file.Paths").get(path.substring(path.indexOf(':') + 1)).getParent().toString();
  return parent.replaceAll("\\", "/")
}
"""

* def afterFeatureMocks =
"""
function() {
  karate.log('afterFeatureMocks');
  if (karate.env == 'local') {
    var urls = karate.get('urls');
    urls.xxxApiRestStableUrl = karate.get('configApiRestUrl');
    karate.set('urls', urls)
    mockServer.stop().join();
	}
}
"""

# Settings
* def configApiRestUrl = urls.xxxApiRestStableUrl

* configure ssl = { trustAll: true }
* def mockServer = configureMocks()
* configure afterFeature = afterFeatureMocks
