package com.swp2.demo.web;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterUser {
// NoyBlank ko dc bỏ trống
    @NotBlank(message = "thông tin bắt buộc")
    @Size(min = 1, message = "tối thiểu 1")
    private String username;

    @NotBlank(message = "thông tin bắt buộc")
    @Size(min = 1, message = "tối thiểu 1")

    private String password;


    private String email;


    @NotBlank(message = "thông tin bắt buộc")
    private String firstName;

    @NotBlank(message = "thông tin bắt buộc")
    private String lastName;

    public RegisterUser() {
    }

    public RegisterUser(String username, String password, String email, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
