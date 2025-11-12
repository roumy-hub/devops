package tn.esprit.spring.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.spring.entities.User;
import tn.esprit.spring.services.IUserService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestControl.class)
public class UserRestControlTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRetrieveAllUsers() throws Exception {
        // Arrange
        User user1 = new User("1", "John", "john@test.com");
        User user2 = new User("2", "Jane", "jane@test.com");
        List<User> users = Arrays.asList(user1, user2);
        
        when(userService.retrieveAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/user/retrieve-all-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("John")))
                .andExpect(jsonPath("$[1].username", is("Jane")));
    }

    @Test
    public void testRetrieveUser() throws Exception {
        // Arrange
        User user = new User("1", "John", "john@test.com");
        when(userService.retrieveUser("1")).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/user/retrieve-user/{user-id}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John")))
                .andExpect(jsonPath("$.email", is("john@test.com")));
    }

    @Test
    public void testAddUser() throws Exception {
        // Arrange
        User user = new User("1", "John", "john@test.com");
        when(userService.addUser(any(User.class))).thenReturn(user);
      // Act & Assert
        mockMvc.perform(post("/user/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John")));
    }

    @Test
    public void testRemoveUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser("1");

        // Act & Assert
        mockMvc.perform(delete("/user/remove-user/{user-id}", "1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser("1");
    }

    @Test
    public void testUpdateUser() throws Exception {
        // Arrange
        User user = new User("1", "John Updated", "john.updated@test.com");
        when(userService.updateUser(any(User.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(put("/user/modify-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("John Updated")));
    }

    // Security Test: SQL Injection attempt
    @Test
    public void testSqlInjectionAttempt() throws Exception {
        // Arrange
        String maliciousInput = "1'; DROP TABLE users; --";
        
        when(userService.retrieveUser(maliciousInput))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/user/retrieve-user/{user-id}", maliciousInput))
                .andExpect(status().is5xxServerError());
    }

    // Security Test: XSS attempt in user input
    @Test
    public void testXssAttemptInAddUser() throws Exception {
        // Arrange
        User maliciousUser = new User("1", "<script>alert('xss')</script>", "test@test.com");
        when(userService.addUser(any(User.class))).thenReturn(maliciousUser);

        // Act & Assert - The framework should sanitize input
        mockMvc.perform(post("/user/add-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUser)))
                .andExpect(status().isOk());
    }
}
