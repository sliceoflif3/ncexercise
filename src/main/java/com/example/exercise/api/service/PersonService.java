package com.example.exercise.api.service;

import com.example.exercise.api.dto.PersonDTO;
import com.example.exercise.api.model.Person;
import com.example.exercise.api.repository.PersonRepository;
import lombok.Builder;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Builder
public class PersonService {
    private final PersonRepository personRepository;

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<PersonDTO> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(person -> PersonDTO.builder()
                        .firstName(person.getFirstName())
                        .lastName(person.getLastName())
                        .taxNumber(person.getTaxNumber())
                        .age(calculateAge(person.getBirthDate()))
                        .taxDebt(person.getTaxDebt())
                        .build())
                .collect(Collectors.toList());
    }


    public PersonDTO findPersonByTaxNumber(String taxNumber) {
        Person person = personRepository.findByTaxNumber(taxNumber);
        PersonDTO personDTO = PersonDTO.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .taxNumber(person.getTaxNumber())
                .taxDebt(person.getTaxDebt())
                .build();
        Integer age = calculateAge(person.getBirthDate());
        personDTO.setAge(age);
        return personDTO;
    }

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    public Person updatePerson(String taxNumber, Person updatedPerson) {
            Person existingPerson = personRepository.findByTaxNumber(taxNumber);
            existingPerson.setFirstName(updatedPerson.getFirstName());
            existingPerson.setLastName(updatedPerson.getLastName());
            existingPerson.setBirthDate(updatedPerson.getBirthDate());
            return personRepository.save(existingPerson);
    }

    public Person calculatePersonTax(String taxNumber, Double taxAmount) {
        Person existingPerson = personRepository.findByTaxNumber(taxNumber);
        existingPerson.setTaxDebt(existingPerson.getTaxDebt() + taxAmount);
        return personRepository.save(existingPerson);
    }

    @Transactional
    public void deletePerson(String taxNumber) {
        personRepository.deleteByTaxNumber(taxNumber);
    }

    public List<PersonDTO> getAllPersons2() {
        return personRepository.findAll()
                .stream()
                .filter(person -> (person.getFirstName().startsWith("Mi") || person.getLastName().startsWith("Mi"))
                && calculateAge(person.getBirthDate()) > 30)
                .map(person -> PersonDTO.builder()
                        .firstName(person.getFirstName())
                        .lastName(person.getLastName())
                        .taxNumber(person.getTaxNumber())
                        .age(calculateAge(person.getBirthDate()))
                        .build())
                .collect(Collectors.toList());
    }


}
