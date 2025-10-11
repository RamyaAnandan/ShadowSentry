package com.shadowsentry.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        // Ensure we use TLSv1.2 for outbound HTTPS calls (HIBP requires modern TLS)
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        SpringApplication.run(BackendApplication.class, args);
    }
}
