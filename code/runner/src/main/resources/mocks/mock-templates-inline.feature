@ignore @mock.templates.inline
Feature: Mock templates processor - inline

Scenario: Mock templates processor - inline
# ###############################################
# Mock Templates settings
# ###############################################
* def mockFile = 'classpath:mocks/mock-templates.feature'
# Variables from Caller: mockTemplatesFolder, mockPort, mockArgs
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> mockTemplatesFolder >> ', mockTemplatesFolder)
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> mockFile            >> ', mockFile)
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> mockPort            >> ', mockPort)
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> mockArgs            >> ', mockArgs)

# Log if standalone mocks running
* def standAloneMocks = karate.properties['karate.options'] ? ( karate.properties['karate.options'].includes('~@mock.templates.standalone') ? false : true ) : true
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> karate.options[@mock.templates.standalone] >> ', standAloneMocks)

# ###############################################
# Configure afterScenario
# ###############################################
* configure afterScenario = 
"""
function(){
  if (mockServer) mockServer.stop().join()
}
"""
# ###############################################
# Start Mock
# ###############################################
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> start mock')
* def mockServer = com.intuit.karate.core.MockServer.feature(mockFile).http(mockPort).args(mockArgs).build()
* karate.logger.debug('>> karate.tools >> mock-templates-inline >> start mock >> DONE')
# ###############################################
