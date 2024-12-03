package dev.inditex.karate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@Tag("IT")
@ActiveProfiles({"test-mvc"})
@SpringBootTest
@AutoConfigureMockMvc
public class BasicApiControllerIT {

  @Autowired
  private MockMvc mvc;

  @Nested
  class BasicApi {
    @Test
    void when_basic_api_calls_expect_success() throws Exception {

      // Create item Unauthorized
      final var createItemUnauthorized = mvc.perform(post("/items")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"id\": 4, \"name\": \"Item4\", \"tag\": \"Tag4\"}"))
          .andExpect(status().isUnauthorized())
          .andReturn();

      assertThat((Integer) JsonPath.parse(createItemUnauthorized.getResponse().getContentAsString()).read("$.code"))
          .isEqualTo(401);
      assertThat((String) JsonPath.parse(createItemUnauthorized.getResponse().getContentAsString()).read("$.message"))
          .isEqualTo("Unauthorized");
      assertThat((String) JsonPath.parse(createItemUnauthorized.getResponse().getContentAsString()).read("$.stack"))
          .isEqualTo("Unauthorized");

      // Create item
      final var createItem = mvc.perform(post("/items")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"id\": 4, \"name\": \"Item4\", \"tag\": \"Tag4\"}")
          .header("Authorization", "Basic dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw"))
          .andExpect(status().isCreated())
          .andReturn();

      assertThat((Integer) JsonPath.parse(createItem.getResponse().getContentAsString()).read("$.id")).isEqualTo(4);
      assertThat((String) JsonPath.parse(createItem.getResponse().getContentAsString()).read("$.name")).isEqualTo("Item4");
      assertThat((String) JsonPath.parse(createItem.getResponse().getContentAsString()).read("$.tag")).isEqualTo("Tag4");

      // List items
      final var listItemsInvalid = mvc.perform(get("/items")
          .header("Authorization", "Basic dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw"))
          .andExpect(status().isBadRequest())
          .andReturn();

      assertThat((Integer) JsonPath.parse(listItemsInvalid.getResponse().getContentAsString()).read("$.code"))
          .isEqualTo(400);
      assertThat((String) JsonPath.parse(listItemsInvalid.getResponse().getContentAsString()).read("$.message"))
          .isEqualTo("Bad Request");
      assertThat((String) JsonPath.parse(listItemsInvalid.getResponse().getContentAsString()).read("$.stack"))
          .isEqualTo("Limit is required");

      // List items
      final var listItems = mvc.perform(get("/items")
          .param("limit", "10")
          .header("Authorization", "Basic dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw"))
          .andExpect(status().isOk())
          .andReturn();

      assertThat((Integer) JsonPath.parse(listItems.getResponse().getContentAsString()).read("$.length()")).isEqualTo(4);

      // Show item NotFound
      final var showItemNotFound = mvc.perform(get("/items/5")
          .header("Authorization", "Basic dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw"))
          .andExpect(status().isNotFound())
          .andReturn();

      assertThat((Integer) JsonPath.parse(showItemNotFound.getResponse().getContentAsString()).read("$.code")).isEqualTo(404);
      assertThat((String) JsonPath.parse(showItemNotFound.getResponse().getContentAsString()).read("$.message"))
          .isEqualTo("Not Found");
      assertThat((String) JsonPath.parse(showItemNotFound.getResponse().getContentAsString()).read("$.stack"))
          .isEqualTo("Item with id 5 not found");

      // Show item
      final var showItem =
          mvc.perform(get("/items/4")
              .header("Authorization", "Basic dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw"))
              .andExpect(status().isOk())
              .andReturn();

      assertThat((Integer) JsonPath.parse(showItem.getResponse().getContentAsString()).read("$.id")).isEqualTo(4);
      assertThat((String) JsonPath.parse(showItem.getResponse().getContentAsString()).read("$.name")).isEqualTo("Item4");
      assertThat((String) JsonPath.parse(showItem.getResponse().getContentAsString()).read("$.tag")).isEqualTo("Tag4");
    }
  }
}
