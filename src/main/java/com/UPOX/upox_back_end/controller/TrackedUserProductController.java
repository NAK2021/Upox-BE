package com.UPOX.upox_back_end.controller;

import com.UPOX.upox_back_end.dto.request.*;
import com.UPOX.upox_back_end.dto.response.*;
import com.UPOX.upox_back_end.entity.Transaction;
import com.UPOX.upox_back_end.service.TrackedUserProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TrackedUserProductController {

    @Autowired
    TrackedUserProductService trackedUserProductService;
    @GetMapping(path = "/myHistory")
    ApiResponse<MyHistoryProductResponse> getMyProductHistory(){

        ApiResponse<MyHistoryProductResponse> apiResponse = new ApiResponse<>();
        var result = trackedUserProductService.getMyProductHistory();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping(path = "/checkExist")
    ApiResponse<ProductResponse> checkProductExist(@RequestBody CheckProductExistRequest objRequest){

        ApiResponse<ProductResponse> apiResponse = new ApiResponse<>();
        var result = trackedUserProductService.checkProductExist(objRequest);
        apiResponse.setResult(result);
        return apiResponse;
    }

//    @PostMapping(path = "/create")
//    ApiResponse<TrackedUserProductListResponse> addProduct(@RequestBody TrackedUserProductListRequest listRequest){
////        log.info("Send request successfully");
//        ApiResponse<TrackedUserProductListResponse> apiResponse = new ApiResponse<>();
//        var result = trackedUserProductService.addProduct(listRequest);
//        apiResponse.setResult(result);
//        return apiResponse;
////        return null;
//    }

    @PostMapping(path = "/updateCategory")
    void updateCategory(@RequestBody TrackedUserProductRequest objRequest){
//        log.info("Send request successfully");
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        Transaction newTransaction = trackedUserProductService.createCategory();

        //Xử lý mảng
        //Truyền từng request vào takeInfoProduct
        var res = trackedUserProductService.takeInfoProduct(objRequest, newTransaction.getTransactionId(),userName);
//        return null;
    }




}
