@inditex-oss-karate
@karate-mock-functions

Feature: Mocks

Background:

* print karate.toJson(karate.config)

# ###############################################
# Mock Templates settings
# ###############################################
* def mockTemplatesFolder = 'mocks/templates'
* def mockFile = 'classpath:mocks/mock-templates.feature'
* def mockPort = 0

* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockTemplatesFolder >> ', mockTemplatesFolder)
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockFile            >> ', mockFile)
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockPort            >> ', mockPort)
# ###############################################

# ###############################################
# Start Mock
# ###############################################
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> start mock')
* print 'karate >>>' + karate
* print 'karate.engine >>>' + karate.engine
# * def mockArgs = karate.getEngine().getAllVariablesAsMap()
* def mockArgs = karate.toMap(karate.getContext())
* def mockServer = com.intuit.karate.core.MockServer.feature(mockFile).http(mockPort).args(mockArgs).build()
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> start mock >> DONE')
# ###############################################

@find-template-not-found
Scenario: find-template-not-found
Given url 'http://localhost:' + mockServer.port
Given path '/post/123'
When method POST
Then match responseStatus == 404

@find-template-found
Scenario: find-template-found
Given url 'http://localhost:' + mockServer.port
Given path '/get'
When method GET
Then match responseStatus == 200

@find-template-not-found-path-param
Scenario: find-template-not-found-path-param
Given url 'http://localhost:' + mockServer.port
Given path '/post/123/value'
When method POST
Then match responseStatus == 404

@find-template-found-path-param
Scenario: find-template-found-path-param
Given url 'http://localhost:' + mockServer.port
Given path '/get/123'
When method GET
Then match responseStatus == 204
