package dev.inditex.karate.controller;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * The Class RunningServiceDTO.
 */
@Getter
@Builder
public class RunningServiceDTO {

  /** The name. */
  private final String name;

  /** The image. */
  private final String image;

  /** The host. */
  private final String host;

  /** The ports. */
  private final List<Integer> ports;

  /** The env. */
  private final Map<String, String> env;

  /** The labels. */
  private final Map<String, String> labels;
}
