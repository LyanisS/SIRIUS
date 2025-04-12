package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Set;

@JsonRootName(value = "person")
public class Person {
    @JsonProperty("login")
    private String login;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("roles")
    private Set<Role> roles;

    public Person() {}

    public Person(String login, String lastName, String firstName) {
        this(login, lastName, firstName, null);
    }

    public Person(String login, String lastName, String firstName, Set<Role> roles) {
        this.login = login;
        this.lastName = lastName;
        this.firstName = firstName;
        this.roles = roles;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "Person{" +
                "login=" + this.login +
                ", lastName=" + this.lastName +
                ", firstName=" + this.firstName +
                ", roles=" + this.roles +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Person p = (Person) o;
        return p.getLogin().equals(this.login);
    }
}
