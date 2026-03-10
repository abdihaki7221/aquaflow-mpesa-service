package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("meters")
public class Meter {

    @Id
    private Long id;

    @Column("meter_number")
    private String meterNumber;

    @Column("tenant_name")
    private String tenantName;

    @Column("unit_number")
    private String unitNumber;

    @Column("phone")
    private String phone;

    @Column("email")
    private String email;

    @Column("address")
    private String address;

    @Column("previous_reading")
    private Long previousReading;

    @Column("current_reading")
    private Long currentReading;

    @Column("last_read_date")
    private LocalDateTime lastReadDate;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}