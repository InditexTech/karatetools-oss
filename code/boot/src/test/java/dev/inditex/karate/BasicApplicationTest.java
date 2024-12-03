package dev.inditex.karate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import dev.inditex.karate.BasicApplication.DockerComposeServicesReadyEventListener;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.lifecycle.DockerComposeServicesReadyEvent;

class BasicApplicationTest {

  @Nested
  class Main {
    @Test
    void when_default_expect_no_exception() {
      final String[] args = {"--spring.docker.compose.enabled=false", "--server.port=0"};

      final ThrowingCallable code = () -> BasicApplication.main(args);

      assertThatNoException().isThrownBy(code);
    }
  }

  @Nested
  class GetRunningServices {
    @BeforeEach
    void beforeEach() {
      BasicApplication.setRunningServices(null);
      final String[] args = {"--spring.docker.compose.enabled=false", "--server.port=0"};
      BasicApplication.main(args);
    }

    @Test
    void when_no_service_ready_event_expect_null() {
      final List<RunningService> runningServices = BasicApplication.getRunningServices();

      assertThat(runningServices).isNull();
    }

    @Test
    void when_empty_service_ready_event_expect_empty() {
      final var event = mock(DockerComposeServicesReadyEvent.class);
      when(event.getRunningServices()).thenReturn(List.of());
      final DockerComposeServicesReadyEventListener dockerComposeServicesReadyEventListener = new DockerComposeServicesReadyEventListener();
      dockerComposeServicesReadyEventListener.onApplicationEvent(event);

      final List<RunningService> runningServices = BasicApplication.getRunningServices();

      assertThat(runningServices).isEmpty();
    }

    @Test
    void when_informed_service_ready_event_expect_informed() {
      final var event = mock(DockerComposeServicesReadyEvent.class);
      final var service1 = mock(RunningService.class);
      final var service2 = mock(RunningService.class);
      when(event.getRunningServices()).thenReturn(List.of(service1, service2));
      final DockerComposeServicesReadyEventListener dockerComposeServicesReadyEventListener = new DockerComposeServicesReadyEventListener();
      dockerComposeServicesReadyEventListener.onApplicationEvent(event);

      final List<RunningService> runningServices = BasicApplication.getRunningServices();

      assertThat(runningServices).hasSize(2).containsExactlyInAnyOrder(service1, service2);
    }
  }
}
