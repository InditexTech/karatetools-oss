Feature:
Scenario: HelloTest
* def result =
"""
{
  "stringResponse" : "string",
  "integerResponse" : 1.5,
  "objectResponse2" : {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  }
}
"""
* def objectResponse2 = 
"""
 {
  "stringResponse" : "##string",
  "integerResponse" : "#number"
} 
"""
* match result == 
"""
 {
  "stringResponse" : "#string",
  "objectResponse2" : "##(^^objectResponse2)",
  "integerResponse" : "#number"
} 
"""
