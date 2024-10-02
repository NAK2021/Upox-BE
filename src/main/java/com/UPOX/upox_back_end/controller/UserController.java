package com.UPOX.upox_back_end.controller;

import com.UPOX.upox_back_end.dto.response.ApiResponse;
import com.UPOX.upox_back_end.dto.request.UserCreationRequest;
import com.UPOX.upox_back_end.dto.request.UserUpdateRequest;
import com.UPOX.upox_back_end.dto.response.UserResponse;
import com.UPOX.upox_back_end.entity.User;
import com.UPOX.upox_back_end.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest objRequest){
        //@Valid khai báo cho framework biết là phải validate obj này theo rule đã set sẵn

        //Bọc kết quả trả về vào trong API response theo 1 chuẩn cụ thể
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createRequest(objRequest));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<User>> getUser(){
        ApiResponse<List<User>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUsers());
        return apiResponse;
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUserByID(userId));
        return apiResponse;
    }

    @PutMapping("{userId}") //update Password
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest objRequest){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateRequest(userId,objRequest));
        return apiResponse;
    }

    @DeleteMapping("{userId}")
    String deleteUser(@PathVariable("userId") String userId){
        userService.deleteRequest(userId);
        return "Delete Successfully";
    }
}
