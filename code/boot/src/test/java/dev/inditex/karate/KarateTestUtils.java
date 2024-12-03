package dev.inditex.karate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.intuit.karate.FileUtils;
import com.intuit.karate.JsonUtils;
import com.intuit.karate.resource.ResourceUtils;

public class KarateTestUtils {
  @SuppressWarnings("unchecked")
  public static Map<Object, Object> readYaml(final String path) throws IOException {
    // equivalent to karate.read for a Yaml file
    final File workingDir = new File("").getAbsoluteFile();
    try (final InputStream resource = ResourceUtils.getResource(workingDir, path).getStream()) {
      final String contents = FileUtils.toString(resource);
      return (Map<Object, Object>) JsonUtils.fromYaml(contents);
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromJson(final String text) {
    // equivalent to karate conversion from json to object
    return (Map<String, Object>) JsonUtils.fromJson(text);
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> fromJsonList(final String text) {
    // equivalent to karate conversion from json to list
    return (List<Map<String, Object>>) JsonUtils.fromJson(text);
  }

}
