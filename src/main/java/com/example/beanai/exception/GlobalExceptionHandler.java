package com.example.beanai.exception;

import com.example.beanai.dto.PredictionResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * Bộ xử lý ngoại lệ toàn cục cho các API của Bean AI.
 * Đảm bảo các thông báo lỗi luôn nêu rõ:
 * 1. Lỗi gì (Sự cố cụ thể)
 * 2. Ở đâu (Vị trí phát sinh)
 * 3. Hướng khắc phục gợi ý
 * Tuyệt đối không trả về chuỗi mã lỗi kỹ thuật hoặc stack trace ra ngoài cho người dùng.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Bắt lỗi khi người dùng tải lên tệp vượt quá dung lượng tối đa cho phép
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<PredictionResultDTO> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Kích thước tệp tải lên vượt quá giới hạn: {}", ex.getMessage());

        PredictionResultDTO errorResponse = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Ảnh vượt quá dung lượng cho phép")
                .errorLocation("Cổng tiếp nhận tệp của máy chủ (Web Server Upload Gate)")
                .error("Kích thước tệp hình ảnh lá cây vượt quá giới hạn tối đa cho phép (25MB).")
                .suggestion("Vui lòng chọn ảnh có dung lượng dưới 25MB hoặc nén giảm dung lượng ảnh trước khi phân tích.")
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * Bắt lỗi liên quan đến quá trình truyền tải Multipart file
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<PredictionResultDTO> handleMultipartException(MultipartException ex) {
        log.error("Lỗi quá trình tiếp nhận dữ liệu tệp tải lên: ", ex);

        PredictionResultDTO errorResponse = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Lỗi truyền tải tệp dữ liệu")
                .errorLocation("Bộ giải mã Multipart máy chủ (Servlet Multipart Resolver)")
                .error("Quá trình truyền tải tệp hình ảnh lên máy chủ bị gián đoạn hoặc định dạng tệp bị lỗi.")
                .suggestion("Vui lòng kiểm tra lại đường truyền mạng và thực hiện tải ảnh lại.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bắt lỗi đối số không hợp lệ
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PredictionResultDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Tham số yêu cầu không hợp lệ: {}", ex.getMessage());

        PredictionResultDTO errorResponse = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Dữ liệu đầu vào không hợp lệ")
                .errorLocation("Bộ kiểm định tham số hệ thống (Request Validator)")
                .error(ex.getMessage() != null ? ex.getMessage() : "Dữ liệu yêu cầu gửi lên không đúng định dạng quy định.")
                .suggestion("Vui lòng kiểm tra lại hình ảnh và thao tác theo đúng hướng dẫn trên màn hình.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bắt tất cả các lỗi hệ thống không mong muốn khác
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PredictionResultDTO> handleGeneralException(Exception ex) {
        log.error("Sự cố hệ thống không mong muốn: ", ex);

        PredictionResultDTO errorResponse = PredictionResultDTO.builder()
                .success(false)
                .errorTitle("Sự cố hệ thống máy chủ BeanAI")
                .errorLocation("Trung tâm xử lý ứng dụng (Application Core Service)")
                .error("Máy chủ gặp sự cố trong quá trình tiếp nhận hoặc phân tích dữ liệu.")
                .suggestion("Vui lòng làm mới trang (F5) hoặc thử lại sau giây lát.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
