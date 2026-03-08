package com.aquaflow.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    @NotBlank private String nationalId;
    @NotBlank private String propertyName;
    @NotBlank private String unitNumber;
}
