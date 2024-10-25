package com.UPOX.upox_back_end.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity //Tạo một bản sao với database (Entity = thực thể/bảng)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String notiId;

    //Attribute
    LocalDateTime dateSend;
    String noti_content;
    String heading;
    String type;

    //Foreign Key
    //Many to One với User
    @ManyToOne(fetch = FetchType.LAZY)
    User user;

}
