package dev.inditex.karate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import dev.inditex.karate.BasicApplication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.docker.compose.core.ConnectionPorts;
import org.springframework.boot.docker.compose.core.ImageReference;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class DockerHealthControllerTest {

  protected DockerHealthController controller;

  @BeforeEach
  protected void beforeEach() {
    controller = new DockerHealthController();
  }

  @Nested
  class Health {
    @Test
    void when_null_running_services_expect_service_unavailable_with_null_body() {
      try (final MockedStatic<BasicApplication> application = mockStatic(BasicApplication.class)) {
        application.when(BasicApplication::getRunningServices).thenReturn(null);

        final var response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()));
        assertThat(response.getBody()).isNull();
      }
    }

    @Test
    void when_empty_running_services_expect_ok_with_empty_body() {
      try (final MockedStatic<BasicApplication> application = mockStatic(BasicApplication.class)) {
        application.when(BasicApplication::getRunningServices).thenReturn(List.of());

        final var response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertThat(response.getBody()).isEmpty();
      }
    }

    @Test
    void when_running_services_expect_ok_with_informed_body() {
      try (final MockedStatic<BasicApplication> application = mockStatic(BasicApplication.class)) {
        final RunningService service1 = mockService("service-1", "docker.host/library/image-1", "host-1", List.of(11, 12),
            Map.of("env-11", "env-value-11", "env-12", "env-value-12"), Map.of("label-11", "label-value-11", "label-12", "label-value-12"));
        final RunningService service2 = mockService("service-2", "docker.host/library/image-1", "host-2", List.of(21, 22),
            Map.of("env-21", "env-value-21", "env-22", "env-value-22"), Map.of("label-21", "label-value-21", "label-22", "label-value-22"));
        application.when(BasicApplication::getRunningServices).thenReturn(List.of(service1, service2));
        final var response = controller.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(HttpStatus.OK.value()));
        assertThat(response.getBody()).isNotEmpty().hasSize(2);
      }
    }

    private RunningService mockService(final String name, final String image, final String host, final List<Integer> ports,
        final Map<String, String> env, final Map<String, String> labels) {
      final ConnectionPorts connectionPorts = mock(ConnectionPorts.class);
      when(connectionPorts.getAll()).thenReturn(ports);
      final RunningService service = mock(RunningService.class);
      when(service.name()).thenReturn(name);
      when(service.host()).thenReturn(host);
      when(service.image()).thenReturn(ImageReference.of(image));
      when(service.ports()).thenReturn(connectionPorts);
      when(service.env()).thenReturn(env);
      when(service.labels()).thenReturn(labels);
      return service;
    }
  }
}
