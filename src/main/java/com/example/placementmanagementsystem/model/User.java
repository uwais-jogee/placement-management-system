package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User model, implements UserDetails to allow Spring Security to authenticate users with this model
 */
@Entity(name = "PMS_User") // User is a reserved keyword in some databases, PMS_User is used as the table name
@Inheritance(strategy = InheritanceType.JOINED)
// Other user classes that extend user has its own table, with a foreign key back to the user table
public class User implements UserDetails {

    @Id
    private String username;
    private LocalDateTime created;
    private LocalDateTime lastLogin;
    private String password;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true; // Used as an 'Active' flag. Inactive users have not been verified by email and do not have a password set
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(name = "user_notifications", joinColumns = @JoinColumn(name = "username"), inverseJoinColumns = @JoinColumn(name = "notification_id"))
    private List<Notification> notifications = new ArrayList<>();
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "password_reset_email_token_token")
    private EmailToken passwordResetEmailToken;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_activation_email_token_token")
    private EmailToken accountActivationEmailToken;

    public User() {
        this.created = LocalDateTime.now();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(this.role);
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public EmailToken getPasswordResetEmailToken() {
        return passwordResetEmailToken;
    }

    public void setPasswordResetEmailToken(EmailToken passwordResetEmailToken) {
        this.passwordResetEmailToken = passwordResetEmailToken;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public EmailToken getAccountActivationEmailToken() {
        return accountActivationEmailToken;
    }

    public void setAccountActivationEmailToken(EmailToken accountActivationEmailToken) {
        this.accountActivationEmailToken = accountActivationEmailToken;
    }
}