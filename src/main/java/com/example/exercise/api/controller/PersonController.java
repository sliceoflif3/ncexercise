package com.example.exercise.api.controller;

import com.example.exercise.api.consumer.MessageConsumer;
import com.example.exercise.api.dto.PersonDTO;
import com.example.exercise.api.model.KafkaResponse;
import com.example.exercise.api.model.Person;
import com.example.exercise.api.producer.MessageProducer;
import com.example.exercise.api.repository.PersonRepository;
import com.example.exercise.api.service.PersonService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

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
    @Autowired
    private MessageConsumer messageConsumer;

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
//
//    @Autowired
//    private KafkaTemplate<String, Person> kafkaTemplate;
//
//    @Autowired
//    private ConsumerFactory<String, String> consumerFactory;

    @GetMapping("/consume")
    public KafkaResponse consumeEvents(@RequestParam(defaultValue = "10") int maxEvents) {
        messageProducer.sendMessage("ping-topic","test",null);
        List<String> events = new ArrayList<>();
        int messagesLeft = 0;
        boolean hasMoreMessages = false;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group-id");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("ping-topic"));
            var records = consumer.poll(Duration.ofSeconds(1));
            var count = 0;

            for (ConsumerRecord<String, String> record : records) {
                if (count < maxEvents) {
                    events.add(record.value());
                    count++;
                } else {
                    hasMoreMessages = true;
                    break;
                }
            }
            messagesLeft = records.count() - count;
            consumer.commitSync();
        }

        System.out.println(messagesLeft);

        return new KafkaResponse(events, messagesLeft, hasMoreMessages);


    }

}
