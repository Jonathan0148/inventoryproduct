package com.example.inventory.security;

import com.example.inventory.config.ApiKeyProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ApiKeyRegistry {

    private final ApiKeyProperties properties;

    private final Map<String, List<String>> routeRestrictions = Map.of(
            "/api/inventory", List.of("frontend")
    );

    public ApiKeyRegistry(ApiKeyProperties properties) {
        this.properties = properties;
    }

    public Optional<String> getServiceByApiKey(String apiKey) {
        return properties.getKeys().entrySet().stream()
                .filter(entry -> entry.getValue().equals(apiKey))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public boolean isAccessAllowed(String path, String service) {
        return routeRestrictions.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .allMatch(entry -> entry.getValue().contains(service));
    }

    public Set<String> getAllValidKeys() {
        return new HashSet<>(properties.getKeys().values());
    }
}
