@inditex-oss-karate
@karate-openapi
@karate-mocks

Feature: with-mocks

Background:

* def getParentFolder =
  """
  function() {
    var path = karate.feature.prefixedPath;
    var parent = Java.type("java.nio.file.Paths").get(path.substring(path.indexOf(':') + 1)).getParent().toString();
    return parent.replaceAll("\\", "/")
  }
  """

# ###############################################
# Start Mock Server
# ###############################################
* karate.logger.debug('>> karate.tools >> inline-mocks >> mock-templates-inline >> start mock')
* def mockTemplatesFolder = getParentFolder() + '/mocks'
* def mockPort = 0
* def mockArgs = karate.getEngine().getAllVariablesAsMap()

* callonce read('classpath:mocks/mock-templates-inline.feature')
# ###############################################

# Capture configuration URLs
* def configApiRestUrl = urls.xxxApiRestStableUrl

# Override URL with Inline Mock Server
* def inlineMocksScenario =
"""
function() {
  var urls = karate.get('urls');
  urls.xxxApiRestStableUrl = 'http://localhost:' + mockServer.port
  karate.set('urls', urls)
  karate.logger.debug('>> urls.xxxApiRestStableUrl', urls.xxxApiRestStableUrl)
}
"""

# Restore configuration URLs
* configure afterScenario =
"""
function() {
  karate.logger.debug('after scenario:', karate.scenario.name);
  var urls = karate.get('urls');
  urls.xxxApiRestStableUrl = karate.get('configApiRestUrl');
  karate.set('urls', urls)
  karate.logger.debug('>> urls.xxxApiRestStableUrl', urls.xxxApiRestStableUrl)
}
"""

@karate-mocks-standalone
@ENGPROCLAB-418
@env=local
Scenario: get-standalone
Given def req = {}
When def res = call read('classpath:apis/package/tag/ops/get.feature') req
Then match res.responseStatus == 200

@karate-mocks-standalone
@ENGPROCLAB-418
@env=local
Scenario: post-standalone
Given def req = { op: 2, status : '1' }
When def res = call read('classpath:apis/package/tag/ops/post.feature') req
Then match res.responseStatus == 201

@karate-mocks-inline
@ENGPROCLAB-418
@env=local
Scenario: get-inline
* inlineMocksScenario()
Given def req = {}
When def res = call read('classpath:apis/package/tag/ops/get.feature') req
Then match res.responseStatus == 204

@karate-mocks-inline
@ENGPROCLAB-418
@env=local
Scenario: post-inline
* inlineMocksScenario()
Given def req = { op: 2, status : '1' }
When def res = call read('classpath:apis/package/tag/ops/post.feature') req
Then match res.responseStatus == 202
