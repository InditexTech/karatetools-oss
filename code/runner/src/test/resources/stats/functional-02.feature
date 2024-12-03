@functional
@op.op 

Feature: op

Background:

Scenario: op
Given def req = { status : '1' }
When def res = call read('classpath:apis/package/tag/op/op.feature') req

Given def req = { status : '2' }
When def res = call read('classpath:apis/package/tag/op/op.feature') req

Given def req = { status : '3' }
When def res = call read('classpath:apis/package/tag/op/op.feature') req
