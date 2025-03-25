package com.krstudy.kapi.global.app

import com.krstudy.kapi.domain.kafkaproducer.entity.WebLog
import com.krstudy.kapi.domain.trade.event.OrderEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConfig {

    // Producer 설정
    @Bean
    fun orderProducerFactory(): ProducerFactory<String, OrderEvent> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun orderKafkaTemplate(): KafkaTemplate<String, OrderEvent> {
        return KafkaTemplate(orderProducerFactory()).apply {
            defaultTopic = "order-events"
        }
    }

    // Consumer 설정
    @Bean
    fun orderConsumerFactory(): ConsumerFactory<String, OrderEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            // Trust packages 설정
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            JsonDeserializer(OrderEvent::class.java, false)
        )
    }

    @Bean
    fun orderKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, OrderEvent> {
        return ConcurrentKafkaListenerContainerFactory<String, OrderEvent>().apply {
            this.consumerFactory = orderConsumerFactory()
            setConcurrency(3)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        }
    }

    // WebLog Producer 설정
    @Bean
    fun webLogProducerFactory(): ProducerFactory<String, WebLog> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun webLogKafkaTemplate(): KafkaTemplate<String, WebLog> {
        return KafkaTemplate(webLogProducerFactory())
    }

    // WebLog Consumer 설정
    @Bean
    fun webLogConsumerFactory(): ConsumerFactory<String, WebLog> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "weblog-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            JsonDeserializer(WebLog::class.java, false)
        )
    }

    @Bean
    fun webLogKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, WebLog> {
        return ConcurrentKafkaListenerContainerFactory<String, WebLog>().apply {
            this.consumerFactory = webLogConsumerFactory()
            setConcurrency(3)
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        }
    }
}