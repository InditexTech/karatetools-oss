@ignore
Feature: karate-auth-background-feature

Background:

Scenario: karate-auth-background-feature

# Functions
* def configureMocks =
"""
function() {
  if (karate.env == 'local') {
    var parentFolder = getParentFolder(karate.feature.prefixedPath, false)
    var parentFolderAbsolute = getParentFolder(karate.feature.prefixedPath)
    var mock = 'classpath:' + parentFolder + '/mocks/mock-auth.feature'
	  var cert = parentFolderAbsolute + '/mocks/karate-mock-server-cert.pem'
	  var key  = parentFolderAbsolute + '/mocks/karate-mock-server-key.pem'
	  var ssl  = true

	  var mockServer = karate.start( { 'mock': mock, 'ssl': ssl, 'cert': cert, 'key': key } )
	  urls.xxxApiRestStableUrl = 'https://localhost:' + mockServer.port + '/service'
	  return mockServer
  }
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
    mockServer.stopAndWait();
	}
}
"""

# Settings
* def configApiRestUrl = urls.xxxApiRestStableUrl

* configure ssl = { trustAll: true }
* def mockServer = configureMocks()
* configure afterFeature = afterFeatureMocks
