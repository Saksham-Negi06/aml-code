package com.aml.config.db;

import org.springframework.context.annotation.Configuration;

@Configuration
public class OracleDataSourceConfig {
    // Spring Boot AutoConfiguration will automatically construct the Oracle DataSource 
    // using the spring.datasource.* properties defined in the active service configuration.
}
