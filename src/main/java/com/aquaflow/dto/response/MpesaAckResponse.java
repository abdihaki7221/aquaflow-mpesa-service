package com.aquaflow.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard acknowledgment response sent back to Safaricom
 * on callback endpoints (validation / confirmation / B2B result).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaAckResponse {

    @JsonProperty("ResultCode")
    private Integer resultCode;

    @JsonProperty("ResultDesc")
    private String resultDesc;

    public static MpesaAckResponse accepted() {
        return MpesaAckResponse.builder()
                .resultCode(0)
                .resultDesc("Accepted")
                .build();
    }

    public static MpesaAckResponse rejected(String reason) {
        return MpesaAckResponse.builder()
                .resultCode(1)
                .resultDesc(reason)
                .build();
    }
}
