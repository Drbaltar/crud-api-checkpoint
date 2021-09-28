package com.drbaltar.crudapicheckpoint.Models;

public class AuthenticatedUser {
    private boolean authenticated;
    private User user;

    public AuthenticatedUser() {
    }

    public AuthenticatedUser(User user) {
        this.user = user;
        authenticated = true;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
