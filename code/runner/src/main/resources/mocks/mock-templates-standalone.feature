@ignore @mock.templates.standalone
Feature: Mock templates processor - standalone

Scenario: Mock templates processor - standalone
# ###############################################
# Mock Templates settings
# ###############################################
* def mockTemplatesFolder = 'mocks/templates'
* def mockFile = 'classpath:mocks/mock-templates.feature'
* def mockPort = parseInt( karate.properties['KARATE_MOCK_SERVER_PORT'] || '58081' )

* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockTemplatesFolder >> ', mockTemplatesFolder)
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockFile            >> ', mockFile)
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> mockPort            >> ', mockPort)
# ###############################################

# ###############################################
# Start Mock
# ###############################################
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> start mock')
* def mockArgs = karate.getEngine().getAllVariablesAsMap()
* def mockServer = com.intuit.karate.core.MockServer.feature(mockFile).http(mockPort).args(mockArgs).build()
* karate.logger.debug('>> karate.tools >> mock-templates-standalone >> start mock >> DONE')
# ###############################################
