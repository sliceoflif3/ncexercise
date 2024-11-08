package com.example.exercise.api.controller;

import com.example.exercise.api.dto.PersonDTO;
import com.example.exercise.api.model.Person;
import com.example.exercise.api.producer.MessageProducer;
import com.example.exercise.api.repository.PersonRepository;
import com.example.exercise.api.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;
//
//    public PersonController(PersonService personService) {
//        this.personService = personService;
//    }

    @Autowired
    private MessageProducer messageProducer;

    @GetMapping("/person")
    public ResponseEntity<PersonDTO> findPersonByTaxNumber(@RequestParam String taxNumber) {
        PersonDTO personDTO = personService.findPersonByTaxNumber(taxNumber);
        return new ResponseEntity<>(personDTO, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Person> createPerson(@RequestBody Person person) {
        person.setTaxDebt(0.0);
//        for(int i = 0; i < 11; i++) {
//            person.setTaxNumber((i+10)+"");
//            messageProducer.sendMessage("person-topic","CREATE",person);
//            System.out.println("sent " +  person.getTaxNumber());
//        }
        messageProducer.sendMessage("person-topic","CREATE",person);
//        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(person));

        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @GetMapping("/persons")
    public ResponseEntity<List<PersonDTO>> findAllPersons() {
        List<PersonDTO> persons = personService.getAllPersons();
        return ResponseEntity.status(HttpStatus.OK).body(persons);
    }

    @PutMapping("/update")
    public ResponseEntity<Person> updatePerson(@RequestParam String taxNumber,@RequestBody Person person) {
//        Optional<Person> updatedPerson = personService.updatePerson(taxNumber, person);
//        return updatedPerson.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        messageProducer.sendMessage("person-topic", "UPDATE-" + taxNumber, person);
        return ResponseEntity.status(HttpStatus.OK).body(person);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Person> deletePerson(@RequestParam String taxNumber) {
//        personService.deletePerson(taxNumber);
        messageProducer.sendMessage("person-topic","DELETE-" + taxNumber, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/step2")
    public ResponseEntity<List<PersonDTO>> findPersonNameStartsWithMiAndOlderThan30() {
        List<PersonDTO> persons = personService.getAllPersons2();
        return ResponseEntity.status(HttpStatus.OK).body(persons);
    }

    @PutMapping("/calculateTax")
    public ResponseEntity<Person> calculateTax(@RequestParam String taxNumber, @RequestParam Double taxAmount) {
        messageProducer.sendMessage("tax-calculation-topic","CALCULATETAX-" + taxNumber + "-" + taxAmount, null);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @PostMapping("/send")
//    public String sendMessage(@RequestParam("message") String message) {
//        messageProducer.sendMessage("my-topic", message);
//        return "Message sent: " + message;
//    }
}
