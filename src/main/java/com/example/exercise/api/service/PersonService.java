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
                        .build())
                .collect(Collectors.toList());
    }


    public Optional<PersonDTO> findPersonByTaxNumber(String taxNumber) {
        Person person = personRepository.findByTaxNumber(taxNumber);
        PersonDTO personDTO = PersonDTO.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .taxNumber(person.getTaxNumber())
                .build();
        Integer age = calculateAge(person.getBirthDate());
        personDTO.setAge(age);
        return Optional.of(personDTO);
    }

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    public Optional<Person> updatePerson(String taxNumber, Person updatedPerson) {
        Optional<Person> existingPersonOptional = Optional.ofNullable(personRepository.findByTaxNumber(taxNumber));
        if (existingPersonOptional.isPresent()) {
            Person existingPerson = existingPersonOptional.get();
            existingPerson.setFirstName(updatedPerson.getFirstName());
            existingPerson.setLastName(updatedPerson.getLastName());
            existingPerson.setBirthDate(updatedPerson.getBirthDate());
            return Optional.of(personRepository.save(existingPerson));
        }
        return Optional.empty();
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
