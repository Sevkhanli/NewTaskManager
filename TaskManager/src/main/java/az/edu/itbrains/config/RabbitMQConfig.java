package az.edu.itbrains.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "email_queue";

    @Bean
    public Queue emailQueue() {
        // durable: true - yəni RabbitMQ sönsə belə növbə itməsin
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Mesajları JSON formatına çevirmək üçün
        return new SimpleMessageConverter();
    }
}