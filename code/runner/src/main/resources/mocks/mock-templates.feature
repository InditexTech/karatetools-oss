@ignore
Feature: Mock templates processor

Background:
* callonce read('classpath:mocks/mock-templates-functions.js')

* def mockTemplatesFolder = karate.get('mockTemplatesFolder', 'mocks/templates')
* karate.logger.debug('>> karate.tools >> mock-templates >> mockTemplatesFolder >>', mockTemplatesFolder)

* karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates')
* def templatesList = listTemplates(mockTemplatesFolder)
* karate.logger.debug('>> karate.tools >> mock-templates >> listTemplates >>', templatesList)

* karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates')
* def templates = readTemplates(templatesList)
* karate.logger.debug('>> karate.tools >> mock-templates >> readTemplates >>', templates)

Scenario: 
# catch-all

* def template = findTemplate(templates, requestMethod, requestUri, requestParams, request)
* karate.logger.debug('>> karate.tools >> mock-templates >> template >> \n >> ', template);

* def responseStatus = template.responseStatus
* def responseHeaders = template.responseHeaders
* def response = template.response
