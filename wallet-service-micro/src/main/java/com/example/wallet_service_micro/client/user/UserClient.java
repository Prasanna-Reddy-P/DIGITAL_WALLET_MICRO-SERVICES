package com.example.wallet_service_micro.client.user;

import com.example.wallet_service_micro.dto.user.UserDTO;
import com.example.wallet_service_micro.dto.user.UserInfoResponse;
import com.example.wallet_service_micro.exception.RemoteUserServiceException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserClient {

    private final RestTemplate restTemplate; // This object is used to send HTTP requests to other services (GET, POST, PUT, DELETE, etc.).
    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Fetch single user by ID safely from user-service
    public UserDTO getUserById(Long userId, String authHeader) {
        String url = "http://localhost:8085/api/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        /*
HttpHeaders.AUTHORIZATION is a constant representing the header name "Authorization".

authHeader is a String variable that is expected to contain the actual authorization token or credentials (e.g., a Bearer token,)

HttpEntity is a generic class in Spring Framework that represents an HTTP request or response entity, consisting of headers and an optional body.

<Void> as the generic type indicates that this HttpEntity does not have a request body. It only contains headers.

The headers object, containing the Authorization header, is passed to the HttpEntity constructor.

 */

        try {
            ResponseEntity<UserDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UserDTO.class
            );
            return response.getBody();

        } catch (HttpClientErrorException.NotFound ex) {
            // 🟥 User not found in user-service
            throw new RemoteUserServiceException("User not found with id: " + userId);

        } catch (HttpClientErrorException.Unauthorized ex) {
            // 🟧 Unauthorized (invalid or missing JWT)
            throw new RemoteUserServiceException("Unauthorized to access user-service: " + ex.getMessage());

        } catch (HttpClientErrorException.Forbidden ex) {
            // 🟨 Forbidden (insufficient permissions)
            throw new RemoteUserServiceException("Access denied to user-service: " + ex.getMessage());

        } catch (HttpClientErrorException ex) {
            // 🟦 Other 4xx (Bad request, etc.)
            throw new RemoteUserServiceException("Client error from user-service: " + ex.getResponseBodyAsString());

        } catch (HttpServerErrorException ex) {
            // Directly propagate the exact JSON body
            throw new RemoteUserServiceException(ex.getResponseBodyAsString());

        } catch (ResourceAccessException ex) {
            // 🟫 user-service is down or unreachable
            throw new RemoteUserServiceException("User-service is unavailable: " + ex.getMessage());
        }
    }


    // ✅ Fetch all users — FIXED: include Authorization header
    public List<UserDTO> getAllUsers(String authHeader) {
        String url = "http://localhost:8085/api/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserDTO[].class
        );

        UserDTO[] usersArray = response.getBody();
        return usersArray != null ? List.of(usersArray) : List.of();
    }

    // ✅ Fetch user info from token
    public UserDTO getUserFromToken(String authHeader) {
        String url = "http://localhost:8085/api/users/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, UserDTO.class);

        return response.getBody();
    }
}
