package dev.inditex.karate.openapi.data;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * The Class MavenUtils.
 */
@Slf4j
public class MavenUtils {

  /**
   * Instantiates a new maven utils.
   */
  protected MavenUtils() {
  }

  /**
   * Gets the pom artifacts.
   *
   * @param path the path
   * @return the pom artifacts
   */
  public static Map<String, MavenArtifact> getPomArtifacts(final String path) {
    final Map<String, MavenArtifact> artifacts = new TreeMap<>();
    try (final FileReader pom = new FileReader(path, StandardCharsets.UTF_8)) {
      final MavenXpp3Reader reader = new MavenXpp3Reader();
      final Model model = reader.read(pom);
      artifacts.put(model.getGroupId() + ":" + model.getArtifactId(), new MavenArtifact(model.getGroupId(), model.getArtifactId()));
      model.getDependencies().forEach(
          d -> artifacts.put(d.getGroupId() + ":" + d.getArtifactId(), new MavenArtifact(d.getGroupId(), d.getArtifactId())));
    } catch (IOException | XmlPullParserException e) {
      log.error("getPomArtifacts({}) Exception [{}]", path, e.getMessage(), e);
    }
    return artifacts;
  }
}
