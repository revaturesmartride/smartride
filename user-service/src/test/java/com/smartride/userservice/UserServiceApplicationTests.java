package com.smartride.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
/*this annotation tells that to run applications
using "test" profile configuration
when executing the test class
 */
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
