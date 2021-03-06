package uk.co.andrewrea.claim.fraud;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.andrewrea.claim.fraud.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.claim.fraud.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.fraud.domain.events.publish.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.fraud.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.fraud.services.ClaimFraudHttpService;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQExpections;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.infrastructure.spark.Settings;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestClaimFraudHttpService {
    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private ClaimFraudServiceConfiguration config;

    @Before
    public void before() throws IOException, TimeoutException, JSONException, UnirestException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest(this.rabbitMQFacadeForTest);
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = this.sut.getConfiguration();
        this.config.amqpUsername = "admin";
        this.config.amqpPassword = "admin";
    }

    @After
    public void after() throws IOException, TimeoutException, JSONException, UnirestException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimVerifiedEvent() throws IOException, TimeoutException, UnirestException, InterruptedException {

        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        //ARRANGE
        ClaimFraudHttpService claimFraudService = new ClaimFraudHttpService(this.config);
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimFraudServiceExchangeName);
        claimFraudService.start();

        //Wait for the server to start
        Thread.sleep(Settings.SERVER_INIT_WAIT);

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(this.rabbitMQFacadeForTest.createLocalRabbitMQChannel());

        rabbitMQExpectations.ExpectForExchange(this.config.claimFraudServiceExchangeName, messages -> messages.stream().filter(rabbitMQMessage ->
                rabbitMQMessage.envelope.getRoutingKey().equals(ClaimVerifiedEvent.NAME)).count() == 1);

        ClaimDto claim = sut.getSampleClaim();

        //ACT
        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.id = "someClaimId";
        claimRegisteredEvent.claim = claim;

        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, claimRegisteredEvent);

        //ASSERT
        try {
            rabbitMQExpectations.VerifyAllExpectations();
        } finally {
            claimFraudService.stop();
        }

    }
}
