package com.nedap.workshop.syntaxis.api.json;

import com.nedap.workshop.syntaxis.domain.Composition;

public class JsonComposition {
    private Long id;
    private Long clientId;
    private String rmObject;

    public JsonComposition() {}

    public JsonComposition(Composition composition) {
        this.id = composition.getId();
        this.clientId = composition.getClient().getId();
        this.rmObject = composition.getRmObject();
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getRmObject() {
        return rmObject;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setRmObject(String rmObject) {
        this.rmObject = rmObject;
    }
}
