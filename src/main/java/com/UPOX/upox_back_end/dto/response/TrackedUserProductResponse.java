package com.UPOX.upox_back_end.dto.response;


import com.UPOX.upox_back_end.model.TrackedUserProductOpened;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data //Cung cấp các method getter/setter, toString, equalHashCode, requiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder //Tạo ra 1 builder class cho một DTO
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackedUserProductResponse {

    String productName;
    String statusName;
    String categoryName;
    LocalDateTime expiryDate;
    int quantity;
    int cost;
    int volume;
    LocalDateTime dateBought;
    int peopleUse;
    String frequency;
    String wayPreserve;
    boolean isOpened;


    TrackedUserProductOpened trackedUserProductOpened;
}
