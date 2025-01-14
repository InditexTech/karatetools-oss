package dev.inditex.karate.controller;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import dev.inditex.karate.openapitest.dto.ErrorDTO;
import dev.inditex.karate.openapitest.dto.ItemDTO;
import dev.inditex.karate.openapitest.service.BasicApiInterface;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * The Class BasicApiController.
 */
@RestController
public class BasicApiController implements BasicApiInterface {

  /** The items. */
  protected SortedMap<Integer, ItemDTO> items = new TreeMap<>(Map.of(
      1, new ItemDTO().id(1).name("Item1").tag("Tag1"),
      2, new ItemDTO().id(2).name("Item2").tag("Tag2"),
      3, new ItemDTO().id(3).name("Item3").tag("Tag3")));

  /**
   * Creates the items.
   *
   * @param itemDTO the item DTO
   * @return the response entity
   */
  @Override
  public ResponseEntity<ItemDTO> createItems(@Valid final ItemDTO itemDTO) {
    authz();
    items.put(itemDTO.getId(), itemDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(itemDTO);
  }

  /**
   * List items.
   *
   * @param limit the limit
   * @return the response entity
   */
  @Override
  public ResponseEntity<List<ItemDTO>> listItems(@Max(100) @Valid final Integer limit) {
    authz();
    if (limit == null) {
      throw new IllegalArgumentException("Limit is required");
    }
    return ResponseEntity.ok(items.entrySet().stream().limit(limit).map(Entry::getValue).toList());
  }

  /**
   * Show item by id.
   *
   * @param itemId the item id
   * @return the response entity
   */
  @Override
  public ResponseEntity<ItemDTO> showItemById(final Integer itemId) {
    authz();
    if (!items.containsKey(itemId)) {
      throw new NotFoundException(String.format("Item with id %d not found", itemId));
    }
    return ResponseEntity.ok(items.get(itemId));
  }

  /**
   * Delete all items.
   * 
   * @return the response entity
   */
  @Override
  public ResponseEntity<Void> deleteAllItems() {
    authz();
    items.clear();
    return ResponseEntity.noContent().build();
  }

  /**
   * Sets the items.
   *
   * @param items the items
   */
  public void setItems(final SortedMap<Integer, ItemDTO> items) {
    this.items = items;
  }

  /**
   * Authz.
   */
  protected void authz() {
    final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      throw new UnauthorizedException("Unauthorized");
    }
    final String auth = requestAttributes.getRequest().getHeader("Authorization");
    // null or blank auth header or invalid bearer token or invalid basic auth
    if (auth == null || auth.isBlank() || Boolean.TRUE.equals(!isValidJWT(auth) && !isValidBasic(auth))) {
      throw new UnauthorizedException("Unauthorized");
    }
  }

  /**
   * JWT token for username100.
   *
   * @param auth the auth
   * @return the boolean
   */
  protected Boolean isValidJWT(final String auth) {
    final String validToken = "Bearer "
        + "eyJhbGciOiJIUzI1NiIsImtpZCI6InRlc3QiLCJ0eXAiOiJKV1"
        + "QifQ.eyJleHAiOjIxNDc0ODM2NDcsImlhdCI6MTcwNDA2NzIwM"
        + "CwiaWQiOiIxMjM0IiwiaXNzIjoiaHR0cHM6Ly93d3cuaW5kaXR"
        + "leC5jb20vand0LXRva2VuIiwic3ViIjoidXNlcm5hbWUxMDAif"
        + "Q.qXP_iGhZSgFTVZtup9dYFont8QjLH8x2q2UDlQM0tIY";
    return auth.equals(validToken);
  }

  /**
   * Basic auth for username100:username100p.
   *
   * @param auth the auth
   * @return the boolean
   */
  protected Boolean isValidBasic(final String auth) {
    final String validBasic = "Basic "
        + "dXNlcm5hbWUxMDA6dXNlcm5hbWUxMDBw";
    return auth.equals(validBasic);
  }

  /**
   * Handle bad request.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler({
      HttpMessageNotReadableException.class,
      MethodArgumentNotValidException.class,
      MethodArgumentTypeMismatchException.class,
      ConstraintViolationException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<ErrorDTO> handleBadRequest(final Exception exception) {
    final ErrorDTO error = generateErrorDetail(HttpStatus.BAD_REQUEST, exception);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle not found.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler({
      NotFoundException.class
  })
  public ResponseEntity<ErrorDTO> handleNotFound(final Exception exception) {
    final ErrorDTO error = generateErrorDetail(HttpStatus.NOT_FOUND, exception);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  /**
   * Handle unauthorized.
   *
   * @param exception the exception
   * @return the response entity
   */
  @ExceptionHandler({
      UnauthorizedException.class
  })
  public ResponseEntity<ErrorDTO> handleUnauthorized(final Exception exception) {
    final ErrorDTO error = generateErrorDetail(HttpStatus.UNAUTHORIZED, exception);
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Generate error detail.
   *
   * @param code the code
   * @param exception the exception
   * @return the error DTO
   */
  protected static ErrorDTO generateErrorDetail(final HttpStatus code, final Exception exception) {
    final ErrorDTO item = new ErrorDTO();
    item.setCode(code.value());
    item.setMessage(code.getReasonPhrase());
    item.setStack(exception.getMessage());
    return item;
  }
}

class NotFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public NotFoundException(final String message) {
    super(message);
  }
}

class UnauthorizedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public UnauthorizedException(final String message) {
    super(message);
  }
}
