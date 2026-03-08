package com.aquaflow.dto.daraja;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class B2BPaymentRequest {
    @JsonProperty("Initiator") private String initiator;
    @JsonProperty("SecurityCredential") private String securityCredential;
    @JsonProperty("CommandID") private String commandID;
    @JsonProperty("SenderIdentifierType") private String senderIdentifierType;
    @JsonProperty("RecieverIdentifierType") private String recieverIdentifierType;
    @JsonProperty("Amount") private String amount;
    @JsonProperty("PartyA") private String partyA;
    @JsonProperty("PartyB") private String partyB;
    @JsonProperty("AccountReference") private String accountReference;
    @JsonProperty("Remarks") private String remarks;
    @JsonProperty("QueueTimeOutURL") private String queueTimeOutURL;
    @JsonProperty("ResultURL") private String resultURL;
}
