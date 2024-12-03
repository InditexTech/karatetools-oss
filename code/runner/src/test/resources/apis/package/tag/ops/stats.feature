@ignore
@op.stats

Feature: Stats Test Operation

Background:

Scenario: print
* def req = __arg

* print 'stats test:', req

* def result = 'ok'