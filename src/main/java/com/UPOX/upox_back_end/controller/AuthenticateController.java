package com.UPOX.upox_back_end.controller;

import com.UPOX.upox_back_end.dto.request.ActivateRequest;
import com.UPOX.upox_back_end.dto.request.AuthenticateRequest;
import com.UPOX.upox_back_end.dto.request.IntrospectRequest;
import com.UPOX.upox_back_end.dto.request.UserUpdateRequest;
import com.UPOX.upox_back_end.dto.response.ApiResponse;
import com.UPOX.upox_back_end.dto.response.AuthenticateResponse;
import com.UPOX.upox_back_end.dto.response.IntrospectResponse;
import com.UPOX.upox_back_end.dto.response.UserResponse;
import com.UPOX.upox_back_end.entity.User;
import com.UPOX.upox_back_end.exception.ErrorCode;
import com.UPOX.upox_back_end.model.Otp;
import com.UPOX.upox_back_end.service.AuthenticateService;
import com.UPOX.upox_back_end.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateController {

    AuthenticateService authenticateService;
    UserService userService;

    @GetMapping("/{userGmail}")
    Object getOtpForChangePassWord(@PathVariable("userGmail") String userGmail){
        //đã có tài khoản rồi
        //trả về:
        //OTP: OTP code
        //ExpiredOTP: OTP sẽ đổi thành mã này sau 5p, nhằm mục đích vô hiệu hoá mã cũ
        //Gôm vào Api response
        if(!userService.isEmailExisted(userGmail)){
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setSucceed(false);
            apiResponse.setStatusCode(ErrorCode.GMAIL_NOT_EXISTED.getCode());
            apiResponse.setMessage(ErrorCode.GMAIL_NOT_EXISTED.getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }
        else{
            Otp otpResponse = authenticateService.getOtpResponse();
            authenticateService.sendMail(userGmail,otpResponse.getOtp());
            ApiResponse<Otp> apiResponse = new ApiResponse<>();
            apiResponse.setResult(otpResponse);
            return apiResponse;
        }
    }

    @PutMapping("/{userId}")
    ApiResponse<Otp> activateAccountByOtp(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest objRequest){
        //Cập nhật mail user vào database
        System.out.println(objRequest);

        UserResponse activatedUser = userService.updateRequest(userId,objRequest);
        Otp otpResponse = authenticateService.getOtpResponse();
        //Gửi mail
        authenticateService.sendMail(activatedUser.getEmail(), otpResponse.getOtp());
        ApiResponse<Otp> apiResponse = new ApiResponse<>();
        apiResponse.setResult(otpResponse);
        return apiResponse;
    }

    @PostMapping("/log-in")
    ApiResponse<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest authenticateRequest){
        var result = authenticateService.authenticate(authenticateRequest); //Nhập đúng tài khoản mật khẩu
        ApiResponse<AuthenticateResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);

        return apiResponse;
    }

    @PostMapping("/activate")
    ApiResponse<AuthenticateResponse> activate (@RequestBody ActivateRequest activateRequest){
        var result = authenticateService.activate(activateRequest); //Xác nhận đã kích hoạt tài khoản chưa?
        ApiResponse<AuthenticateResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);

        return apiResponse;
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest)
            throws ParseException, JOSEException {
        var result = authenticateService.introspect(introspectRequest);
        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);

        return apiResponse;
    }

}
