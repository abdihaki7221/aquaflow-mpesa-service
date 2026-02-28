package com.aquaflow.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class B2BPaymentResponse {

    @JsonProperty("OriginatorConversationID")
    private String originatorConversationId;

    @JsonProperty("ConversationID")
    private String conversationId;

    @JsonProperty("ResponseCode")
    private String responseCode;

    @JsonProperty("ResponseDescription")
    private String responseDescription;
}
