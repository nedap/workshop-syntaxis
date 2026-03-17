package com.nedap.workshop.syntaxis.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nedap.archie.json.JacksonUtil;
import jakarta.persistence.*;

@Entity
public class Composition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Client client;

    private String rmObject;

    @Transient
    private com.nedap.archie.rm.composition.Composition composition;

    public Composition() {}

    public Composition(Client client, String rmObject) {
        this.client = client;
        this.rmObject = rmObject;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getRmObject() {
        return rmObject;
    }

    public void setRmObject(String rmObject) {
        this.rmObject = rmObject;
    }

    public com.nedap.archie.rm.composition.Composition getComposition() throws JsonProcessingException {
        if (composition == null) {
            var objectMapper = JacksonUtil.getObjectMapper();
            composition = objectMapper.readValue(rmObject, com.nedap.archie.rm.composition.Composition.class);
        }
        return composition;
    }
}
