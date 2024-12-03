package dev.inditex.karate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import dev.inditex.karate.BasicApplication.DockerComposeServicesReadyEventListener;
import dev.inditex.karate.controller.DockerHealthControllerIT.TestApplicationLoader;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@Tag("IT")
@ActiveProfiles({"test-docker"})
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(loader = TestApplicationLoader.class)
public class DockerHealthControllerIT {

  @Autowired
  private MockMvc mvc;

  @Nested
  class DockerHealth {
    @SuppressWarnings("unchecked")
    @Test
    void when_docker_ready_expect_app_up_and_health_ok() throws Exception {

      final var result = mvc.perform(get("/health/docker"))
          .andExpect(status().isOk())
          .andReturn();

      assertThat(result).isNotNull();
      assertThat(result.getResponse()).isNotNull();
      assertThat(result.getResponse().getContentAsString()).isNotNull();
      final List<String> services = (List<String>) JsonPath.parse(result.getResponse().getContentAsString()).read("$[*].name");
      assertThat(services).isNotNull().containsExactlyInAnyOrder(expectedServices());
    }
  }

  public static class TestApplicationLoader extends SpringBootContextLoader {
    @Override
    protected SpringApplication getSpringApplication() {
      final SpringApplication app = super.getSpringApplication();
      app.addListeners(new DockerComposeServicesReadyEventListener());
      return app;
    }
  }

  protected String[] expectedServices() {
    return new String[]{
        "kt-amq",
        "kt-kafka",
        "kt-mariadb",
        "kt-mongodb",
        "kt-postgres",
        "kt-schema-registry",
        "kt-schema-registry-basic-auth",
        "kt-zookeeper"};
  }
}
