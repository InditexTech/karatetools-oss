@inditex-oss-karate
@karate-openapi
@functional
@op.op

Feature: ops-functional

Background:

Scenario: op1
Given def req = { op: 1, status : '200' }
When def res = call read('classpath:apis/package/tag/ops/op.feature') req
Then match res.status == '200'

Scenario: op2
Given def req = { op: 2, status : '201' }
When def res = call read('classpath:apis/package/tag/ops/op.feature') req
Then match res.status == '201'
