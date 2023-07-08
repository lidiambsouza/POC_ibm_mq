package com.br.poc.bradesco.systemibmmq.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import com.br.poc.bradesco.systemibmmq.service.MessageHandler;

@Configuration
public class MqConfig {

    @Autowired
    JmsListenerEndpointRegistrar registrar;

    @Autowired
    private MessageHandler queueController;

    @Value("${ibm.queues.sampleQueues}")
    String[] sampleQueues;

    @Value("${demo.concurrency.size.low}")
    Integer messageConcurrencyLow;

    @Value("${demo.concurrency.size.high}")
    Integer messageConcurrencyHigh;

    String jmsMessageConcurrency = "";

    @PostConstruct
    public void init() {
        jmsMessageConcurrency = String.format("%s-%s", messageConcurrencyLow, messageConcurrencyHigh);
        configureJmsListeners(registrar);
    }

    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
        int i = 0;
        for (final String queueName : sampleQueues) {
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId("demo-" + i++);
            endpoint.setDestination(queueName);
            endpoint.setConcurrency(jmsMessageConcurrency);

            endpoint.setMessageListener(message -> {
                queueController.recv(queueName, message);
            });
            registrar.registerEndpoint(endpoint);
        }
    }
}