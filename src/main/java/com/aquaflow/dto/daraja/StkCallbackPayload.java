package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StkCallbackPayload {
    @JsonProperty("Body") private Body body;

    @Data
    public static class Body {
        @JsonProperty("stkCallback") private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        @JsonProperty("MerchantRequestID") private String merchantRequestID;
        @JsonProperty("CheckoutRequestID") private String checkoutRequestID;
        @JsonProperty("ResultCode") private Integer resultCode;
        @JsonProperty("ResultDesc") private String resultDesc;
        @JsonProperty("CallbackMetadata") private CallbackMetadata callbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        @JsonProperty("Item") private List<Item> item;
    }

    @Data
    public static class Item {
        @JsonProperty("Name") private String name;
        @JsonProperty("Value") private Object value;
    }
}
