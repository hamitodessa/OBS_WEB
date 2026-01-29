package com.hamit.obs.dto.user;

import java.util.List;

public class UserRowDTO {
	private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String adminHesap;
    private String calisandvzcins;
    private List<String> roles;

    public UserRowDTO() {}

    public UserRowDTO(Long id, String email, String firstName, String lastName,
                      String adminHesap, String calisandvzcins, List<String> roles) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.adminHesap = adminHesap;
        this.calisandvzcins = calisandvzcins;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAdminHesap() { return adminHesap; }
    public void setAdminHesap(String adminHesap) { this.adminHesap = adminHesap; }

    public String getCalisandvzcins() { return calisandvzcins; }
    public void setCalisandvzcins(String calisandvzcins) { this.calisandvzcins = calisandvzcins; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
