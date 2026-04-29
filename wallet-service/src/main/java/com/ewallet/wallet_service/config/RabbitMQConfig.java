package com.ewallet.wallet_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "ewallet.exchange";

    public static final String WALLET_QUEUE       = "wallet.registered.queue";
    public static final String NOTIFICATION_QUEUE = "notification.transaction.queue";

    public static final String ROUTING_KEY_MONEY_SENT     = "wallet.money.sent";
    public static final String ROUTING_KEY_MONEY_RECEIVED = "wallet.money.received";
    public static final String ROUTING_KEY_USER_REGISTERED = "notification.registered";

    @Bean
    public DirectExchange ewalletExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue walletQueue() {
        return QueueBuilder.durable(WALLET_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding moneySentToWalletBinding(Queue walletQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(walletQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_SENT);
    }

    @Bean
    public Binding moneyReceivedToWalletBinding(Queue walletQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(walletQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_RECEIVED);
    }

    @Bean
    public Binding moneySentToNotificationBinding(Queue notificationQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_SENT);
    }

    @Bean
    public Binding moneyReceivedToNotificationBinding(Queue notificationQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_RECEIVED);
    }

    @Bean
    public Binding userRegisteredToNotificationBinding(Queue notificationQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_USER_REGISTERED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}