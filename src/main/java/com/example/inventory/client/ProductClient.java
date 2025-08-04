package com.example.inventory.client;

import com.example.inventory.dto.ApiResponse;
import com.example.inventory.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    private final String apiKey;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url}") String productServiceUrl,
                         @Value("${product.service.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.apiKey = apiKey;
    }

    public ProductDTO getProductById(Long productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey); // 👈 Incluye la API Key si es requerida
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                productServiceUrl + "/api/products/" + productId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        return response.getBody().getData();
    }
}
