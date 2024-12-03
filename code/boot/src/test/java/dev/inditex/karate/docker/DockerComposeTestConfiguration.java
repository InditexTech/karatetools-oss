package dev.inditex.karate.docker;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    MongoAutoConfiguration.class
})
@ComponentScan("dev.inditex.karate")
public class DockerComposeTestConfiguration {

}
