package com.br.poc.bradesco.systemibmmq.config;

import java.util.List;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import lombok.Getter;

@Configuration
@Getter
public class JmsConfig {
    @Value("${ibm.mq.host}")
    private String host;

    @Value("${ibm.mq.port}")
    private Integer port;

    @Value("${ibm.mq.queueManager}")
    private String queueManager;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.user}")
    private String user;

    @Value("${ibm.mq.password}")
    private String password;

    @Value("${ibm.mq.connName}")
    private String connName;

    @Value("${ibm.queues.sampleQueues}")
    private List<String> sampleQueues;

    @Value("${demo.concurrency.size.low}")
    private String concurrencyMin;

    @Value("${demo.concurrency.size.high}")
    private String concurrencyMax;

    @Bean
    public JmsTemplate jmsTemplate() throws JMSException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(cachingConnectionFactory());
        return jmsTemplate;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() throws JMSException {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setSessionCacheSize(1);
        factory.setTargetConnectionFactory(createConnectionFactory());
        factory.setReconnectOnException(true);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public JmsConnectionFactory createConnectionFactory() throws JMSException {
        JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.WMQ_PROVIDER);
        JmsConnectionFactory factory = ff.createConnectionFactory();
        factory.setObjectProperty(WMQConstants.WMQ_CONNECTION_MODE, Integer.valueOf(WMQConstants.WMQ_CM_CLIENT));
        factory.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
        factory.setObjectProperty(WMQConstants.WMQ_PORT, port);
        factory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
        factory.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
        factory.setStringProperty(WMQConstants.USERID, user);
        factory.setStringProperty(WMQConstants.PASSWORD, password);
        return factory;
    }

    @Bean
    @Primary
    public JmsListenerEndpointRegistry createRegistry() {
        JmsListenerEndpointRegistry registry = new JmsListenerEndpointRegistry();
        return registry;
    }

    @Bean
    public JmsListenerEndpointRegistrar createRegistrar() throws JMSException {
        JmsListenerEndpointRegistrar registrar = new JmsListenerEndpointRegistrar();
        registrar.setEndpointRegistry(createRegistry());
        registrar.setContainerFactory(createDefaultJmsListenerContainerFactory());
        return registrar;
    }

    public DefaultJmsListenerContainerFactory createDefaultJmsListenerContainerFactory() throws JMSException {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(createConnectionFactory());
        return factory;
    }

}
