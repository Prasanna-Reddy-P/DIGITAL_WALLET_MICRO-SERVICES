package com.example.user_service_micro.repository;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // sets up in-memory DB, transactional rollback after each test
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("Prasanna");
        user.setEmail("prasanna@example.com");
        user.setPassword("password123");
        user.setAge(25);
        user.setRole("USER");

        userRepository.save(user);
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setName("John");
        newUser.setEmail("john@example.com");
        newUser.setPassword("pass123");
        newUser.setAge(30);
        newUser.setRole("USER");

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testFindByEmail_UserExists() {
        Optional<User> found = userRepository.findByEmail("prasanna@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Prasanna");
    }

    @Test
    void testFindByEmail_UserDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void testFindById_UserExists() {
        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("prasanna@example.com");
    }

    @Test
    void testFindById_UserDoesNotExist() {
        Optional<User> found = userRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdateUser() {
        user.setName("Prasanna Reddy");
        User updatedUser = userRepository.save(user);

        assertThat(updatedUser.getName()).isEqualTo("Prasanna Reddy");
    }

    @Test
    void testDeleteUser() {
        userRepository.delete(user);
        Optional<User> deleted = userRepository.findById(user.getId());
        assertThat(deleted).isEmpty();
    }
}
