package com.example.exercise.api.repository;

import com.example.exercise.api.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Person findByTaxNumber(String taxNumber);
    void deleteByTaxNumber(String taxNumber);
}
