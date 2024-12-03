@functional
@op.createItems 
@mock.templates.inline
@env=local

Feature: createItemsWithInlineMocks

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
* karate.log('>> createItemsWithInlineMocks >> Background >> Start Mock')
* def mockTemplatesFolder = getParentFolder() + '/mocks'
* def mockPort = parseInt( karate.properties['KARATE_MOCK_SERVER_PORT'] || '58081' )
* def mockArgs = karate.getEngine().getAllVariablesAsMap()

* callonce read('classpath:mocks/mock-templates-inline.feature')
# ###############################################

Scenario: createItemsWithInlineMocks

# createItems-201
Given def createItemsRequest = read('test-data/createItems_201.yml')
When def createItemsResponse = call read('classpath:apis/dev/inditex/karate/openapi/test/BasicApi/createItems/createItems.feature') createItemsRequest
Then match createItemsResponse.responseStatus == 201
