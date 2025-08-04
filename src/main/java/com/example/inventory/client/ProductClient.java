package com.example.inventory.client;

import com.example.inventory.dto.ApiResponse;
import com.example.inventory.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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

    @Retryable(
            value = { RestClientException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public ProductDTO getProductById(Long productId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ApiResponse<ProductDTO>> response = restTemplate.exchange(
                productServiceUrl + "/api/products/" + productId,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<ProductDTO>>() {}
        );

        return response.getBody().getData();
    }

    @Recover
    public ProductDTO recover(RestClientException e, Long productId) {
        System.err.println("Fallarn todos los intentos para obtener el producto con ID: " + productId);
        return null;
    }
}
