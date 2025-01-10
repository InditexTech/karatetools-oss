package dev.inditex.karate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import dev.inditex.karate.openapitest.dto.ErrorDTO;
import dev.inditex.karate.openapitest.dto.ItemDTO;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class BasicApiControllerTest {

  protected BasicApiController controller;

  @BeforeEach
  protected void beforeEach() {
    controller = new BasicApiController();
    final SortedMap<Integer, ItemDTO> items = new TreeMap<>();
    items.put(1, new ItemDTO().id(1).name("Item 1").tag("Tag 1"));
    items.put(2, new ItemDTO().id(2).name("Item 2").tag("Tag 2"));
    items.put(3, new ItemDTO().id(3).name("Item 3").tag("Tag 3"));
    controller.setItems(items);

    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", getValidBasicAuth());
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Nested
  class CreateItems {
    @Test
    void when_item_does_not_exist_expect_create_item() {
      final ItemDTO item = new ItemDTO().id(4).name("Item 4").tag("Tag 4");

      final ResponseEntity<ItemDTO> response = controller.createItems(item);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(item);
    }

    @Test
    void when_item_exists_expect_update_item() {
      final ItemDTO item = new ItemDTO().id(1).name("Item 1 Updated").tag("Tag 1 Updated");

      final ResponseEntity<ItemDTO> response = controller.createItems(item);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(item);
    }
  }

  @Nested
  class ListItems {
    @Test
    void when_limit_is_lower_than_items_expect_return_items_up_to_limit() {

      final ResponseEntity<List<ItemDTO>> response = controller.listItems(2);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(2);
      assertThat(response.getBody().get(0).getId()).isEqualTo(1);
      assertThat(response.getBody().get(0).getName()).isEqualTo("Item 1");
      assertThat(response.getBody().get(0).getTag()).isEqualTo("Tag 1");
      assertThat(response.getBody().get(1).getId()).isEqualTo(2);
      assertThat(response.getBody().get(1).getName()).isEqualTo("Item 2");
      assertThat(response.getBody().get(1).getTag()).isEqualTo("Tag 2");
    }

    @Test
    void when_limit_is_higher_than_items_expect_return_all_items() {

      final ResponseEntity<List<ItemDTO>> response = controller.listItems(10);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(3);
      assertThat(response.getBody().get(0).getId()).isEqualTo(1);
      assertThat(response.getBody().get(0).getName()).isEqualTo("Item 1");
      assertThat(response.getBody().get(0).getTag()).isEqualTo("Tag 1");
      assertThat(response.getBody().get(1).getId()).isEqualTo(2);
      assertThat(response.getBody().get(1).getName()).isEqualTo("Item 2");
      assertThat(response.getBody().get(1).getTag()).isEqualTo("Tag 2");
      assertThat(response.getBody().get(2).getId()).isEqualTo(3);
      assertThat(response.getBody().get(2).getName()).isEqualTo("Item 3");
      assertThat(response.getBody().get(2).getTag()).isEqualTo("Tag 3");
    }
  }

  @Nested
  class ShowItemById {
    @Test
    void when_item_exists_expect_return_item() {

      final ResponseEntity<ItemDTO> response = controller.showItemById(2);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().getId()).isEqualTo(2);
      assertThat(response.getBody().getName()).isEqualTo("Item 2");
      assertThat(response.getBody().getTag()).isEqualTo("Tag 2");
    }

    @Test
    void when_item_does_not_exist_expect_throw_not_found_exception() {
      assertThatThrownBy(() -> controller.showItemById(4))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Item with id 4 not found");
    }
  }

  @Nested
  class DeleteAllItems {
    @Test
    void when_items_exist_expect_no_content_all_items_deleted() {

      final ResponseEntity<Void> response = controller.deleteAllItems();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(controller.items).isEmpty();
    }

    @Test
    void when_items_does_not_exist_expect_no_content() {
      controller.items.clear();

      final ResponseEntity<Void> response = controller.deleteAllItems();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      assertThat(controller.items).isEmpty();
    }
  }

  @Nested
  class Authz {
    @Test
    void when_request_attributes_is_null_expect_throw_unauthorized_exception() {
      RequestContextHolder.setRequestAttributes(null);

      assertThatThrownBy(() -> controller.authz())
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Unauthorized");
    }

    @Test
    void when_auth_is_null_expect_throw_unauthorized_exception() {
      final MockHttpServletRequest request = new MockHttpServletRequest();
      RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

      assertThatThrownBy(() -> controller.authz())
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Unauthorized");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "Basic invalid", "Bearer invalid"})
    void when_auth_is_invalid_expect_throw_unauthorized_exception(final String authz) {
      final MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader("Authorization", authz);
      RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

      assertThatThrownBy(() -> controller.authz())
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Unauthorized");
    }

    @Test
    void when_basic_auth_is_valid_expect_no_exception() {
      final MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader("Authorization", getValidBasicAuth());
      RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

      assertThatCode(() -> controller.authz()).doesNotThrowAnyException();
    }

    @Test
    void when_bearer_auth_is_valid_expect_no_exception() {
      final MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader("Authorization", getValidBearerAuth());
      RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

      assertThatCode(() -> controller.authz()).doesNotThrowAnyException();
    }
  }

  @Nested
  class HandleBadRequest {

    @ParameterizedTest
    @MethodSource
    void when_exception_is_for_bad_request_expect_400(final Exception exception, final String expected) {
      final ResponseEntity<ErrorDTO> response = controller.handleBadRequest(exception);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
      assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
      assertThat(response.getBody().getMessage()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
      assertThat(response.getBody().getStack()).contains(expected);
    }

    private static Stream<Arguments> when_exception_is_for_bad_request_expect_400() throws NoSuchMethodException, SecurityException {
      final var method = ExceptionMANVE.class.getMethod("test");
      final var methodParameter = new MethodParameter(method, -1);
      final var binder = new DataBinder(new Object());
      final var bindingResult = binder.getBindingResult();
      final var exceptionMANVE = new MethodArgumentNotValidException(methodParameter, bindingResult);
      final var exceptionHMNRE = new HttpMessageNotReadableException("Bad Request (HttpMessageNotReadableException)", null, null);
      final var exceptionCVE = new ConstraintViolationException("Bad Request (ConstraintViolationException)", null);
      final var exceptionIAE = new IllegalArgumentException("Bad Request (IllegalArgumentException)");
      final var exceptionMATE = new MethodArgumentTypeMismatchException(
          "Bad Request (MethodArgumentTypeMismatchException)", Integer.class, "test", methodParameter, null);

      return Stream.of(
          Arguments.of(exceptionMANVE, "Validation failed for argument [-1]"),
          Arguments.of(exceptionHMNRE, "Bad Request (HttpMessageNotReadableException)"),
          Arguments.of(exceptionCVE, "Bad Request (ConstraintViolationException)"),
          Arguments.of(exceptionIAE, "Bad Request (IllegalArgumentException)"),
          Arguments.of(exceptionMATE, "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'"));
    }

    static class ExceptionMANVE {
      public void test() {
        throw new UnsupportedOperationException();
      }
    }
  }

  @Nested
  class HandleNotFound {

    @Test
    void when_exception_is_not_found_exception_expect_return_not_found() {
      final ResponseEntity<ErrorDTO> response = controller.handleNotFound(new NotFoundException("Not Found"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
      assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
      assertThat(response.getBody().getMessage()).isEqualTo(HttpStatus.NOT_FOUND.getReasonPhrase());
      assertThat(response.getBody().getStack()).isEqualTo("Not Found");
    }
  }

  @Nested
  class HandleUnauthorized {

    @Test
    void when_exception_is_unauthorized_exception_expect_return_unauthorized() {
      final ResponseEntity<ErrorDTO> response = controller.handleUnauthorized(new UnauthorizedException("Unauthorized"));

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
      assertThat(response.getBody().getMessage()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
      assertThat(response.getBody().getStack()).isEqualTo("Unauthorized");
    }

  }

  /**
   * Basic auth for username100:username100p.
   */
  protected String getValidBasicAuth() {
    return "Basic " + "dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw";
  }

  /**
   * JWT token for username100.
   */
  protected String getValidBearerAuth() {
    return "Bearer "
        + "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1"
        + "QifQ.eyJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwM"
        + "CwiaWQiOiIxMjM0IiwiaXNzIjoiaHR0cHM6Ly93d3cuaW5kaXR"
        + "leC5jb20vand0LXRva2VuIiwic3ViIjoidXNlcm5hbWUxMDAif"
        + "Q.qXP_iGhZSgFTVZtup9dYFont8QjLH8x2q2UDlQM0tIY";
  }
}
