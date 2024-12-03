package dev.inditex.karate.controller;

import org.springframework.boot.docker.compose.core.RunningService;

/**
 * The Class RunningServiceMapper.
 */
public class RunningServiceMapper {

  /**
   * Instantiates a new running service mapper.
   */
  private RunningServiceMapper() {
  }

  /**
   * To DTO.
   *
   * @param value the value
   * @return the running service DTO
   */
  public static RunningServiceDTO toDTO(final RunningService value) {
    return RunningServiceDTO.builder()
        .name(value.name())
        .image(value.image().toString())
        .host(value.host())
        .ports(value.ports().getAll())
        .env(value.env())
        .labels(value.labels())
        .build();
  }

}
