package tn.esprit.spring.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.entities.User;
import tn.esprit.spring.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testRetrieveAllUsers() {
        // Arrange
        User user1 = new User("1", "John", "john@test.com");
        User user2 = new User("2", "Jane", "jane@test.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<User> users = userService.retrieveAllUsers();

        // Assert
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testRetrieveUser_Success() {
        // Arrange
        User user = new User("1", "John", "john@test.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // Act
        User result = userService.retrieveUser("1");

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getUsername());
        verify(userRepository, times(1)).findById("1");
    }

    @Test
    public void testRetrieveUser_NotFound() {
        // Arrange
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.retrieveUser("999"));
    }

    @Test
    public void testAddUser() {
        // Arrange
        User user = new User("1", "John", "john@test.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.addUser(user);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteById("1");

        // Act
        userService.deleteUser("1");

        // Assert
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        User existingUser = new User("1", "John", "john@test.com");
        User updatedUser = new User("1", "John Updated", "john.updated@test.com");
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        User result = userService.updateUser(updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("John Updated", result.getUsername());
        verify(userRepository, times(1)).save(updatedUser);
    }
}
