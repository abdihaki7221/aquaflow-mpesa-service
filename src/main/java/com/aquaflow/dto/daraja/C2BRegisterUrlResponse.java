package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class C2BRegisterUrlResponse {
    @JsonProperty("OriginatorCoversationID") private String originatorConversationId;
    @JsonProperty("ResponseCode") private String responseCode;
    @JsonProperty("ResponseDescription") private String responseDescription;
}
