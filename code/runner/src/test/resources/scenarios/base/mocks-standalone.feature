@inditex-oss-karate
@karate-base-mocks

Feature: karate-base-mocks-standalone

Background:

Given def options = karate.properties['karate.options']
Given print 'options >>' + options

Given print 'mocks >>' + mocks

@mock.templates.standalone
Scenario: mocks-bootstrap-enabled-with-standalone-tag
Given match karate.env == 'local'
When def standaloneDisabled = options.contains('~@mock.templates.standalone')
Then match standaloneDisabled == false
Then match mocks == '#notnull'
Then match mocks.mockFile == 'classpath:mocks/mock-templates.feature'
Then match mocks.mockTemplatesFolder == 'mocks/templates'
Then match mocks.mockPort == '#number'
Then match mocks.mockPort == '#(parseInt(karate.properties["KARATE_MOCK_SERVER_PORT"]))'

@mock.templates.inline
Scenario: mocks-bootstrap-disabled-with-inline-tag
Given match karate.env == 'local'
When def options = karate.properties['karate.options']
When def standaloneDisabled = options.contains('~@mock.templates.standalone')
Then match standaloneDisabled == true
Then assert mocks == null || typeof mocks == 'undefined'
