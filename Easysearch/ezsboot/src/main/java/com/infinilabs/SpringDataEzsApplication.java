package com.infinilabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

// mvn spring-boot:run -Dspring-boot.run.profiles=dev
// java -jar target/ezsboot-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableElasticsearchRepositories(basePackages = {"com.infinilabs.dao.es"})
public class SpringDataEzsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringDataEzsApplication.class, args);
    }
}
