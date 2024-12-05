package dev.inditex.karate.openapi.data;

import java.util.Arrays;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The Class MavenArtifact.
 */
@Data
@Builder
public class MavenArtifact {

  /** The group id. */
  private final String groupId;

  /** The artifact id. */
  private final String artifactId;

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return groupId + ":" + artifactId;
  }

  /**
   * From id.
   *
   * @param id the id
   * @return the maven artifact
   */
  public static MavenArtifact fromId(final String id) {
    if (id.indexOf(":") >= 0 && id.split(":").length == 2) {
      return new MavenArtifact(id.split(":")[0], id.split(":")[1]);
    }
    return null;
  }

  /**
   * Artifact id to path.
   *
   * @return the string
   */
  public String artifactIdToPath() {
    return toPath(getArtifactId());
  }

  /**
   * To path.
   *
   * @return the string
   */
  public String toPath() {
    return toPath(getId());
  }

  /**
   * To path.
   *
   * @param value the value
   * @return the string
   */
  protected static String toPath(final String value) {
    return value != null ? FilenameUtils.separatorsToSystem(
        value.toLowerCase()
            // replace all special chars except "-", group separator (.) and artifact separator (:)
            .replaceAll("[^-.:a-zA-Z]+", "")
            // replace group separator (.) and artifact separator (:) to folder
            .replaceAll("[.:]", "/"))
        : null;
  }

  /**
   * To package.
   *
   * @return the string
   */
  public String toPackage() {
    return toPackage(getId());
  }

  /**
   * To package.
   *
   * @param value the value
   * @return the string
   */
  protected static String toPackage(final String value) {
    return value != null ? FilenameUtils.separatorsToSystem(
        value.toLowerCase()
            // replace all special chars except group separator (.) and artifact separator (:)
            .replaceAll("[^.:a-zA-Z]+", "")
            // replace group separator (.) and artifact separator (:) to folder
            .replaceAll("[.:]", "/"))
        : null;
  }

  /**
   * To api url.
   *
   * @return the string
   */
  public String toApiUrl() {
    if (artifactId != null) {
      return StringUtils.uncapitalize(Arrays.asList(artifactId.toLowerCase().split("[ _-]")).stream().map(
          StringUtils::capitalize).collect(Collectors.joining())) + "Url";
    }
    return "testApiUrl";
  }

}
