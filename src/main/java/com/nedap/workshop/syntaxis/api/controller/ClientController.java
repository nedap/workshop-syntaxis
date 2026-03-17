package com.nedap.workshop.syntaxis.api.controller;

import com.nedap.workshop.syntaxis.api.json.JsonClient;
import com.nedap.workshop.syntaxis.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public ResponseEntity<List<JsonClient>> all() {
        return ResponseEntity.ok(
                clientRepository.findAll()
                        .stream().map(JsonClient::new)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonClient> byId(@PathVariable("id") Long id) {
        var client = clientRepository.findById(id);
        return client
                .map(JsonClient::new)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
