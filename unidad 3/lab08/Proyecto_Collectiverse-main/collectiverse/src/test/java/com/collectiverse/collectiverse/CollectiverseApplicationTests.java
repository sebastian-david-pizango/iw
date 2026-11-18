package com.collectiverse.collectiverse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")   // usa H2 en memoria, no la base de datos de Neon
class CollectiverseApplicationTests {

	@Test
	void contextLoads() {
	}

}
