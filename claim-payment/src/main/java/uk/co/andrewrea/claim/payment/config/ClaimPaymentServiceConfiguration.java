package uk.co.andrewrea.claim.payment.config;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimPaymentServiceConfiguration {

    public int servicePort = 8086;
    public String serviceIp = "127.0.0.1";
    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";
    public String amqpUsername;
    public String amqpPassword;
    public String claimAwardServiceExchangeName = "claim-award-service";
    public String claimPaymentServiceExchangeName = "claim-payment-service";

}
