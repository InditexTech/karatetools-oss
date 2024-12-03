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
* def objectResponse =
"""
 {
  "stringResponse" : "##string",
  "integerResponse" : "##number"
}
"""
* match result ==
"""
 {
  "stringResponse" : "##string",
  "objectResponse" : "##(^^objectResponse)",
  "integerResponse" : "##number"
}
"""
