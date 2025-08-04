package com.example.inventory.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ApiKeyFilter implements Filter {

    private static final String HEADER_NAME = "X-API-KEY";

    @Autowired
    private ApiKeyRegistry apiKeyRegistry;

    // Lista de rutas que NO requieren autenticación
    private static final List<String> WHITELIST = List.of(
            "/swagger-ui.html",
            "/swagger-ui/",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/v3/api-docs/swagger-config"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = httpReq.getRequestURI();

        // ✅ Ignorar rutas públicas (Swagger)
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = httpReq.getHeader(HEADER_NAME);

        if (apiKey == null || apiKey.isBlank()) {
            reject((HttpServletResponse) response, "Falta el header X-API-KEY");
            return;
        }

        Optional<String> serviceOpt = apiKeyRegistry.getServiceByApiKey(apiKey);
        if (serviceOpt.isEmpty()) {
            reject((HttpServletResponse) response, "API key inválida");
            return;
        }

        String service = serviceOpt.get();
        if (!apiKeyRegistry.isAccessAllowed(path, service)) {
            reject((HttpServletResponse) response, "Acceso denegado para esta ruta");
            return;
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":401,\"message\":\"" + message + "\",\"error\":{}}");
    }
}
