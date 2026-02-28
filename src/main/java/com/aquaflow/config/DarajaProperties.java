package com.aquaflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "daraja")
public class DarajaProperties {

    private String baseUrl;
    private String consumerKey;
    private String consumerSecret;
    private C2B c2b = new C2B();
    private B2B b2b = new B2B();

    @Data
    public static class C2B {
        private String shortcode;
        private String confirmationUrl;
        private String validationUrl;
        private String responseType;
    }

    @Data
    public static class B2B {
        private String initiatorName;
        private String securityCredential;
        private String senderShortcode;
        private String receiverShortcode;
        private String commandId;
        private String queueTimeoutUrl;
        private String resultUrl;
        private int disbursementPercentage = 50;
    }
}
