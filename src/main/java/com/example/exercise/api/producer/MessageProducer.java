package com.example.exercise.api.producer;

import com.example.exercise.api.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, Person> kafkaTemplate;

    public void sendMessage(String topic, String key, Person person) {
        kafkaTemplate.send(topic, key, person);
    }

//    public void sendUpdateMessage(String topic, String key, Person person) {
//        kafkaTemplate.send(topic, key, person);
//    }

}
