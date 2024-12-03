import java.nio.file.Path
import java.nio.file.Paths
import org.apache.commons.io.FileUtils

// the path where the project got generated
Path projectPath = Paths.get(request.outputDirectory, request.artifactId)

// the properties available to the archetype
Properties properties = request.properties

String kafka = properties.get("includeKafkaClients")
String activemq = properties.get("includeJMSClient_ActiveMQ")
String mariadb = properties.get("includeJDBCClient_MariaDB")
String mongodb = properties.get("includeMongoDBClient")
String postgresql = properties.get("includeJDBCClient_PostgreSQL")

// If options not selected remove the file/directory
if (kafka != "yes" && kafka != "y") {
  FileUtils.deleteDirectory(projectPath.resolve("src/test/resources/config/kafka").toFile())
}
if (activemq != "yes" && activemq != "y") {
  FileUtils.getFile(projectPath.toString() + "/src/test/resources/config/jms/activemq-config-local.yml").delete()
}
if (mariadb != "yes" && mariadb != "y") {
  FileUtils.getFile(projectPath.toString() + "/src/test/resources/config/db/mariadb-config-local.yml").delete()
}
if (mongodb != "yes" && mongodb != "y") {
  FileUtils.getFile(projectPath.toString() + "/src/test/resources/config/db/mongodb-config-local.yml").delete()
}
if (postgresql != "yes" && postgresql != "y") {
  FileUtils.getFile(projectPath.toString() + "/src/test/resources/config/db/postgresql-config-local.yml").delete()
}

File dbDir = projectPath.resolve("src/test/resources/config/db").toFile()
if ((dbDir.listFiles()).length== 0) {
  FileUtils.deleteDirectory(dbDir)
}

File jmsDir = projectPath.resolve("src/test/resources/config/jms").toFile()
if ((jmsDir.listFiles()).length== 0) {
  FileUtils.deleteDirectory(jmsDir)
}

// List the generated sources on the project with the archetype
System.out.println("-------------------------------------------------------------------------------")
System.out.println("Created the following files and directories:")
System.out.println("-------------------------------------------------------------------------------")
listFileNames(FileUtils.getFile(projectPath.toString()).listFiles())
System.out.println("-------------------------------------------------------------------------------")

void listFileNames (File[] listOfFiles) {
  for (file in listOfFiles){
    if (file.isDirectory() && file.listFiles().length > 0) {
      listFileNames(file.listFiles())
    } else {
      System.out.println(file)
    }
  }
}
