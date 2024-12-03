@karate-clients
@db @mongodb @db-mongodb
@mongodb-client 
@env=local

Feature: Mongo DB Client Available Operations 

Background:
# public MongoDBClient(final Map<Object, Object> configMap)
# Instantiate MongoDBClient
Given def config = read('classpath:config/db/mongodb-config-' + karate.env + '.yml')
Given def MongoDBClient = Java.type('dev.inditex.karate.mongodb.MongoDBClient')
Given def mongoDBClient = new MongoDBClient(config)

# Define collection
Given def collection = "data"

# Define Filters
Given def manyFilter = { "name": { "$regex": "^karate-(.*)" } }
Given def oneFilter = { "_id": "1" }
Given def someFilter = { "value": { $gt: 1 } }
# Define Data
Given def oneDocument = { "_id": "1", "name": "karate-01", "value": 1 }
Given def oneDocumentReplaced = { "_id": "1", "name": "karate-01BIS", "value": 1 }
Given def manyDocuments = [ { "_id": "2", "name": "karate-02", "value": 2 }, { "_id": "3", "name": "karate-03", "value": 3 } ]

Scenario: Mongo DB Client Available Operations
# public Boolean available()
When def available = mongoDBClient.available()
Then if (!available) karate.fail('MongoDB Client not available')

# public Long count(final String collectionName)
When def count = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', count)
Then assert count >= 0 

# public long deleteMany(final String collectionName, final Map<String, Object> filter)
When def deletedMany = mongoDBClient.deleteMany(collection, manyFilter)
Then karate.log('mongoDBClient.delete(',collection,', ', manyFilter, ')=', deletedMany)
Then assert deletedMany >= 0 
When def countAfterDeleteMany = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', countAfterDeleteMany)
Then assert countAfterDeleteMany == 0 

# public void insert(final String collectionName, final Map<String, Object> value)
# insert(String, Map<String, Object>) : void
When mongoDBClient.insert(collection, oneDocument)
When def countAfterInsertOne = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', countAfterInsertOne)
Then assert countAfterInsertOne == 1 

# public List<Map<String, Object>> find(final String collectionName, final Map<String, Object> filter)
When def findOne = mongoDBClient.find(collection, oneFilter)
Then karate.log('mongoDBClient.find(',collection,',', oneFilter, ')=', findOne.length )
Then assert findOne.length == 1
Then match findOne[0]._id == '1'
Then match findOne[0].name == 'karate-01'
Then match findOne[0].value == 1

# public void insertMany(final String collectionName, final List<Map<String, Object>> values)
When mongoDBClient.insertMany(collection, manyDocuments)
When def countAfterInsertMany = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', countAfterInsertMany)
Then assert countAfterInsertMany == 3

# public List<Map<String, Object>> find(final String collectionName)
When def findAll = mongoDBClient.find(collection)
Then karate.log('mongoDBClient.find(',collection,')=', findAll.length )
Then assert findAll.length == 3 

# public List<Map<String, Object>> find(final String collectionName, final Map<String, Object> filter)
When def findSome = mongoDBClient.find(collection, someFilter)
Then karate.log('mongoDBClient.find(',collection,',', someFilter, ')=', findSome.length )
Then assert findSome.length == 2
Then def result = karate.sort(findSome, x => x._id)
Then match result[0]._id == '2'
Then match result[0].name == 'karate-02'
Then match result[0].value == 2
Then match result[1]._id == '3'
Then match result[1].name == 'karate-03'
Then match result[1].value == 3

# public long replace(final String collectionName, final Map<String, Object> filter, final Map<String, Object> value)
When def replace = mongoDBClient.replace(collection, oneFilter, oneDocumentReplaced)
Then karate.log('mongoDBClient.replace(',collection,', ', oneFilter, ', ', oneDocumentReplaced, ')=', replace)
Then def countAfterReplace = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', countAfterReplace)
Then assert countAfterReplace == 3 

# public long delete(final String collectionName, final Map<String, Object> value)
When def deleteOne = mongoDBClient.delete(collection, oneFilter)
Then karate.log('mongoDBClient.delete(',collection,', ', oneFilter, ')=', deleteOne)
Then assert deleteOne == 1 
Then def countAfterDeleteOne = mongoDBClient.count(collection)
Then karate.log('mongoDBClient.count(',collection,')=', countAfterDeleteOne)
Then assert countAfterDeleteOne == 2
