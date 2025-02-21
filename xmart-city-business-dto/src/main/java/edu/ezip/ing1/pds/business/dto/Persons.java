package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;
import java.util.Set;

public class Persons {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("persons")
    private Set<Person> persons = new LinkedHashSet<Person>();

    public Set<Person> getPersons() {
        return this.persons;
    }

    public void setPersons(Set<Person> persons) {
        this.persons = persons;
    }

    public final Persons add(final Person person) {
        this.persons.add(person);
        return this;
    }

    @Override
    public String toString() {
        return "Persons{" +
                "persons=" + this.persons +
                '}';
    }
}
