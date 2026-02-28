package com.aquaflow.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class C2BRegisterUrlResponse {

    @JsonProperty("OriginatorCoversationID")
    private String originatorConversationId;

    @JsonProperty("ConversationID")
    private String conversationId;

    @JsonProperty("ResponseDescription")
    private String responseDescription;
}
