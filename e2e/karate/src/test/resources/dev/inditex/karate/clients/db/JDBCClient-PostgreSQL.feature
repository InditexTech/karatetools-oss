@karate-clients
@db @postgresql @db-postgresql
@jdbc-client 
@env=local

Feature: JDBC Client Available Operations - PostgreSQL 

Background:
# public JDBCClient(final Map<Object, Object> configMap)
# Instantiate JDBCClient
Given def config = read('classpath:config/db/postgresql-config-' + karate.env + '.yml')
Given def JDBCClient = Java.type('dev.inditex.karate.db.JDBCClient')
Given def jdbcClient = new JDBCClient(config)

Scenario: JDBC Client Available Operations - PostgreSQL
# public Boolean available()
When def available = jdbcClient.available()
Then if (!available) karate.fail('JDBC Client not available')

# public int executeUpdateScript(final String script, final boolean ignoreSeparator)
# Truncate Table and Insert 2 records
Given def scriptSQL = karate.readAsString('JDBCClient-PostgreSQL.sql');
When def insertedScript = jdbcClient.executeUpdateScript(scriptSQL, false)
Then karate.log('jdbcClient.executeUpdateScript(',scriptSQL,')=',insertedScript)
Then assert insertedScript == 2 

# public int executeUpdate(final String sql)
# Insert a record
Given def insertSQL = "INSERT INTO DATA (NAME, VALUE) VALUES ('karate-03', 3)"
When def inserted = jdbcClient.executeUpdate(insertSQL)
Then karate.log('jdbcClient.executeUpdate(',insertSQL,')=',inserted)
Then assert inserted == 1 

# public List<Map<String, Object>> executeQuery(final String sql)
# Select inserted records
Given def selectSQL = "SELECT ID, NAME, VALUE FROM DATA ORDER BY VALUE ASC";
When def result = jdbcClient.executeQuery(selectSQL)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')#=',result.length)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')=',result)
Then assert result.length == 3
Then match result[0].id == '#notnull'
Then match result[0].name == 'karate-01'
Then match result[0].value == 1
Then match result[1].id == '#notnull'
Then match result[1].name == 'karate-02'
Then match result[1].value == 2
Then match result[2].id == '#notnull'
Then match result[2].name == 'karate-03'
Then match result[2].value == 3

# public int executeUpdate(final String sql)
# Delete inserted record
Given def deleteSQL = "DELETE FROM DATA WHERE NAME = 'karate-03'";
When def deleted = jdbcClient.executeUpdate(deleteSQL)
Then karate.log('jdbcClient.executeUpdate(',deleteSQL,')=',deleted)
Then assert deleted == 1 

# public List<Map<String, Object>> executeQuery(final String sql)
# Select records after delete
Given def selectSQL = "SELECT ID, NAME, VALUE FROM DATA ORDER BY VALUE ASC";
When def result = jdbcClient.executeQuery(selectSQL)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')#=',result.length)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')=',result)
Then assert result.length == 2
Then match result[0].id == '#notnull'
Then match result[0].name == 'karate-01'
Then match result[0].value == 1
Then match result[1].id == '#notnull'
Then match result[1].name == 'karate-02'
Then match result[1].value == 2
