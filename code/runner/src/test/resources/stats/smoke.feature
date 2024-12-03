@smoke
@op.op

Feature: op Smoke Tests 

Background:

Scenario Outline: op <status>
* def req = { status : '<status>' }
* def result = call read('classpath:apis/package/tag/op/op.feature') req
Examples:
| status  |
| 201     |
| 400     |
| default |
