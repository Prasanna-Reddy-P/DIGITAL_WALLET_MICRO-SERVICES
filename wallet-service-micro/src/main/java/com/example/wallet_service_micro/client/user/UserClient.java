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

@Component // Indicates spring to create an instance of this class, so that it can create and manage beans.
public class UserClient {

    private final WebClient webClient; // used to make API calls
    private final String userServiceUrl; // base URL of the user-service (injected from application.yml)
    private final ObjectMapper objectMapper; // used to deserialize JSON responses (especially for errors)

    public UserClient(WebClient webClient, @Value("${user.service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
        this.objectMapper = new ObjectMapper();
    }

    // ✅ Still fine (internal call, no domain violation)
    public UserDTO getUserById(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/users/" + userId;
        try {
            return webClient.get() // Initiates an HTTP GET request using the WebClient instance.
                    .uri(url) // Specifies the target URL for the request.
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve() // Executes the request and retrieves the response.
                    .bodyToMono(UserDTO.class) // Instructs WebClient to convert the response body into a Mono that emits a single UserDTO object
                    .block(); // Wait for the response (blocking, not reactive), This means the program execution will pause at this line until the HTTP request and response processing are finished.
        } catch (WebClientResponseException ex) { // Here we catch 401, 403
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) { // here 500, 502(Bad-gateway, Like no user exist with id=50)
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

    public void blacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/blacklist";
        try {
            webClient.put() // Initiating a PUT request.
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON) // Here we are explicitly mentioning to set the content type to JSON.
                    .bodyValue(new UserIdRequest(userId)) // Here we are setting up th request body, that is userId, bodyValue() converts the object into JSON automatically.
                    .retrieve() // It's responsible to retrieve the response.
                    .bodyToMono(Void.class)// here we are expecting the void response (that is no content from the server)
                    .block(); // Blocks the current thread until the request completes, This converts the reactive non-blocking call into a synchronous call
        } catch (WebClientResponseException ex) {
            throw new RemoteUserServiceException(extractMessage(ex));
        } catch (Exception ex) {
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }

    public void unblacklistUser(Long userId, String authHeader) {
        String url = userServiceUrl + "/api/admin/users/unblacklist";
        try {
            webClient.put() // Initiating a PUT request
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON) // Indicates the server that the body is of JSON type.
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


/*
URI vs URL

A URI is a general term for a string of characters used to identify a resource.
It can identify a resource by its name (URN) or by its location (URL), or both.
 */