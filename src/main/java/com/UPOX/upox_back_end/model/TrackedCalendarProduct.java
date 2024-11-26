package com.UPOX.upox_back_end.model;


import com.UPOX.upox_back_end.entity.TrackedUserProduct;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data //Cung cấp các method getter/setter, toString, equalHashCode, requiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder //Tạo ra 1 builder class cho một DTO
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackedCalendarProduct {
    TrackedUserProduct trackedUserProduct;
    LocalDate dateDisplay;
}
