package dev.inditex.karate.controller;

import java.util.List;

import dev.inditex.karate.BasicApplication;

import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class DockerHealthController.
 */
@RestController
public class DockerHealthController {

  /**
   * Health.
   *
   * @return the response entity
   */
  @GetMapping(value = "/health/docker")
  public ResponseEntity<List<RunningServiceDTO>> health() {
    final List<RunningService> runningServices = BasicApplication.getRunningServices();
    if (runningServices == null) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE.value()).build();
    }
    return ResponseEntity.ok(runningServices.stream().map(RunningServiceMapper::toDTO).toList());
  }
}
