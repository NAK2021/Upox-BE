package com.UPOX.upox_back_end.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data //Cung cấp các method getter/setter, toString, equalHashCode, requiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder //Tạo ra 1 builder class cho một DTO
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackedUserProductOpened {
    LocalDateTime dateOpen;
    int volumeLeft;
    String statusName; //take from statusId
    LocalDateTime openExpiryDate;
    LocalDateTime openStatusChangedDate;
}
