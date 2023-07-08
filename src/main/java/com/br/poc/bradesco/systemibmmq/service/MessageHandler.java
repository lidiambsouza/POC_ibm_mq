package com.br.poc.bradesco.systemibmmq.service;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQMessage;

@Component
public class MessageHandler {

    @Autowired
    private JmsTemplate jmsTemplate;

    protected final static Logger L = LoggerFactory.getLogger(MessageHandler.class);

    public void recv(String destination, Message message) {
        try {
            MQMessage mqMessage = new MQMessage();
            mqMessage.writeString(((TextMessage) message).getText());
            mqMessage.setStringProperty("IncomingDestination", destination);
            processMessage(mqMessage);
        } catch (Exception e) {
            L.error("Error while reading message", e);
        }
    }

    public void processMessage(MQMessage mqMessage) {
        // TODO add application specific message processing code here
    }

    public void send(String destinationQueue, String messageBody) {
        // TODO Create a messageCreator using the messageBody here and use JmsTemplate
        // to send the message
        // The message body depends on what the application needs
        // jmsTemplate.send(destinationQueue, messageCreator);
    }
}