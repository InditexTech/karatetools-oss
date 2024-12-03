Feature:
Scenario: HelloTest
* def result =
"""
{
  "defaultArray" : [ {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  } ],
  "requiredArray" : [ {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  } ],
  "nullableArray" : [ {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  } ],
  "nestedArray" : [ [ {
    "stringResponse" : "string",
    "integerResponse" : 1.5
  } ] ],
  "simpleType" : [ "string" ],
  "simpleType2" : [ "string" ],
  "simpleType3" : [ {
    "hello" : "world"
  } ]
}
"""
* def nullableArray =
"""
 {
  "stringResponse" : "##string",
  "integerResponse" : "#number"
}
"""
* def requiredArray =
"""
 {
  "stringResponse" : "##string",
  "integerResponse" : "#number"
}
"""
* def defaultArray =
"""
 {
  "stringResponse" : "##string",
  "integerResponse" : "#number"
}
"""
* match result ==
"""
 {
  "simpleType" : "##[] #string",
  "nullableArray" : "##[] nullableArray",
  "simpleType3" : "##[] #object",
  "simpleType2" : "##[] ##string",
  "requiredArray" : "#[] requiredArray",
  "nestedArray" : "##[]",
  "simpleType4" : "##object",
  "defaultArray" : "##[] defaultArray"
}
"""
