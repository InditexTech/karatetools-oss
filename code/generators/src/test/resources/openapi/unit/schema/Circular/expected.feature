Feature:
Scenario: HelloTest
* def result =
"""
{
  "name" : "string",
  "partner" : {
    "name" : "string",
    "partner" : { },
    "children" : [ { } ]
  },
  "children" : [ {
    "name" : "string",
    "partner" : { },
    "children" : [ { } ]
  } ]
}
"""
* match result == 
"""
 {
  "partner" : "##object",
  "children" : "##[] #object",
  "name" : "##string"
} 
"""
