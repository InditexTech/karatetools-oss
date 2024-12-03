Feature:
  Scenario: HelloTest
    * def result =
"""
{
  "name" : "string"
}
"""
    * match result ==
"""
 "#object"
"""
