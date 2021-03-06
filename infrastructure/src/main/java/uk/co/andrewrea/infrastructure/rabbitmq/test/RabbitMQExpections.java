package uk.co.andrewrea.infrastructure.rabbitmq.test;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by vagrant on 5/11/16.
 */
public class RabbitMQExpections {
    private Channel channel;
    private final Map<String, ArrayList<RabbitMQMessage>> messages = new HashMap<>();
    private final Map<String, ArrayList<RabbitMQExpectation>> expectations = new HashMap<>();

    public RabbitMQExpections(Channel channel) {
        this.channel = channel;
    }

    public RabbitMQExpections ExpectForExchange(String name, RabbitMQExpectation expectation) throws IOException {
        if (!this.messages.containsKey(name)) {
            this.messages.put(name, new ArrayList<>());
        }
        if (!this.expectations.containsKey(name)) {
            this.expectations.put(name, new ArrayList<>());
        }
        this.expectations.get(name).add(expectation);

        String queueName = UUID.randomUUID().toString();
        String cTag = UUID.randomUUID().toString();

        //Create a queue and bind to the exchange
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, name, "*");
        //Create a consumer of the queue
        Runnable consumer = () -> {
            try {
                channel.basicConsume(queueName, false, cTag,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException {
                                messages.get(name).add(new RabbitMQMessage(consumerTag, envelope, properties, body));
                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();
        return this;
    }

    public void VerifyAllExpectations() throws InterruptedException {
        //Breathe, interrupt and let the core propagate
        Thread.sleep(100);

        for (Map.Entry<String, ArrayList<RabbitMQExpectation>> expectationsForExchange : expectations.entrySet()) {
            ArrayList<RabbitMQMessage> input = messages.get(expectationsForExchange.getKey());
            for (RabbitMQExpectation expectation : expectationsForExchange.getValue()) {
                if (!expectation.match(input)) {
                    throw new RuntimeException(String.format("Expectations failed for Exchange: %s [failed]", expectationsForExchange.getKey()));
                }
            }
        }
    }


}
