package com.br.poc.bradesco.systemibmmq.controller;

import java.util.Set;
import java.util.UUID;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.poc.bradesco.systemibmmq.config.JmsConfig;
import com.br.poc.bradesco.systemibmmq.service.MessageHandler;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

@RestController
@RequestMapping("/context_switch")
public class ContextSwitchController {
    protected final static Logger L = LoggerFactory.getLogger(ContextSwitchController.class);
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private JmsConnectionFactory factory;
    @Autowired
    private JmsListenerEndpointRegistry registry;
    @Autowired
    private JmsListenerEndpointRegistrar registrar;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private JmsConfig jmsConfig;

    @PostMapping
    public String switchContext(@RequestBody JmsConfig configRequest) throws Exception {
        try {
            if (configRequest.getQueueManager() != null) {
                BeanUtils.copyProperties(jmsConfig, configRequest);
                setupConnection(configRequest);
                switchQueues(configRequest);
            }
            L.debug("Context switched successfully");
        } catch (Exception e) {
            throw new Exception("Unable to switch context.");
        }
        return "Switched to QM: " + jmsConfig.getQueueManager() + " with host: " + jmsConfig.getHost() + " and port: "
                + jmsConfig.getPort();
    }

    private void setupConnection(JmsConfig configRequest) throws Exception {
        Set<String> listenerContainerIds = registry.getListenerContainerIds();
        for (String id : listenerContainerIds) {
            registry.getListenerContainer(id).stop();
        }
        try {
            factory.setStringProperty(WMQConstants.WMQ_HOST_NAME, configRequest.getHost());
            factory.setObjectProperty(WMQConstants.WMQ_PORT, Integer.valueOf(configRequest.getPort()));
            factory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, configRequest.getQueueManager());
            factory.setStringProperty(WMQConstants.WMQ_CHANNEL, configRequest.getChannel());
            factory.setStringProperty(WMQConstants.USERID, configRequest.getUser());
            factory.setStringProperty(WMQConstants.PASSWORD, configRequest.getPassword());

            factory.createConnection();
            jmsTemplate.setConnectionFactory(factory);
        } catch (JMSException e) {
            throw new Exception("Invalid Connection Factory Parameters");
        }
    }

    private void switchQueues(JmsConfig configRequest) {
        for (String queueName : configRequest.getSampleQueues()) {
            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
            endpoint.setId("demo-" + UUID.randomUUID());
            endpoint.setDestination(queueName);
            endpoint.setConcurrency(configRequest.getConcurrencyMin() + "-" + configRequest.getConcurrencyMax());
            endpoint.setMessageListener(message -> {
                messageHandler.recv(queueName, message);
            });
            registrar.registerEndpoint(endpoint);
        }
    }
}