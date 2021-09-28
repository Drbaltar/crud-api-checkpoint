package com.drbaltar.crudapicheckpoint.Repositories;

import com.drbaltar.crudapicheckpoint.Models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
