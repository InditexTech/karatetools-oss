Feature:
Scenario: HelloTest
* def result =
"""
{
  "mapProperty" : { },
  "objectProperty" : {
    "hello" : "world"
  },
  "objectPropertyTitle" : {
    "hello" : "string"
  },
  "objectPropertyTitle2" : {
    "hello" : "string"
  }
}
"""
* def ObjectResponse2 =
"""
 {
  "hello" : "##string"
}
"""
* match result ==
"""
 {
  "objectPropertyTitle" : "##(^^ObjectResponse2)",
  "objectProperty" : "##object",
  "objectPropertyTitle2" : "##(^^ObjectResponse2)",
  "mapProperty" : "##object"
}
"""
