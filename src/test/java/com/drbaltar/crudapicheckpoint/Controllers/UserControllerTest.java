package com.drbaltar.crudapicheckpoint.Controllers;

import com.drbaltar.crudapicheckpoint.Models.User;
import com.drbaltar.crudapicheckpoint.Repositories.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository repository;

    @Test
    @Transactional
    @Rollback
    void shouldGetListOfUsersFromDB() throws Exception {
        User testUser1 = saveNewUserToDB("john@example.com", "password");
        User testUser2 = saveNewUserToDB("eliza@example.com", "password");
        String expected = """
                  [
                    {
                      "id": %d,
                      "email": "john@example.com"
                    },
                    {
                      "id": %d,
                      "email": "eliza@example.com"
                    }
                  ]
                """.formatted(testUser1.getId(), testUser2.getId());

        var request = get("/users");

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expected))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    @Transactional
    @Rollback
    void shouldSaveNewUserToDB() throws Exception {
        String testJSON = """
                {
                    "email": "john@example.com",
                    "password": "something-secret"
                } 
                """;

        var request = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJSON);

        MvcResult results = mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", instanceOf(Number.class)))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        var responseBody = results.getResponse().getContentAsString();
        int newUserID = JsonPath.parse(responseBody).read("$.id");
        var newUserInDB = repository.findById(Long.valueOf(newUserID)).get();
        assertEquals("something-secret", newUserInDB.getPassword());
    }

    @Test
    @Transactional
    @Rollback
    void shouldRetrieveUserByID() throws Exception {
        var testID = saveNewUserToDB("john@example.com", "password").getId();
        String expected = """
                {
                  "id": %d,
                  "email": "john@example.com"
                } 
                """.formatted(testID);

        var request = get("/users/%d".formatted(testID));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expected))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Transactional
    @Rollback
    void shouldPatchUserEntryWIthJsonWIthBothProperties() throws Exception {
        var testUser = saveNewUserToDB("john@example.com", "password1");

        var testJSON = """
                {
                    "email": "rob@example.com",
                    "password": "password2"
                }
                """;
        var expected = """
                {
                    "email": "rob@example.com",
                    "id": %d
                }
                """.formatted(testUser.getId());

        var request = patch("/users/%d".formatted(testUser.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expected))
                .andExpect(jsonPath("$.password").doesNotExist());

        var updatedUser = repository.findById(testUser.getId()).get();
        assertEquals("password2", updatedUser.getPassword());
    }

    @Test
    @Transactional
    @Rollback
    void shouldDeleteUserByID() throws Exception {
        saveNewUserToDB("june@gmail.com", "password1");
        var testUserID = saveNewUserToDB("robert@gmail.com", "password1").getId();

        var expected = """
                {
                    "count": 1
                }
                """;

        var request = delete("/users/%d".formatted(testUserID));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expected));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"email\": \"angelica@example.com\",\"password\": \"1234\"}={ \"authenticated\": true,\"user\": {\"id\": %d, \"email\": \"angelica@example.com\"}}",
            "{\"email\": \"angelica@example.com\",\"password\": \"1235\"}={ \"authenticated\": false}"
    }, delimiter = '=')
    @Transactional
    @Rollback
    void shouldReturnJSONShowingUserIsAuthenticated(String testJSON, String expected) throws Exception {
        var testUserID = saveNewUserToDB("angelica@example.com", "1234").getId();

        var request = post("/users/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(expected.formatted(testUserID)));
    }

    private User saveNewUserToDB(String emailAddress, String password) {
        User testUser = new User();
        testUser.setEmail(emailAddress);
        testUser.setPassword(password);
        return repository.save(testUser);
    }
}
