@ignore
Feature: mock auth feature

Background:

# #################################################
# Echo Service
# #################################################
Scenario:

* def mock = {}

* def responseStatus =  200
* def responseHeaders = karate.merge({ 'Content-Type': 'application/json' }, requestHeaders)
* def response =        request

* call read('@logRequestResponse')

@logRequestResponse @ignore
Scenario:
* karate.logger.debug('>> requestUrlBase =', requestUrlBase);
* karate.logger.debug('>> requestUri     =', requestUri)
* karate.logger.debug('>> requestMethod  =', requestMethod)
* karate.logger.debug('>> requestParams  =', requestParams)
* karate.logger.debug('>> requestHeaders =', requestHeaders)
* karate.logger.debug('>> request        =', request)
* karate.logger.debug('>> mock           =', mock)
* karate.logger.debug('>> responseStatus =', responseStatus)
* karate.logger.debug('>> responseHeaders=', responseHeaders)
* karate.logger.debug('>> response       =', response)
