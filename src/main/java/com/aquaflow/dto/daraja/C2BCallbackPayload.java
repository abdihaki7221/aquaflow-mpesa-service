package com.aquaflow.dto.daraja;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload Safaricom sends to our C2B Validation and Confirmation URLs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class C2BCallbackPayload {

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("TransID")
    private String transId;

    @JsonProperty("TransTime")
    private String transTime;

    @JsonProperty("TransAmount")
    private BigDecimal transAmount;

    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    @JsonProperty("BillRefNumber")
    private String billRefNumber;

    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;

    @JsonProperty("OrgAccountBalance")
    private BigDecimal orgAccountBalance;

    @JsonProperty("ThirdPartyTransID")
    private String thirdPartyTransId;

    @JsonProperty("MSISDN")
    private String msisdn;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("MiddleName")
    private String middleName;

    @JsonProperty("LastName")
    private String lastName;
}
