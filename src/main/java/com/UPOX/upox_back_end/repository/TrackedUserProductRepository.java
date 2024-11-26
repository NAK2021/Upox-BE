package com.UPOX.upox_back_end.repository;

import com.UPOX.upox_back_end.entity.Product;
import com.UPOX.upox_back_end.entity.TrackedUserProduct;
import com.UPOX.upox_back_end.model.IdClass.TrackedUserProductID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedUserProductRepository extends JpaRepository<TrackedUserProduct, TrackedUserProductID> {
    List<TrackedUserProduct> findTop10ByOrderByDateBoughtDesc();

    List<TrackedUserProduct> findAllByProduct(Product product);

}
