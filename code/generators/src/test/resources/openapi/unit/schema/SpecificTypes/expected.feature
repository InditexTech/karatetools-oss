Feature:
Scenario: HelloTest
* def result =
"""
{
  "uuidSchema" : "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "emailSchema" : "apiteam@swagger.io",
  "passwordSchema" : "string",
  "dateSchema" : "2015-07-20",
  "datetimeSchema" : "2015-07-20T15:49:04-07:00",
  "booleanSchema" : true
}
"""
* match result ==
"""
 {
  "byteSchema" : "##string",
  "booleanSchema" : "##boolean",
  "uuidSchema" : "##uuid",
  "datetimeSchema" : "##string",
  "binarySchema" : "##string",
  "emailSchema" : "##string",
  "dateSchema" : "##string",
  "passwordSchema" : "##string"
}
"""
