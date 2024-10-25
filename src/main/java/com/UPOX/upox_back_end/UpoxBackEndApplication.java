package com.UPOX.upox_back_end;

import com.UPOX.upox_back_end.entity.TrackedUserProduct;
import jakarta.persistence.Id;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.lang.reflect.Field;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement
public class UpoxBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpoxBackEndApplication.class, args);
//		for (Field field : TrackedUserProduct.class.getDeclaredFields()) {
//			if (field.isAnnotationPresent(Id.class)) {
//				log.info(field.getName() + " is the primary key.");
//			}
//		}
	}

}
