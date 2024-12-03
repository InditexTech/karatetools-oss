@inditex-oss-karate
@karate-stats
@functional
@op.stats

Feature: ops-stats

Background:

Scenario: stats
Given def req = {}
When def res = call read('classpath:apis/package/tag/ops/stats.feature') req
Then match res.result == 'ok'

