package com.example.orderingapp.dto;

public class UserUpdateRequest {
    private Employee oldUser;
    private Employee newUser;

    public UserUpdateRequest(Employee oldUser, Employee newUser) {
        this.oldUser = oldUser;
        this.newUser = newUser;
    }

    public Employee getOldUser() {
        return oldUser;
    }

    public void setOldUser(Employee oldUser) {
        this.oldUser = oldUser;
    }

    public Employee getNewUser() {
        return newUser;
    }

    public void setNewUser(Employee newUser) {
        this.newUser = newUser;
    }
}
