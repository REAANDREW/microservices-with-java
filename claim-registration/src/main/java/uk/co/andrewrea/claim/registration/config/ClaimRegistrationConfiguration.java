package uk.co.andrewrea.claim.registration.config;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegistrationConfiguration {

    public int servicePort = 8083;
    public String serviceIp = "127.0.0.1";
    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";
    public String amqpUsername;
    public String amqpPassword;

    public int mongoDbPort = 27017;
    public String mongoDbHost = "127.0.0.1";
    public String mongoDatabaseName = "claimRegistration";
    public String mongoClaimCollectionName = "claims";

    public String claimRegistrationServiceExchangeName = "claim-registration-service";

}
