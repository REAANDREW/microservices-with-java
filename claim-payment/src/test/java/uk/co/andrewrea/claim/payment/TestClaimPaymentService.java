package uk.co.andrewrea.claim.payment;

import com.rabbitmq.client.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.claim.payment.config.ClaimPaymentServiceConfiguration;
import uk.co.andrewrea.claim.payment.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.payment.domain.events.publish.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.payment.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.claim.payment.services.ClaimPaymentHttpService;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQExpections;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class TestClaimPaymentService {

    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private SystemUnderTest sut;
    private ClaimPaymentServiceConfiguration config;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.sut = new SystemUnderTest();
        this.config = this.sut.getConfiguration();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimAwardPaidEvent() throws IOException, InterruptedException, TimeoutException {

        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimAwardedServiceExchangeName);

        ClaimPaymentServiceConfiguration claimPaymentServiceConfiguration = new ClaimPaymentServiceConfiguration();

        Service service = Service.ignite().port(claimPaymentServiceConfiguration.port);
        Channel channel = this.rabbitMQFacadeForTest.createLocalRabbitMQChannel();
        Publisher publisher = RabbitMQPublisher.create(channel,this.config.claimPaymentServiceExchangeName);
        ClaimPaymentHttpService claimPaymentHttpService = new ClaimPaymentHttpService(service, publisher, this.config);
        claimPaymentHttpService.start();

        Channel expectationsChannel = this.rabbitMQFacadeForTest.createLocalRabbitMQChannel();
        RabbitMQExpections expectations = new RabbitMQExpections(expectationsChannel);
        expectations.ExpectForExchange(this.config.claimPaymentServiceExchangeName,messages -> {
           return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimAwardPaidEvent.NAME);
        });

        ClaimDto claim = this.sut.getSampleClaim();
        ClaimAwardedEvent claimAwardedEvent = new ClaimAwardedEvent();
        claimAwardedEvent.id = "someId";
        claimAwardedEvent.claim = claim;

        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimAwardedServiceExchangeName, ClaimAwardedEvent.NAME, claimAwardedEvent);

        try{
            expectations.VerifyAllExpectations();
        }finally{
            claimPaymentHttpService.stop();
        }
    }
}