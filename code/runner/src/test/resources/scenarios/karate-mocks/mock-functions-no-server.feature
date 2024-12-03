@inditex-oss-karate
@karate-mock-functions

Feature: Mocks

Background:
* callonce read('classpath:mocks/mock-templates-functions.js')

@list-templates-no-folder
Scenario: list-templates-no-folder
Given def mockTemplatesFolder = 'mocks/templates/no-folder'
When def templatesList = listTemplates(mockTemplatesFolder)
Then match templatesList == '#[0]'

@list-templates-empty-folder
Scenario: list-templates-empty-folder
Given def mockTemplatesFolder = 'mocks/templates/empty-folder'
When def templatesList = listTemplates(mockTemplatesFolder)
Then match templatesList == '#[0]'

@list-templates-folder-with-mocks
Scenario: list-templates-folder-with-mocks
Given def mockTemplatesFolder = 'mocks/templates/standalone'
When def templatesList = listTemplates(mockTemplatesFolder)
Then match templatesList == '#[3]'

@read-templates-no-folder
Scenario: read-templates-no-folder
Given def mockTemplatesFolder = 'mocks/templates/no-folder'
Given def templatesList = listTemplates(mockTemplatesFolder)
When def templates = readTemplates(templatesList)
Then match templates == '#[0]'

@read-templates-empty-folder
Scenario: read-templates-empty-folder
Given def mockTemplatesFolder = 'mocks/templates/empty-folder'
Given def templatesList = listTemplates(mockTemplatesFolder)
When def templates = readTemplates(templatesList)
Then match templates == '#[0]'

@read-templates-folder-with-mocks
Scenario: read-templates-folder-with-mocks
Given def mockTemplatesFolder = 'mocks/templates/standalone'
Given def templatesList = listTemplates(mockTemplatesFolder)
When def templates = readTemplates(templatesList)
Then match templates == '#[3]'
