package com.example.wallet_service_micro.client.user;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.userRequest.UserIdRequest;
import com.example.wallet_service_micro.exception.user.RemoteUserServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    // ✅ Still fine (internal call, no domain violation)
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

    // ✅ New: Send userId in body (matches AdminController refactor)
    public void blacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/blacklist";
        try {
            webClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new UserIdRequest(userId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    // ✅ New: Send userId in body (matches AdminController refactor)
    public void unblacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/unblacklist";
        try {
            webClient.put()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new UserIdRequest(userId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    // ✅ Shared error extraction
    private String extractMessage(WebClientResponseException ex) {
        try {
            Map<String, Object> body = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
            return body.getOrDefault("message", ex.getStatusCode() + " " + ex.getStatusText()).toString();
        } catch (Exception e) {
            return ex.getStatusCode() + " " + ex.getStatusText();
        }
    }
}
