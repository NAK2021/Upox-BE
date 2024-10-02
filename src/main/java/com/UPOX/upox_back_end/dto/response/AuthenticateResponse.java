package com.UPOX.upox_back_end.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data //Cung cấp các method getter/setter, toString, equalHashCode, requiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder //Tạo ra 1 builder class cho một DTO
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticateResponse {
    String token;
    boolean authenticated;
}
