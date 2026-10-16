package com.example.beanai.exception;

import com.example.beanai.dto.PredictionResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JUnit 5 - Kiểm thử bộ xử lý ngoại lệ GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Test 1: Bắt lỗi vượt quá dung lượng ảnh (MaxUploadSizeExceededException) -> Trả về 413, rõ lỗi gì, ở đâu, hướng khắc phục")
    void testHandleMaxUploadSizeExceeded() {
        // Given
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(25 * 1024 * 1024);

        // When
        ResponseEntity<PredictionResultDTO> response = exceptionHandler.handleMaxUploadSizeExceeded(exception);

        // Then
        assertNotNull(response, "Response không được null");
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode(), "Mã trạng thái phải là 413 Payload Too Large");

        PredictionResultDTO body = response.getBody();
        assertNotNull(body, "Body không được null");
        assertFalse(body.isSuccess(), "Trạng thái success phải là false");
        assertEquals("Ảnh vượt quá dung lượng cho phép", body.getErrorTitle(), "Tên lỗi (Lỗi gì) phải chính xác");
        assertEquals("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)", body.getErrorLocation(), "Vị trí phát sinh (Ở đâu) phải chính xác");
        assertTrue(body.getError().contains("25MB"), "Nội dung lỗi phải nêu rõ giới hạn 25MB");
        assertNotNull(body.getSuggestion(), "Phải có hướng dẫn khắc phục");
        assertFalse(body.getError().contains("Exception"), "Tuyệt đối không để lộ tên ngoại lệ Java ra ngoài");
    }

    @Test
    @DisplayName("Test 2: Bắt lỗi gián đoạn truyền tải tệp (MultipartException) -> Trả về 400, thông điệp thân thiện")
    void testHandleMultipartException() {
        // Given
        MultipartException exception = new MultipartException("Connection reset by peer");

        // When
        ResponseEntity<PredictionResultDTO> response = exceptionHandler.handleMultipartException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Mã trạng thái phải là 400 Bad Request");
        PredictionResultDTO body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Lỗi truyền tải tệp dữ liệu", body.getErrorTitle());
        assertEquals("Bộ giải mã Multipart máy chủ (Servlet Multipart Resolver)", body.getErrorLocation());
        assertNotNull(body.getSuggestion());
        assertFalse(body.getError().contains("Connection reset by peer"), "Không được để lộ thông báo ngoại lệ thô của hệ thống");
    }

    @Test
    @DisplayName("Test 3: Bắt lỗi tham số không hợp lệ (IllegalArgumentException) -> Trả về 400 với thông điệp kiểm định")
    void testHandleIllegalArgumentException() {
        // Given
        String validationMsg = "Tên tệp không hợp lệ hoặc rỗng";
        IllegalArgumentException exception = new IllegalArgumentException(validationMsg);

        // When
        ResponseEntity<PredictionResultDTO> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        PredictionResultDTO body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Dữ liệu đầu vào không hợp lệ", body.getErrorTitle());
        assertEquals("Bộ kiểm định tham số hệ thống (Request Validator)", body.getErrorLocation());
        assertEquals(validationMsg, body.getError());
    }

    @Test
    @DisplayName("Test 4: Bắt tất cả các lỗi hệ thống không lường trước (Exception) -> Trả về 500, ẩn hoàn toàn mã lỗi / stack trace")
    void testHandleGeneralException() {
        // Given
        Exception unexpectedException = new NullPointerException("Null reference at line 142 in internal logic");

        // When
        ResponseEntity<PredictionResultDTO> response = exceptionHandler.handleGeneralException(unexpectedException);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Mã trạng thái phải là 500");
        PredictionResultDTO body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Sự cố hệ thống máy chủ BeanAI", body.getErrorTitle());
        assertEquals("Trung tâm xử lý ứng dụng (Application Core Service)", body.getErrorLocation());
        assertFalse(body.getError().contains("NullPointerException"), "Tuyệt đối không hiển thị NullPointerException cho người dùng");
        assertFalse(body.getError().contains("line 142"), "Tuyệt đối không hiển thị số dòng code lỗi trong thông báo");
        assertNotNull(body.getSuggestion());
    }
}
