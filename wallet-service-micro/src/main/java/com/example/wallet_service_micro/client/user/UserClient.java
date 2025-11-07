package com.example.wallet_service_micro.client.user;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.exception.user.RemoteUserServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class UserClient {

    private final WebClient webClient;
    private final String userServiceUrl;
    private final ObjectMapper objectMapper;

    public UserClient(WebClient webClient, @Value("${user.service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
        this.objectMapper = new ObjectMapper();
    }

    public UserDTO getUserById(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/users/" + userId;
        try {
            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    public List<UserDTO> getAllUsers(String authHeader) {
        String url = userServiceUrl + "/api/admin/users";
        try {
            UserDTO[] usersArray = webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(UserDTO[].class)
                    .block();
            return usersArray != null ? List.of(usersArray) : List.of();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    public UserDTO getUserFromToken(String authHeader) {
        String url = userServiceUrl + "/api/users/me";
        try {
            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    /**
     * Extracts a clean error message from WebClientResponseException.
     * If the response body contains JSON with a 'message' field, it uses that.
     * Otherwise, falls back to HTTP status code + reason.
     */
    private String extractMessage(WebClientResponseException ex) {
        try {
            Map<String, Object> body = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
            return body.getOrDefault("message", ex.getStatusCode() + " " + ex.getStatusText()).toString();
        } catch (Exception e) {
            return ex.getStatusCode() + " " + ex.getStatusText();
        }
    }
}
