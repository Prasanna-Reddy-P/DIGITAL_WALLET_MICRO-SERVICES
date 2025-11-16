package com.example.user_service_micro.repository;

import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");
        user.setPassword("password123");
        user.setAge(25);
        user.setRole("USER");
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(user)).thenReturn(user);

        User saved = userRepository.save(user);

        assertNotNull(saved);
        assertEquals("Prasanna", saved.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindByEmail_UserExists() {
        when(userRepository.findByEmail("prasanna@example.com"))
                .thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findByEmail("prasanna@example.com");

        assertTrue(found.isPresent());
        assertEquals("Prasanna", found.get().getName());
    }

    @Test
    void testFindByEmail_UserDoesNotExist() {
        when(userRepository.findByEmail("none@example.com"))
                .thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByEmail("none@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindById_UserExists() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Optional<User> found = userRepository.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("prasanna@example.com", found.get().getEmail());
    }

    @Test
    void testFindById_UserNotFound() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        Optional<User> found = userRepository.findById(999L);

        assertFalse(found.isPresent());
    }
}
