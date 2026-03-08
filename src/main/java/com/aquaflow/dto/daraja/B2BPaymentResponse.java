package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class B2BPaymentResponse {
    @JsonProperty("ConversationID") private String conversationID;
    @JsonProperty("OriginatorConversationID") private String originatorConversationID;
    @JsonProperty("ResponseCode") private String responseCode;
    @JsonProperty("ResponseDescription") private String responseDescription;
}
