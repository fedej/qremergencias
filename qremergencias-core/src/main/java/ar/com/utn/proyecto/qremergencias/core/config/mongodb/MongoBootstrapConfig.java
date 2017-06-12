package ar.com.utn.proyecto.qremergencias.core.config.mongodb;

import com.mongodb.util.JSON;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.data.mongodb.core.BulkOperations.BulkMode.ORDERED;

@Configuration
@ConditionalOnBean(MongoTemplate.class)
@Log
public class MongoBootstrapConfig {

    @Autowired
    private MongoBootstrapProperties config;

    @Autowired
    private ResourceLoader resourceLoader;

    @Component
    @ConfigurationProperties("mongo.bootstrap")
    @SuppressWarnings("PMD.ImmutableField")
    @Data
    public static class MongoBootstrapProperties {
        private boolean enabled;
        private String collectionName = "test";
        private String location = "data.json";

    }

    @Bean
    public Object initMongo(final MongoTemplate mongoTemplate) {

        if (config.isEnabled()) {

            final Resource resource = resourceLoader.getResource(config.getLocation());

            try {
                final Path p = resource.getFile().toPath();
                final BulkOperations bulkOperations = mongoTemplate
                        .bulkOps(ORDERED, config.getCollectionName());
                Files.readAllLines(p)
                        .stream()
                        .map(JSON::parse)
                        .forEach(bulkOperations::insert);
                bulkOperations.execute();
            } catch (IOException e) {
                log.severe(e::getMessage);
            }
        }
        return new Object();
    }
}
