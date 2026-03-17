package com.nedap.workshop.syntaxis.api.json;

import com.nedap.workshop.syntaxis.domain.Client;

public class JsonClient {
    private Long id;
    private String firstName;
    private String lastName;

    public JsonClient() {}

    public JsonClient(Client client) {
        this.id = client.getId();
        this.firstName = client.getFirstName();
        this.lastName = client.getLastName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
