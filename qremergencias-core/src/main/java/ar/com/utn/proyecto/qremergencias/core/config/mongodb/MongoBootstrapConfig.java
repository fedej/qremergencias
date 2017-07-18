package ar.com.utn.proyecto.qremergencias.core.config.mongodb;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

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
        private String location = "data.json";
    }

    @Bean
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public Object initMongo(final MongoTemplate mongoTemplate, final ObjectMapper objectMapper) {

        if (config.isEnabled()) {

            try (final InputStream inputStream = resourceLoader.getResource(CLASSPATH_URL_PREFIX
                    + config.getLocation()).getInputStream()) {

                final ObjectMapper mapper = objectMapper.copy();
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.EXISTING_PROPERTY);

                final MongoInitializer mongoInitializer =
                        mapper.readValue(inputStream, MongoInitializer.class);

                final Map<String, Object> stored = new ConcurrentHashMap<>();
                for (final MongoCollection collection: mongoInitializer.getCollections()) {
                    final String name = collection.getName();
                    mongoTemplate.dropCollection(name);
                    final List<MongoObject> elements = collection.getObjects();
                    for (int i = 0; i < elements.size(); i++) {
                        final MongoObject element = elements.get(i);
                        loadRelationships(element.getRelationships(), element.getData(), stored);
                        mongoTemplate.insert(element.getData(), name);
                        stored.put(name + "[" + i + "]", element.getData());
                    }
                }

            } catch (Throwable throwable) {
                log.severe(throwable::getMessage);
            }

        }
        return new Object();
    }

    private void loadRelationships(final Map<String, String> relationships,
                                   final Object data, final Map<String, Object> stored)
            throws Throwable {

        if (relationships != null) {
            for (final Map.Entry<String, String> entry: relationships.entrySet()) {
                final String accessor = entry.getKey();
                final Object referred = stored.get(entry.getValue());

                final MethodHandle setter = MethodHandles.lookup()
                        .findVirtual(data.getClass(), "set" + capitalize(accessor),
                                MethodType.methodType(void.class, referred.getClass()));
                setter.invoke(data, referred);

            }
        }

    }

    private String capitalize(final Object target) {

        if (target == null) {
            return null;
        }
        final StringBuilder result = new StringBuilder(target.toString());
        if (result.length() > 0) {
            result.setCharAt(0, Character.toTitleCase(result.charAt(0)));
        }
        return result.toString();

    }

}
