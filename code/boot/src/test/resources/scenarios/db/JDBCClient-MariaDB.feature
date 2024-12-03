@inditex-oss-karate @karate-clients
@db @mariadb @db-mariadb
@jdbc-client 
@env=local
Feature: JDBC Client Available Operations - MariaDB 

Background:
# public JDBCClient(final Map<Object, Object> configMap)
# Instantiate JDBCClient
Given def config = read('classpath:config/db/mariadb-config.yml')
Given def JDBCClient = Java.type('dev.inditex.karate.db.JDBCClient')
Given def jdbcClient = new JDBCClient(config)

Scenario: JDBC Client Available Operations - MariaDB
# public Boolean available()
When def available = jdbcClient.available()
Then if (!available) karate.fail('JDBC Client not available')

# public int executeUpdateScript(final String script, final boolean ignoreSeparator)
# Truncate Table and Insert 2 records
Given def scriptSQL = karate.readAsString('classpath:scenarios/db/JDBCClient-MariaDB.sql')
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
Given def selectSQL = "SELECT ID, NAME, VALUE FROM DATA ORDER BY VALUE ASC"
When def result = jdbcClient.executeQuery(selectSQL)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')#=',result.length)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')=',result)
Then assert result.length == 3
Then match result[0].ID == '#notnull'
Then match result[0].NAME == 'karate-01'
Then match result[0].VALUE == 1
Then match result[1].ID == '#notnull'
Then match result[1].NAME == 'karate-02'
Then match result[1].VALUE == 2
Then match result[2].ID == '#notnull'
Then match result[2].NAME == 'karate-03'
Then match result[2].VALUE == 3

# public int executeUpdate(final String sql)
# Delete inserted record
Given def deleteSQL = "DELETE FROM DATA WHERE NAME = 'karate-03'"
When def deleted = jdbcClient.executeUpdate(deleteSQL)
Then karate.log('jdbcClient.executeUpdate(',deleteSQL,')=',deleted)
Then assert deleted == 1 

# public List<Map<String, Object>> executeQuery(final String sql)
# Select records after delete
Given def selectSQL = "SELECT ID, NAME, VALUE FROM DATA ORDER BY VALUE ASC"
When def result = jdbcClient.executeQuery(selectSQL)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')#=',result.length)
Then karate.log('jdbcClient.executeQuery(',selectSQL,')=',result)
Then assert result.length == 2
Then match result[0].ID == '#notnull'
Then match result[0].NAME == 'karate-01'
Then match result[0].VALUE == 1
Then match result[1].ID == '#notnull'
Then match result[1].NAME == 'karate-02'
Then match result[1].VALUE == 2
