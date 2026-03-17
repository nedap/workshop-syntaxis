package com.nedap.workshop.syntaxis.api.controller;

import com.nedap.workshop.syntaxis.ApplicationTest;
import com.nedap.workshop.syntaxis.domain.Client;
import com.nedap.workshop.syntaxis.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApplicationTest
class ClientControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientController clientController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
    }

    @Test
    void shouldReturnAllClients() throws Exception {
        clientRepository.deleteAll();
        clientRepository.save(new Client("John", "Doe"));
        clientRepository.save(new Client("Jane", "Smith"));

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }
}
