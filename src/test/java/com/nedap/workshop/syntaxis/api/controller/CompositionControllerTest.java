package com.nedap.workshop.syntaxis.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nedap.workshop.syntaxis.ApplicationTest;
import com.nedap.workshop.syntaxis.domain.Client;
import com.nedap.workshop.syntaxis.repository.ClientRepository;
import com.nedap.workshop.syntaxis.repository.CompositionRepository;
import com.nedap.workshop.syntaxis.util.ExampleData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApplicationTest
class CompositionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CompositionRepository compositionRepository;

    @Autowired
    private CompositionController compositionController;

    private Client client;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(compositionController).build();
        clientRepository.deleteAll();
        compositionRepository.deleteAll();
        client = clientRepository.save(new Client("John", "Doe"));
    }

    @Test
    void shouldGetValuesForPathsBatch() throws Exception {
        // First create a composition
        String validRmObject = ExampleData.BODY_WEIGHT_RM_OBJECT;
        com.nedap.workshop.syntaxis.domain.Composition composition = compositionRepository.save(
                new com.nedap.workshop.syntaxis.domain.Composition(client, validRmObject)
        );

        mockMvc.perform(get("/compositions/values_for_paths")
                        .queryParam("ids", composition.getId().toString())
                        .queryParam("paths", "/name/value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
