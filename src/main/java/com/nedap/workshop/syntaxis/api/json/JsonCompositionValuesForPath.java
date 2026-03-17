package com.nedap.workshop.syntaxis.api.json;

import java.util.ArrayList;
import java.util.List;

public class JsonCompositionValuesForPath {
    private String path;
    private List<Object> values;

    public JsonCompositionValuesForPath() {
    }

    public JsonCompositionValuesForPath(String path) {
        this.path = path;
        this.values = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public void addValue(Object object) {
        this.values.add(object);
    }
}
