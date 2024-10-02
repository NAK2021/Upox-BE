package com.UPOX.upox_back_end.exception;

import com.UPOX.upox_back_end.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice //Báo cho Spring biết class này sẽ là nơi để aggregate (tập hợp) các Exception để xử lý
public class GlobalExceptionHandler {
    //Define các loại exception sẽ bắt - cách xử lý của nó



    @ExceptionHandler(value = Exception.class) //Catch các Exception không được liệt kê bên dưới

    ResponseEntity<ApiResponse> handlingException(Exception exception){


        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucceed(false);
        apiResponse.setStatusCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }



    @ExceptionHandler(value = RuntimeException.class) //Catch Exception (parameter là kiểu Exception sẽ xử lý)
    //Các Exception có cùng kiểu sẽ đề chạy về đây
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception){
        //Spring sẽ inject exception đang xảy ra vào đây truyền vào parameter "RuntimeException exception"
        //Sẽ access được Exception tại đó để xử lý

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSucceed(false);
        apiResponse.setStatusCode(ErrorCode.USER_EXISTED.getCode());
        apiResponse.setMessage(ErrorCode.USER_EXISTED.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidationException(MethodArgumentNotValidException exception){
        String errorKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(errorKey);
        }catch (IllegalArgumentException ex){
            System.out.println("Unknown key");
        }
        finally {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setSucceed(false);
            apiResponse.setStatusCode(errorCode.getCode());
            apiResponse.setMessage(errorCode.getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

}
