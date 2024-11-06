package com.example.exercise.api.consumer;

import com.example.exercise.api.controller.PersonController;
import com.example.exercise.api.model.Person;
import com.example.exercise.api.service.PersonService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    PersonService personService;

    public MessageConsumer(PersonService personService) {
        this.personService = personService;
    }

    @KafkaListener(topics = "person-topic")
    public void listen(ConsumerRecord<String, Person> record) {
        String key = record.key();
        Person person = record.value();

        if (key.equals("CREATE")) {
            personService.createPerson(person);
        }
        else if (key.startsWith("UPDATE-")) {
            String taxNumber = key.substring("UPDATE-".length());
            personService.updatePerson(taxNumber, person);
        }
        else if (key.startsWith("DELETE-")) {
            String taxNumber = key.substring("UPDATE-".length());
            personService.deletePerson(taxNumber);
        }
    }

}
