package uk.ac.ed.inf.ilpcw1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ILPConfig {

    private static final String DEFAULT_ILP_ENDPOINT = "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/";

    @Bean
    public String ilpEndpoint() {
        String endpoint = System.getenv("ILP_ENDPOINT");
        return (endpoint != null && !endpoint.isEmpty()) ? endpoint : DEFAULT_ILP_ENDPOINT;
    }

}
