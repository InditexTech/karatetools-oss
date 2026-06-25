@inditex-oss-karate
@karate-base-utils

Feature: karate-base-utils

Background:

Given def options = karate.properties['karate.options']
Given print 'options >>' + options

Given print 'utils >>' + utils
Given match utils == '#notnull'

@utils-read-test-data
Scenario: utils-read-test-data-merges-hierarchy
# readTestData chain: requested file -> parent underscore variants -> baseRequest.json
# With requested file order_refund_partial.json, readTestData resolves files in this order:
# 1) baseRequest.json 2) order.json 3) order_refund.json 4) order_refund_partial.json
# It then merges left-to-right, so deeper files override scalar/object fields from earlier ones.
# Arrays are concatenated by utils._deepMerge, which is why flags become ['base','order','refund','partial'].
Given def requestedFile = 'classpath:scenarios/base/data/order_refund_partial.json'
Given def expectedFiles =
"""
[
	'classpath:scenarios/base/data/baseRequest.json',
	'classpath:scenarios/base/data/order.json',
	'classpath:scenarios/base/data/order_refund.json',
	'classpath:scenarios/base/data/order_refund_partial.json'
]
"""
# Read each expected file explicitly so the scenario documents the exact source inputs.
Given def baseRequest = karate.read(expectedFiles[0])
Given def order = karate.read(expectedFiles[1])
Given def orderRefund = karate.read(expectedFiles[2])
Given def orderRefundPartial = karate.read(expectedFiles[3])
Then match baseRequest == { source: 'baseRequest', flags: ['base'], payload: { requestType: 'order', channel: 'web' } }
Then match order == { source: 'order', flags: ['order'], payload: { country: 'ES', currency: 'EUR' } }
Then match orderRefund == { source: 'order_refund', flags: ['refund'], payload: { operation: 'refund', reason: 'damaged' } }
Then match orderRefundPartial == { source: 'order_refund_partial', flags: ['partial'], payload: { amount: 15.5, status: 'approved' } }

# Execute the utility under test and verify both precedence and concatenation behavior.
When def data = utils.readTestData(requestedFile)
Then match data.source == 'order_refund_partial'
Then match data.flags == ['base', 'order', 'refund', 'partial']
Then match data.payload == { requestType: 'order', channel: 'web', country: 'ES', currency: 'EUR', operation: 'refund', reason: 'damaged', amount: 15.5, status: 'approved' }

@utils-is-array
Scenario: utils-is-array-detects-array-values
Then match utils.isArray([]) == true
Then match utils.isArray([1, 2, 3]) == true
Then match utils.isArray({}) == false
Then match utils.isArray('abc') == false

@utils-merge
Scenario: utils-merge-combines-objects
Given def left = { a: 1, nested: { x: 1 }, arr: [1] }
Given def right = { b: 2, nested: { y: 2 }, arr: [2] }
When def merged = utils.merge(left, right)
Then match merged == { a: 1, b: 2, nested: { x: 1, y: 2 }, arr: [1, 2] }

@utils-deep-merge
Scenario: utils-deep-merge-mutates-target
Given def target = { nested: { x: 1 }, arr: [1] }
Given def source = { nested: { y: 2 }, arr: [2], c: 3 }
When eval utils._deepMerge(target, source)
Then match target == { nested: { x: 1, y: 2 }, arr: [1, 2], c: 3 }

@utils-replace-expressions
Scenario: utils-replace-expressions-resolves-known-vars
Given def storeId = 's1'
Given def country = 'es'
Given def source = { id: '#(storeId)', nested: { region: '#(country)' }, unresolved: '#(missing)' }
When def replaced = utils.replaceExpressions(source)
Then match replaced.id == 's1'
Then match replaced.nested.region == 'es'
Then assert replaced.unresolved == '#(missing)'

@utils-manipulate-leaf-values
Scenario: utils-manipulate-leaf-values-applies-callback
Given def source = { a: 1, nested: { b: 2 }, arr: [3, { c: 4 }], text: 'x' }
Given def callback = function(v){ if (typeof v === 'number') return v + 10; return v }
When def transformed = utils.manipulateLeafValues(source, callback)
Then match transformed == { a: 11, nested: { b: 12 }, arr: [13, { c: 14 }], text: 'x' }
