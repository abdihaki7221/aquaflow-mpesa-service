package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class C2BRegisterUrlRequest {
    @JsonProperty("ShortCode") private String shortCode;
    @JsonProperty("ResponseType") private String responseType;
    @JsonProperty("ConfirmationURL") private String confirmationURL;
    @JsonProperty("ValidationURL") private String validationURL;
}
