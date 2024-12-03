@inditex-oss-karate
@karate-openapi
@smoke
@op.get @op.post

Feature: ops-smoke

Background:

Scenario Outline: op1 <status>
Given def req = { op: 1, status : '<status>' }
When def res = call read('classpath:apis/package/tag/ops/op.feature') req
Then match res.status == '<status>'
Examples:
| status  |
| 200     |

Scenario Outline: op2 <status>
Given def req = { op: 2, status : '<status>' }
When def res = call read('classpath:apis/package/tag/ops/op.feature') req
Then match res.status == '<status>'
Examples:
| status  |
| 201     |
