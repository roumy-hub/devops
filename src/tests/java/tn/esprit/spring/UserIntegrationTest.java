package tn.esprit.spring.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.spring.entities.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/user";
    }

    @Test
    public void testFullUserCRUDCycle() {
        // Create User
        User newUser = new User("100", "IntegrationTest", "integration@test.com");
        
        ResponseEntity<User> createResponse = restTemplate.postForEntity(
            getBaseUrl() + "/add-user", newUser, User.class);
        
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        // Retrieve User
        ResponseEntity<User> getResponse = restTemplate.getForEntity(
            getBaseUrl() + "/retrieve-user/100", User.class);
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("IntegrationTest", getResponse.getBody().getUsername());

        // Update User
        User updatedUser = new User("100", "IntegrationTestUpdated", "updated@test.com");
        ResponseEntity<User> updateResponse = restTemplate.exchange(
            getBaseUrl() + "/modify-user", HttpMethod.PUT, 
            new HttpEntity<>(updatedUser), User.class);
        
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

        // Delete User
        restTemplate.delete(getBaseUrl() + "/remove-user/100");

        // Verify deletion
        ResponseEntity<User> deletedResponse = restTemplate.getForEntity(
            getBaseUrl() + "/retrieve-user/100", User.class);
        
        // This might return 404 or throw exception depending on your implementation
        assertTrue(deletedResponse.getStatusCode() != HttpStatus.OK);
    }
}
