@inditex-oss-karate
@karate-cache

Feature: karate-cache

Background:

Scenario: karate-cache-static

Given cache.clear()
Given cache.put('var1', 'value1')
When def var1 = cache.get('var1')
Then match var1 == 'value1'
Then cache.remove('var1')
Then print cache.get()

Scenario: karate-cache-variable

Given cache.clear()
Given def value2 = 'value2'
Given cache.put('var2', value2)
When def var2 = cache.get('var2')
Then match var2 == 'value2'
Then cache.remove('var2')
Then print cache.get()
