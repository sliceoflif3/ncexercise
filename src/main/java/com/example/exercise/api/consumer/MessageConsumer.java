package com.example.exercise.api.consumer;

import com.example.exercise.api.controller.PersonController;
import com.example.exercise.api.model.Person;
import com.example.exercise.api.service.PersonService;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageConsumer {

    PersonService personService;

    public MessageConsumer(PersonService personService) {
        this.personService = personService;
    }

    @KafkaListener(topics = "person-topic")
//    @RetryableTopic(
//            backoff = @Backoff(delay = 1000L,
//                    multiplier = 2 ),
//            attempts = "5"
//    )
    public void listenPerson(List<ConsumerRecord<String, Person>> records, Acknowledgment ack) throws ExecutionException, InterruptedException {
        System.out.println("Start Consume 1 batch that contains " + records.size() + " records");
        for (ConsumerRecord<String, Person> record : records) {
                try {

                    System.out.println("Process record number " + records.indexOf(record));
                    processRecord(record);

//                    String topic = record.topic();
//                    String key = record.key();
//                    Person person = record.value();
//
//
//                    if (key.equals("CREATE")) {
//                        personService.createPerson(person);
//                    }
//                    else if (key.startsWith("UPDATE-")) {
//                        String taxNumber = key.substring("UPDATE-".length());
//                        personService.updatePerson(taxNumber, person);
//                    }
//                    else if (key.startsWith("DELETE-")) {
//                        String taxNumber = key.substring("DELETE-".length());
//                        personService.deletePerson(taxNumber);
//                    }

                }
                catch (Exception e) {
                    System.err.println("Error processing record: " + record.value());
                    handleFailedRecord(record);
                }
            }
        ack.acknowledge();
    }

    private void handleFailedRecord(ConsumerRecord<String, Person> record) {
        System.err.println("Sending record to DLT: " + record.value());
    }

    private void processRecord(ConsumerRecord<String, Person> record) throws ExecutionException, InterruptedException
    {
        String key = record.key();
        Person person = record.value();

        if (isDependentEvent(key)) {
            retryBlocking(() -> handleEvent(key, person));
        } else {
            retryNonBlocking(() -> handleEvent(key, person));
        }

    }

    private boolean isDependentEvent(String key) {
        return key.equals("CREATE");
    }

    private void handleEvent(String key, Person person) {
        if (key.equals("CREATE")) {
            personService.createPerson(person);
        }
        else if (key.startsWith("UPDATE-")) {
            String taxNumber = key.substring("UPDATE-".length());
            personService.updatePerson(taxNumber, person);
        }
        else if (key.startsWith("DELETE-")) {
            String taxNumber = key.substring("DELETE-".length());
            personService.deletePerson(taxNumber);
        } else {
            System.out.println("UNKNOWN KEY");
        }
    }

    private void retryBlocking(Runnable task) {
        int maxAttempts = 5;
        long delay = 1000L;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                task.run();
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                System.out.println("Retrying blocking " + attempt);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(interruptedException);
                }
                delay *= 2;
            }
        }
    }

//    private void retryNonBlocking(Runnable task) throws ExecutionException, InterruptedException {
//        int maxAttempts = 5;
//        long delay = 1000L;
//        AtomicInteger attempt = new AtomicInteger(0);
//
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            while (attempt.get() < maxAttempts) {
//                try {
//                    task.run();
//                    return;
//                } catch (Exception e) {
//                    attempt.incrementAndGet();
//                    if (attempt.get() >= maxAttempts) {
//                        throw new RuntimeException(e);
//                    }
//                    System.err.println("Retrying non blocking " + attempt.get());
//                    try {
//                        Thread.sleep(1000L);
//                    } catch (InterruptedException interruptedException) {
//                        Thread.currentThread().interrupt();
//                        throw new RuntimeException(interruptedException);
//                    }
//                }
//            }
//        });
//        future.get();
//    }

    private void retryNonBlocking(Runnable task) {
        AtomicInteger attempt = new AtomicInteger(0);
        long initialDelay = 1000L;
        int maxAttempts = 5;

        scheduleRetry(task, attempt, initialDelay, maxAttempts);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private void scheduleRetry(Runnable task, AtomicInteger attempt, long initialDelay, int maxAttempts) {
        scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (attempt.incrementAndGet() < maxAttempts) {
                    System.err.println("Retrying (non-blocking) attempt " + attempt.get());
                    scheduleRetry(task, attempt, initialDelay * 2, maxAttempts);
                } else {
                    System.err.println("Max retry attempts reached. Task failed.");
                }
            }
        }, initialDelay, TimeUnit.MILLISECONDS);
    }

    @KafkaListener(topics = "tax-calculation-topic")
    public void listenTaxCalculation(ConsumerRecord<String, Person> record) {
        String topic = record.topic();
        String key = record.key();
        Person person = record.value();
        if (key.startsWith("CALCULATETAX")) {
            String[] parts = key.split("-");
            if (parts.length >= 3) { // Ensure correct parts count
                String taxNumber = parts[1];
                Double taxAmount = Double.valueOf(parts[2]);
                personService.calculatePersonTax(taxNumber, taxAmount);
            }
        }
    }

    @KafkaListener(topics = "person-topic.dlt")
    public void listenDlt(ConsumerRecord<String, String> record) {
        // Handle DLT records, e.g., manual intervention or inspection
        System.err.println("Received message in DLT: " + record.value());
    }

}
