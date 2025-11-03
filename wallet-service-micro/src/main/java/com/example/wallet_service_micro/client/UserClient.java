package com.example.wallet_service_micro.client;

import com.example.wallet_service_micro.dto.UserDTO;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class UserClient {

    private final RestTemplate restTemplate;

    public UserClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Fetch single user by ID
    public UserDTO getUserById(Long userId, String authHeader) {
        String url = "http://localhost:8085/api/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserDTO.class
        );

        return response.getBody();
    }

    // ✅ Fetch all users — FIXED: include Authorization header
    public List<UserDTO> getAllUsers(String authHeader) {
        String url = "http://localhost:8085/api/users";

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
