Feature:
Scenario: HelloTest
* def result =
"""
{
  "stringResponse" : "string",
  "integerResponse" : 1.5,
  "objectResponse" : {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  }
}
"""
* match result ==
"""
 {
  "stringResponse" : "#string",
  "objectResponse" : {
    "stringResponse" : "##string",
    "integerResponse" : "#number"
  },
  "integerResponse" : "#number"
}
"""
