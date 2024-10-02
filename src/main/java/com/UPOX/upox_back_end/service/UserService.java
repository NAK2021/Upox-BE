package com.UPOX.upox_back_end.service;

import com.UPOX.upox_back_end.dto.request.UserCreationRequest;
import com.UPOX.upox_back_end.dto.request.UserUpdateRequest;
import com.UPOX.upox_back_end.dto.response.UserResponse;
import com.UPOX.upox_back_end.entity.User;
//import com.UPOX.upox_back_end.mapper.UserMapper;
import com.UPOX.upox_back_end.model.Mapper;
import com.UPOX.upox_back_end.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService{ //Xử lý business logic

    UserRepository userRepository;


    private Mapper userMapper = new Mapper();

    public UserResponse createRequest(UserCreationRequest objRequest){


        if(userRepository.existsByUsername(objRequest.getUsername())){
            throw new RuntimeException();
        }

        //Dùng phổ biến
        //Chỉ cần 1 vài attribute nhất định, không cần tất cả


//        User newUser = new User();
//        newUser.setUsername(objRequest.getUsername());
//        newUser.setPassword(objRequest.getPassword());
//        newUser.setFirstname(objRequest.getFirstname());
//        newUser.setLastname(objRequest.getLastname());
//        newUser.setDob(objRequest.getDob());

        User newUser = userMapper.toUser(objRequest);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        //Độ mạnh càng lớn thì mật khẩu sẽ càng khó giải mã
        //Mạnh quá thì ảnh hưởng performance
        newUser.setPassword(passwordEncoder.encode(objRequest.getPassword()));

        userRepository.save(newUser); //CREATE user mới

        return userMapper.toUserResponse(newUser);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public UserResponse getUserByID(String Id){
        return  userMapper.toUserResponse(
                userRepository.findById(Id).orElseThrow(() -> new RuntimeException("User not found")));
    }

    public UserResponse updateRequest(String Id, UserUpdateRequest objUpdateRequest){
        User needUpdateUser = userRepository.findById(Id).orElseThrow(()
                -> new RuntimeException("User not found"));

        String oldPass = needUpdateUser.getPassword();
        String newPass = objUpdateRequest.getPassword();

        userMapper.updateUser(needUpdateUser,objUpdateRequest);

//        needUpdateUser.setPassword(objUpdateRequest.getPassword());
//        needUpdateUser.setFirstname(objUpdateRequest.getFirstname());
//        needUpdateUser.setLastname(objUpdateRequest.getLastname());
//        needUpdateUser.setDob(objUpdateRequest.getDob());
//        needUpdateUser.setEmail(objUpdateRequest.getEmail());
//        needUpdateUser.setCity(objUpdateRequest.getCity());
//        needUpdateUser.setPhoneNum(objUpdateRequest.getPhoneNum());

        userRepository.save(needUpdateUser); //UPDATE user mới

        if(!oldPass.matches(newPass)){ //Đã thay đổi pass
            //Disable token cũ
            //Gửi token mới
        }

        return userMapper.toUserResponse(needUpdateUser);
    }

    public void deleteRequest(String Id){
        userRepository.deleteById(Id);
    }

    public boolean isEmailExisted(String email){
        return userRepository.existsByEmail(email);
    }
}
