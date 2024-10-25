package com.UPOX.upox_back_end.service;


import com.UPOX.upox_back_end.dto.request.CheckProductExistRequest;
import com.UPOX.upox_back_end.dto.request.TrackedUserProductListRequest;
import com.UPOX.upox_back_end.dto.request.TrackedUserProductRequest;
import com.UPOX.upox_back_end.dto.response.MyHistoryProductResponse;
import com.UPOX.upox_back_end.dto.response.ProductResponse;
import com.UPOX.upox_back_end.dto.response.TrackedUserProductListResponse;
import com.UPOX.upox_back_end.dto.response.TrackedUserProductResponse;
import com.UPOX.upox_back_end.entity.*;
import com.UPOX.upox_back_end.enums.StatusE;
import com.UPOX.upox_back_end.exception.ErrorCode;
import com.UPOX.upox_back_end.model.IdClass.TrackedUserProductID;
import com.UPOX.upox_back_end.model.TrackedUserProductOpened;
import com.UPOX.upox_back_end.repository.*;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
@Slf4j
public class TrackedUserProductService {

    @Autowired
    private TrackedUserProductRepository trackedUserProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;


//    @Transactional
//    public void saveOrUpdate(TrackedUserProduct entity) {
//        if (entity.isNew()) {
//            entity.setNew(false); // Update the status to existing
//            trackedUserProductRepository.save(entity);
//        } else {
//            trackedUserProductRepository.save(entity); // Will invoke merge operation
//        }
//    }


    public MyHistoryProductResponse getMyProductHistory() {
        return null;
    }

    public ProductResponse checkProductExist(CheckProductExistRequest request) {
        //Xử lý chuỗi
        //Tìm trên DB

        List<Product> listDefProduct = productRepository.findAll();

        Product foundProduct = new Product();
        for (var product : listDefProduct) {
            log.info(product.getProductName());
            if (request.getProductName().contains(product.getProductName())) {
                foundProduct = product;
                break;
            }
        }


        return ProductResponse.builder()
                .productName(foundProduct.getProductName())
                .defPreserveWay(foundProduct.getDefPreserveWay())
                .defCost(foundProduct.getDefCost())
                .defExpiryDate(foundProduct.getDefExpiryDate())
                .defVolume(foundProduct.getDefVolume())
                .build();
    }


    @Transactional
    public TrackedUserProductListResponse addProduct(TrackedUserProductListRequest listRequest, Transaction newTransaction) {
        //Lấy user đang truy cập thông qua token
        var context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();

//        List<Category> categoryList = categoryRepository.findAll();
//        Product product = productRepository.findByProductName("Kem rửa mặt")
//                .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));


        TrackedUserProductListResponse listResponse = new TrackedUserProductListResponse();
        for (var request : listRequest.getRequestList()) {
            var response = takeInfoProduct(request, newTransaction.getTransactionId(), userName);
            listResponse.addResponse(response);
        }
        return listResponse;
    }


    public TrackedUserProductResponse takeInfoProduct(TrackedUserProductRequest request, String transactionId, String userName) {

        try {
            List<Category> categoryList = categoryRepository.findAll();
            //Primary Key
            User user = userRepository.findByUsername(userName)
                    .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage()));

            Product product = productRepository.findByProductName(request.getProductName())
                    .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

            Transaction lookUpTransaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));


            //Foreign key
            Status curStatus = statusRepository.findByStatusProductName(StatusE.NORMAL.name())
                    .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));


            //Take from product
            Category lookUpCategory = product.getCategory();


            //Setup Product Info
            TrackedUserProduct newTrackedUserProduct = TrackedUserProduct.builder()
//                .user(user) //Tạo Connection
//                .transaction(lookUpTransaction) //Tạo Connection
//                .product(product) //Tạo Connection
//                .status(curStatus) //Tạo Connection
                    .dateBought(parseIntoLocalDate(request.getDateBought()))
                    .expiryDate(parseIntoLocalDate(request.getExpiryDate()))
                    .dateStatusChange(LocalDateTime.now())
                    .peopleUse(request.getPeopleUse())
                    .quantity(request.getQuantity())
                    .volume(request.getVolume())
                    .cost(request.getCost())
                    .frequency(request.getFrequency())
                    .wayPreserve(request.getWayPreserve())
                    .isNew(true)
                    .build();


            //Tạo Connection
            user.addTrackedUserProduct(newTrackedUserProduct);
            product.addTrackedUserProduct(newTrackedUserProduct);
            curStatus.addTrackedUserProduct(newTrackedUserProduct);
            lookUpTransaction.addTrackedUserProduct(newTrackedUserProduct);

            String s = "";
            s += "\nProduct_id1:" + newTrackedUserProduct.getProduct().getId();
            s += "\nUser1:" + newTrackedUserProduct.getUser().getId();
            s += "\nTransaction1:" + newTrackedUserProduct.getTransaction().getTransactionId();
            System.err.println(s);

