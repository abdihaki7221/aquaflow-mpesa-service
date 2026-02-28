package com.aquaflow.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * B2B Result callback payload from Safaricom.
 * Sent to the ResultURL after B2B payment processing completes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class B2BResultPayload {

    @JsonProperty("Result")
    private Result result;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        @JsonProperty("ResultType")
        private Integer resultType;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("OriginatorConversationID")
        private String originatorConversationId;

        @JsonProperty("ConversationID")
        private String conversationId;

        @JsonProperty("TransactionID")
        private String transactionId;

        @JsonProperty("ResultParameters")
        private ResultParameters resultParameters;

        @JsonProperty("ReferenceData")
        private ReferenceData referenceData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultParameters {

        @JsonProperty("ResultParameter")
        private List<ResultParameter> resultParameter;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultParameter {

        @JsonProperty("Key")
        private String key;

        @JsonProperty("Value")
        private Object value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceData {

        @JsonProperty("ReferenceItem")
        private ReferenceItem referenceItem;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReferenceItem {

        @JsonProperty("Key")
        private String key;

        @JsonProperty("Value")
        private String value;
    }
}
