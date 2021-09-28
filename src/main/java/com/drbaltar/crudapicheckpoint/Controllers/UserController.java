package com.drbaltar.crudapicheckpoint.Controllers;

import com.drbaltar.crudapicheckpoint.Models.AuthenticatedUser;
import com.drbaltar.crudapicheckpoint.Models.User;
import com.drbaltar.crudapicheckpoint.Repositories.UserRepository;
import com.drbaltar.crudapicheckpoint.Views.UserViews;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @JsonView(UserViews.UserView.class)
    public Iterable<User> getListOfUsers() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @JsonView(UserViews.UserView.class)
    public Optional<User> getUserByID(@PathVariable Long id) {
        return repository.findById(id);
    }

    @PostMapping
    @JsonView(UserViews.UserView.class)
    public User saveNewUser(@RequestBody User newUser) {
        return repository.save(newUser);
    }

    @PatchMapping("/{id}")
    @JsonView(UserViews.UserView.class)
    public User updateUser(@PathVariable Long id, @RequestBody HashMap<String, String> changeMap) {
        Optional<User> userQuery = repository.findById(id);
        return userQuery
                .map(user -> updateUserFields(changeMap, user))
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public Map<String, Long> deleteUserByID(@PathVariable Long id) {
        repository.deleteById(id);
        return getCountMap();
    }

    @PostMapping("/authenticate")
    public AuthenticatedUser authenticateUser(@RequestBody User userLogin) {
        var userQuery = repository.findByEmail(userLogin.getEmail());
        return userQuery
                .filter(user -> user.getPassword().equals(userLogin.getPassword()))
                .map(AuthenticatedUser::new)
                .orElseGet(AuthenticatedUser::new);
    }

    private HashMap<String, Long> getCountMap() {
        HashMap<String, Long> countMap = new HashMap<>();
        countMap.put("count", repository.count());
        return countMap;
    }

    private User updateUserFields(HashMap<String, String> changeMap, User user) {
        changeMap.forEach((key, value) -> updateUserField(user, key, value));
        return user;
    }

    private void updateUserField(User user, String key, String value) {
        if (key.equals("email")) {
            user.setEmail(value);
        } else if (key.equals("password")) {
            user.setPassword(value);
        }
    }
}
