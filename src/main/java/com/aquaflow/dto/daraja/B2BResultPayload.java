package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class B2BResultPayload {
    @JsonProperty("Result") private Result result;

    @Data
    public static class Result {
        @JsonProperty("ResultType") private Integer resultType;
        @JsonProperty("ResultCode") private Integer resultCode;
        @JsonProperty("ResultDesc") private String resultDesc;
        @JsonProperty("OriginatorConversationID") private String originatorConversationID;
        @JsonProperty("ConversationID") private String conversationID;
        @JsonProperty("TransactionID") private String transactionID;
    }
}
