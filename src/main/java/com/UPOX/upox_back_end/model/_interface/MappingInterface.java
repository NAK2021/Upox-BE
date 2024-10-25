package com.UPOX.upox_back_end.model._interface;

import com.UPOX.upox_back_end.dto.request.TrackedUserProductRequest;
import com.UPOX.upox_back_end.dto.request.UserCreationRequest;
import com.UPOX.upox_back_end.dto.request.UserUpdateRequest;
import com.UPOX.upox_back_end.dto.response.UserResponse;
import com.UPOX.upox_back_end.entity.TrackedUserProduct;
import com.UPOX.upox_back_end.entity.User;

public interface MappingInterface {

    User toUser(UserCreationRequest request);

    //Đôi khi sẽ phải map những properties không trùng nhau (source, target, ignore)
    UserResponse toUserResponse(User user);
    void updateUser(User user, UserUpdateRequest request);


    //Tracked User Product
    TrackedUserProduct toTrackedUserProduct(TrackedUserProductRequest request);


}
