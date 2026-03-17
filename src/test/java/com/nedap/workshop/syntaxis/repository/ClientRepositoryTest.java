package com.nedap.workshop.syntaxis.repository;

import com.nedap.workshop.syntaxis.ApplicationTest;
import com.nedap.workshop.syntaxis.domain.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationTest
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void shouldSaveAndFindClient() {
        Client client = new Client("John", "Doe");
        Client savedClient = clientRepository.save(client);

        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getFirstName()).isEqualTo("John");
        assertThat(savedClient.getLastName()).isEqualTo("Doe");

        Client foundClient = clientRepository.findById(savedClient.getId()).orElseThrow();
        assertThat(foundClient.getFirstName()).isEqualTo("John");
        assertThat(foundClient.getLastName()).isEqualTo("Doe");
    }
}
