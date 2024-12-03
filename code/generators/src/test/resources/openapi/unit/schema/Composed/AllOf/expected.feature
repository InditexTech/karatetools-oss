Feature:
Scenario: HelloTest
* def result =
"""
{
  "name" : "string",
  "partner" : "string",
  "children" : [ "string" ],
  "childrenObject" : {
    "id" : "string",
    "value" : "string"
  },
  "childrenObjectArray" : [ {
    "id" : "string",
    "value" : "string"
  } ]
}
"""
* def childrenObjectArray = 
"""
 "#(ComposedSchemaObject)" 
"""
* def ComposedSchemaObject = 
"""
 {
  "id" : "##string",
  "value" : "##string"
} 
"""
* match result == 
"""
 {
  "partner" : "##string",
  "children" : "##[] #string",
  "childrenObject" : "##(^^ComposedSchemaObject)",
  "childrenObjectArray" : "##[] childrenObjectArray",
  "name" : "##string"
} 
"""
