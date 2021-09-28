package com.drbaltar.crudapicheckpoint.Models;

import com.drbaltar.crudapicheckpoint.Views.UserViews;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue
    @JsonView(UserViews.UserView.class)
    private Long id;
    @JsonView(UserViews.UserView.class)
    private String email;
    private String password;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
