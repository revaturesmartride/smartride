package com.revature.RideService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.revature.RideService.dto.response.PaymentResponse;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:rideservicetest;DB_CLOSE_DELAY=-1;MODE=MySQL",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.cloud.config.enabled=false"
})
class RideServiceApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, PaymentResponse> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