//        saveOrUpdate(newTrackedUserProduct);

            //Save vào db
            trackedUserProductRepository.save(newTrackedUserProduct);

            //Nếu sản phẩm được sử dụng ngay
            TrackedUserProductOpened newTrackedUserProductOpened = new TrackedUserProductOpened();
            if (request.isOpened()) {
                newTrackedUserProduct.setOpened(true);
                newTrackedUserProduct.setNumProductOpened(request.getNumProductOpened());
                newTrackedUserProduct.setDateOpen(parseIntoLocalDate(request.getDateOpen()));
                newTrackedUserProduct.setVolumeLeft(request.getVolume());


                newTrackedUserProductOpened.setNumProductOpened(request.getNumProductOpened());
                newTrackedUserProductOpened.setDateOpen(parseIntoLocalDate(request.getDateOpen()));
                newTrackedUserProductOpened.setVolumeLeft(request.getVolume());
            } else {
                newTrackedUserProductOpened.setNumProductOpened(0);
                newTrackedUserProductOpened.setDateOpen(null);
                newTrackedUserProductOpened.setVolumeLeft(request.getVolume());
            }


            //Tạo TrackedUserProductResponse
            return TrackedUserProductResponse.builder()
                    .productName(product.getProductName())
                    .statusName(curStatus.getStatusProductName())
                    .categoryName(lookUpCategory.getCategoryName())
                    .expiryDate(newTrackedUserProduct.getExpiryDate())
                    .quantity(newTrackedUserProduct.getQuantity())
                    .cost(newTrackedUserProduct.getCost())
                    .volume(newTrackedUserProduct.getVolume())
                    .dateBought(newTrackedUserProduct.getDateBought())
                    .peopleUse(newTrackedUserProduct.getPeopleUse())
                    .frequency(newTrackedUserProduct.getFrequency())
                    .wayPreserve(newTrackedUserProduct.getWayPreserve())
                    .isOpened(newTrackedUserProduct.isOpened())
                    .trackedUserProductOpened(newTrackedUserProductOpened)
                    .build();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //Run update DB
    //Test function
    LocalDateTime parseIntoLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(date, formatter);
    }

//    @Transactional
    public void updateProductCategory() {
//        productRepository.deleteAll();
//        categoryRepository.deleteAll();


//        List<Category> categoryList = categoryRepository.findAll();
//        List<Product> productList = productRepository.findAll();

//        Product product = productRepository.findByProductName("Kem rửa mặt")
//                .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

        try {
            var category = categoryRepository.findByCategoryName("Chăm sóc da");
        //    Category category = categoryRepository.findById(categoryId)
        //            .orElseThrow(() -> new RuntimeException(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));

            Product product = Product.builder()
                    .defCost("{\n" +
                            "\"common\": [\"200000 ₫\", \"150000 ₫\"],\n" +
                            "\"lessCommon\": [\"500000 ₫\", \"40000 ₫\"]\n" +
                            "}")
                    .defExpiryDate("1 year")
                    .defPreserveWay("Nhiệt độ phòng")
                    .defVolume("{\n" +
                            "\"common\": [\"150 ml\"],\n" +
                            "\"lessCommon\": [\"200 ml\"]\n" +
                            "}")
                    .productName("Kem rửa mặt")
                    .build();

            product.setCategory(category.orElse(null));
            category.get().addProduct(product);

            productRepository.save(product);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    public Transaction createCategory() {
        try {
//            Category newCategory = Category.builder()
////                    .categoryId("e58b330b-2ecd-4969-944e-d5185e1f5535")
//                    .categoryName("Chăm sóc da")
//                    .build();
//
//            categoryRepository.save(newCategory);
////            categoryRepository.flush();
//            return newCategory.getId();

//            var product = productRepository.findById("cabdf61a-b06a-47c1-8914-d3238a95f1f9");
//            log.info(product.get().getCategory().getCategoryName());
//            List<Product> lst = productRepository.findAll();
//            for (var product: lst) {
//                log.info(String.valueOf(product.getCategory()));
//            }

            //Create new Transaction
            Transaction newTransaction = Transaction.builder()
                    .dateTransaction(LocalDateTime.now())
                    .build();

            transactionRepository.save(newTransaction);

            return newTransaction;

//            productRepository.deleteById("cabdf61a-b06a-47c1-8914-d3238a95f1f9");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
