package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table("meters")
public class Meter {
    @Id private Long id;
    private String meterNumber;
    private String tenantName;
    private String unitNumber;
    private String phone;
    private String email;
    private String address;
    private Long previousReading;
    private Long currentReading;
    private LocalDateTime lastReadDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
