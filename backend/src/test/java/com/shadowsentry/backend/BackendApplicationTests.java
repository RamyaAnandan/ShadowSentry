package com.shadowsentry.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 👇 This tells Spring Boot to not start web server during tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // This test only checks if the Spring context loads without errors
    }
}
