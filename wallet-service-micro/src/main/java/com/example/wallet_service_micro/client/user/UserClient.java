package com.example.wallet_service_micro.client.user;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.exception.user.RemoteUserServiceException;
import com.fasterxml.jackson.databind.ObjectMapper; // Used to parse the JSON response.
import org.springframework.beans.factory.annotation.Value; // injects values from application.properties
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient; // HTTP Client used for API calls.
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class UserClient { // UserClient is an HTTP Client used to interact with UserService

    private final WebClient webClient; // HTTP Client used to send HTTP requests.
    private final String userServiceUrl; // Base URL of the UserService injected from application.properties
    private final ObjectMapper objectMapper; // Used to parse the JSON Error response into maps.

    public UserClient(WebClient webClient, @Value("${user.service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
        this.objectMapper = new ObjectMapper();
    }

    public UserDTO getUserById(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/users/" + userId; // Constructing the final URL.
        try {
            return webClient.get() // This initiates an HTTP GET request using the WebClient instance.
                    .uri(url) // URI (Uniform Resource Identifier, is a way to identify any resource online, URI is a Subset of URL.
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve() // Tells the webClient to prepare for the HTTP call.
                    .bodyToMono(UserDTO.class) // this method ia mainly used to extract the body of HTTP request and convert it into a Mono of specified type.
                    .block(); // Wait synchronously for response, returns UserDTO if HTTP 200, throws WebClientResponseException, if HTTP 4xx or 5xx
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

    public void blacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/" + userId + "/blacklist"; // Constructing final URL.

        try {
            webClient.post() // Initiates an HTTP POST request.
                    .uri(url) // Target admin endpoint
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve() // Prepare client for the HTTP call.
                    .bodyToMono(Void.class) // No response body expected.
                    .block(); // Wait synchronously for completion.
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex)); // Remote error translated
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    public void unblacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/" + userId + "/unblacklist";

        try {
            webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }


}
