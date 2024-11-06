package com.example.exercise.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PersonDTO {
    private long id;
    private String firstName;
    private String lastName;
    private Integer age;
    private String taxNumber;

    public PersonDTO(long id, String firstName, String lastName, Integer age, String taxNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.taxNumber = taxNumber;
    }


}
