package ar.com.utn.proyecto.qremergencias.core.config.mongodb;

import com.mongodb.util.JSON;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.data.mongodb.core.BulkOperations.BulkMode.ORDERED;

@Configuration
@AutoConfigureAfter(MongoAutoConfiguration.class)
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

            try (final InputStream inputStream = resourceLoader.getResource(CLASSPATH_URL_PREFIX
                    + config.getLocation()).getInputStream();
                 final Reader inputStreamReader = new InputStreamReader(inputStream, UTF_8.name());
                 final BufferedReader reader = new BufferedReader(inputStreamReader)) {

                final BulkOperations bulkOperations = mongoTemplate
                        .bulkOps(ORDERED, config.getCollectionName());

                while (reader.ready()) {
                    final String line = reader.readLine();
                    bulkOperations.insert(JSON.parse(line));
                }
                bulkOperations.execute();


            } catch (IOException e) {
                log.severe(e::getMessage);
            }

        }
        return new Object();
    }
}
