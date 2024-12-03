@ignore
@op.op

Feature: Test Operation

Background:
* url urls.xxxApiRestStableUrl

Scenario: print
* def req = __arg
* karate.logger.debug('req=', req)
* def res = req