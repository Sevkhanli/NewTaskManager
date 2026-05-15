package az.edu.itbrains;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
@EnableScheduling
public class TaskManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskManagerApplication.class, args);
	}
}

