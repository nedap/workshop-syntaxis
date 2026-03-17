package com.nedap.workshop.syntaxis.repository;

import com.nedap.workshop.syntaxis.ApplicationTest;
import com.nedap.workshop.syntaxis.domain.Client;
import com.nedap.workshop.syntaxis.domain.Composition;
import com.nedap.workshop.syntaxis.util.ExampleData;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ApplicationTest
public class CompositionRepositoryTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompositionRepository compositionRepository;

    private Client client;

    @BeforeEach
    public void setup() {
        client = clientRepository.save(new Client("John", "Doe"));
    }

    @AfterEach
    public void tearDown() {
        clientRepository.delete(client);
    }

    @Test
    void shouldFailWithoutClient() {
        Composition composition = new Composition();
        assertThrows(DataIntegrityViolationException.class, () -> compositionRepository.save(composition));
    }

    @Transactional
    @Test
    void shouldSaveAndFindComposition() {
        Composition composition = new Composition(client, ExampleData.BODY_WEIGHT_RM_OBJECT);
        Composition savedComposition = compositionRepository.save(composition);

        // Composition is saved with correct data
        assertNotNull(savedComposition.getId());
        assertEquals(ExampleData.BODY_WEIGHT_RM_OBJECT, savedComposition.getRmObject());

        // Composition can be found via the composition repository
        Composition foundComposition = compositionRepository.findById(savedComposition.getId()).orElseThrow();
        assertEquals(savedComposition.getRmObject(), foundComposition.getRmObject());
        assertNotNull(foundComposition.getClient());
        assertEquals("John", foundComposition.getClient().getFirstName());
        assertEquals("Doe", foundComposition.getClient().getLastName());

        // Composition can be found via client
        entityManager.flush();
        entityManager.refresh(client);
        List<Composition> clientCompositions = client.getCompositions();
        assertNotNull(clientCompositions);
        assertEquals(1, clientCompositions.size());
    }
}
