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

    // wallet-service consumes from this queue (registration + deletion events)
    public static final String WALLET_QUEUE                  = "wallet.registered.queue";
    public static final String USER_TO_WALLET_QUEUE          = "user_to_wallet.registered.queue";
    // wallet-service publishes transaction events → notification-service consumes
    public static final String WALLET_TO_NOTIFICATION_QUEUE  = "notification.transaction.queue";

    // Routing keys for incoming messages
    public static final String WALLET_ROUTING_KEY            = "wallet.registered";
    public static final String USER_TO_WALLET_ROUTING_KEY    = "user_to_wallet.registered";

    // Routing keys wallet-service publishes with
    public static final String ROUTING_KEY_MONEY_SENT        = "wallet.money.sent";
    public static final String ROUTING_KEY_MONEY_RECEIVED    = "wallet.money.received";

    @Bean
    public DirectExchange ewalletExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // --- Queues ---

    // Wallet-service consumes user registration/deletion events from this queue
    @Bean
    public Queue walletQueue() {
        return QueueBuilder.durable(WALLET_QUEUE).build();
    }

    @Bean
    public Queue userToWalletQueue() {
        return QueueBuilder.durable(USER_TO_WALLET_QUEUE).build();
    }

    // Wallet-service publishes money sent/received events; notification-service consumes
    @Bean
    public Queue walletToNotificationQueue() {
        return QueueBuilder.durable(WALLET_TO_NOTIFICATION_QUEUE).build();
    }

    // --- Bindings ---

    // Binds user_to_wallet.registered.queue to user_to_wallet.registered routing key
    // (auth-service and user-service both publish with this key)
    @Bean
    public Binding walletBinding(Queue walletQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(walletQueue)
                .to(ewalletExchange)
                .with(WALLET_ROUTING_KEY);
    }

    @Bean
    public Binding userToWalletBinding(Queue userToWalletQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(userToWalletQueue)
                .to(ewalletExchange)
                .with(USER_TO_WALLET_ROUTING_KEY);
    }

    // Binds notification.transaction.queue to wallet.money.sent routing key
    @Bean
    public Binding walletMoneySentBinding(Queue walletToNotificationQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(walletToNotificationQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_SENT);
    }

    // Binds notification.transaction.queue to wallet.money.received routing key
    @Bean
    public Binding walletMoneyReceivedBinding(Queue walletToNotificationQueue, DirectExchange ewalletExchange) {
        return BindingBuilder
                .bind(walletToNotificationQueue)
                .to(ewalletExchange)
                .with(ROUTING_KEY_MONEY_RECEIVED);
    }

    // --- Messaging Infrastructure ---

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // Needed to publish wallet.money.sent / wallet.money.received events
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Needed to consume from user_to_wallet.registered.queue
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}