package com.UPOX.upox_back_end.model;

import com.UPOX.upox_back_end.dto.request.UserCreationRequest;
import com.UPOX.upox_back_end.dto.request.UserUpdateRequest;
import com.UPOX.upox_back_end.dto.response.UserResponse;
import com.UPOX.upox_back_end.entity.User;
import com.UPOX.upox_back_end.model._interface.MappingInterface;

public class Mapper implements MappingInterface {
    public Mapper() {
    }
    @Override
    public User toUser(UserCreationRequest request) {
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());

        return  newUser;
    }

    @Override
    public void updateUser(User needUpdateUser, UserUpdateRequest request) {
        needUpdateUser.setPassword(request.getPassword());
        needUpdateUser.setFirstname(request.getFirstname());
        needUpdateUser.setLastname(request.getLastname());
        needUpdateUser.setDob(request.getDob());
        needUpdateUser.setEmail(request.getEmail());
        needUpdateUser.setCity(request.getCity());
        needUpdateUser.setPhoneNum(request.getPhoneNum());
        needUpdateUser.setGender(request.getGender());
    }

    @Override
    public UserResponse toUserResponse(User user){
        UserResponse userResponse = new UserResponse();

        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstname(user.getFirstname());
        userResponse.setLastname(user.getLastname());
        userResponse.setDob(user.getDob());
        userResponse.setEmail(user.getEmail());
        userResponse.setCity(user.getCity());
        userResponse.setPhoneNum(user.getPhoneNum());
        userResponse.setGender(user.getGender());

        return  userResponse;
    }
}
