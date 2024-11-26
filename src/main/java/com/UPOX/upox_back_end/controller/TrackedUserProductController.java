package com.UPOX.upox_back_end.controller;

import com.UPOX.upox_back_end.dto.request.*;
import com.UPOX.upox_back_end.dto.response.*;
import com.UPOX.upox_back_end.entity.Notification;
import com.UPOX.upox_back_end.entity.TrackedUserProduct;
import com.UPOX.upox_back_end.entity.Transaction;
import com.UPOX.upox_back_end.model.HomePageInformation;
import com.UPOX.upox_back_end.model.WarningCategory;
import com.UPOX.upox_back_end.service.NotificationService;
import com.UPOX.upox_back_end.service.TrackedUserProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api/v1/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TrackedUserProductController {

    @Autowired
    TrackedUserProductService trackedUserProductService;

    @Autowired
    NotificationService notificationService;

    @GetMapping(path = "/myHistory")
    ApiResponse<MyHistoryProductResponse> getMyProductHistory(){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        ApiResponse<MyHistoryProductResponse> apiResponse = new ApiResponse<>();
        var result = trackedUserProductService.getMyProductHistory(userName);
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

    @PostMapping(path = "/addProduct")
    ApiResponse<TrackedUserProductListResponse> addProduct(@RequestBody TrackedUserProductListRequest listRequest){//
//        log.info("Send request successfully");
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        //Transaction
        Transaction newTransaction = trackedUserProductService.createTransaction(userName);

        //List tracked-user products wanted to create
        var res = trackedUserProductService.addProduct(listRequest, newTransaction, userName);


        ApiResponse<TrackedUserProductListResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @PostMapping(path = "/updateCategory")
    void updateCategory(){
        trackedUserProductService.updateProductCategory();
    }

    //Edit --> PUT --> Chỉnh sửa lại thông của 1 sản phẩm

    //Chưa test
    @PutMapping(path = "/finishUsing/{productId}/{transactionId}")
    void finishUsing(@PathVariable("productId") String productId, @PathVariable("transactionId") String transactionId){
        var res = trackedUserProductService.finishUsingProduct(productId,transactionId);
    }

    //Delete --> DELETE --> Xoá một sản phẩm ra khỏi inventory của bạn

    //Chưa test
    @DeleteMapping(path = "/deleteProduct/{productId}/{transactionId}")
    void deleteProduct(@PathVariable("productId") String productId, @PathVariable("transactionId") String transactionId){
        var res = trackedUserProductService.deleteProduct(productId,transactionId);
    }
    //Finish --> PUT --> Sử dụng hoàn tất xong một sản phẩm ĐANG ĐƯỢC SỬ DỤNG

    //Chưa test
    @PutMapping(path = "/updateProduct/{productId}/{transactionId}")
    void updateProduct(@PathVariable("productId") String productId, @PathVariable("transactionId") String transactionId
        ,@RequestBody TrackedUserProductUpdateRequest updateRequest){
        var res = trackedUserProductService.updateProduct(productId,transactionId, updateRequest);
    }

    //Choose using immediately --> GET (boolean) --> Check xem đã có product tương tự đã được sử dụng chưa
    //Đã test
    @GetMapping(path = "/checkProductBeenUsing/{productId}")
    ApiResponse<Boolean> checkProductBeenUsing(@PathVariable("productId") String productId){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var res = trackedUserProductService.isProductBeenUsing(productId, userName);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    @GetMapping("/homePage")
    ApiResponse<HomePageInformation> getHomePageInformation(){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        //get Warning categories (limit 4 categories)
        List<WarningCategory> warningCategories = trackedUserProductService.getWarningCategories(userName);

        //get number of unread notifications
        List<Notification> notifications = notificationService.getNotifications(userName);

        var res = HomePageInformation.builder()
                .warningCategories(warningCategories)
                .notifications(notifications)
                .build();

        ApiResponse<HomePageInformation> apiResponse = new ApiResponse<>();
        apiResponse.setResult(res);
        return apiResponse;
    }

    //Đã test - Chưa test nhiều loại
    //Inventory --> GET: TrackedUserProductListResponse --> Danh sách các product của bạn
    @GetMapping(path = "/getInventory")
    void getInitialInventory(){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var res = trackedUserProductService.getInitialInventory(userName);
    }

    //Hứng parameters để filter (Định nghĩa cái filter bằng ENUM)
    @GetMapping(path = "/getInventory/{searchValue}/{categories}/{status}/{lateness}/{sortBy}/{isAscending}")
    void getFilterInventory(@PathVariable("categories") String categories, @PathVariable("status") String status,
                            @PathVariable("lateness") String lateness, @PathVariable("searchValue") String searchValue,
                            @PathVariable("sortBy") String sortBy, @PathVariable("isAscending") boolean isAscending){

        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        var res = trackedUserProductService.getWithConditionInventory(userName,categories,status,lateness,searchValue,sortBy,isAscending);

    }


    //Calendar page --> GET (tháng, năm)
    //Đã test - Chưa test nhiều loại
    @GetMapping(path = "/getCalendar/{monthYear}")
    //monthYear = mm-yyyy
    void getCalendar(@PathVariable("monthYear") String monthYear){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        String[] monthAndYear = monthYear.split("-");
        int month = Integer.parseInt(monthAndYear[0]); //mm
        int year = Integer.parseInt(monthAndYear[1]); //yyyy

        var res = trackedUserProductService.getCalenderStatus(userName,month,year);

    }

    //Expense page --> GET (tháng, năm)
    //Hứng parameters để filter (tháng, năm)
    //Đã test - Chưa test nhiều loại
    @GetMapping(path = "/getExpense/{monthYear}")
    void getExpense(@PathVariable("monthYear") String monthYear){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        String[] monthAndYear = monthYear.split("-");
        int month = Integer.parseInt(monthAndYear[0]); //mm
        int year = Integer.parseInt(monthAndYear[1]); //yyyy

        trackedUserProductService.getExpense(userName,month,year);

    }


    //CreateExpense --> POST: ExpenseCreateResponse --> Đặt lịch theo rule (1 tháng 1 lần)
    //@Schedule
    @PostMapping(path = "/updateExpense")
    void createMonthlyExpense(){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        trackedUserProductService.createExpense(userName);
    }


    //UpdateStatus --> PUT(<Trống vì sẽ cập nhật tất cả>): TrackedProductListResponse --> Đặt lịch theo rule (mỗi đêm)
    void updateDailyProductStatus() throws ExecutionException, InterruptedException {
        //Update
        List<TrackedUserProduct>  productsNeededToNotify = trackedUserProductService.updateDailyProductStatus();
        //NotificationSending --> POST(NotificationRequest): NotificationResponse
        // --> Bắn thông báo theo lịch (sản phẩm chạm các ngượng đề ra sẽ gọi)
        notificationService.processProductMessage(productsNeededToNotify);
    }

    //
    @GetMapping(path = "/sendAlertSpendTooMuch")
    void sendNotificationSpendTooMuch(){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        notificationService.processOverExpenseMessage(userName);
    }

    @PostMapping(path = "/updateUserFirebaseToken")
    void updateUserFirebaseToken(@RequestBody FirebaseTokenCreateRequest firebaseTokenCreateRequest){
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

        notificationService.updateFirebaseToken(userName,firebaseTokenCreateRequest);
    }

    @GetMapping(path = "/testJson")
    void testReadJsonString(){
        trackedUserProductService.readJsonString();
    }
}
