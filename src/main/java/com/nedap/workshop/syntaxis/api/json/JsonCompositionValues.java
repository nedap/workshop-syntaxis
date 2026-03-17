package com.nedap.workshop.syntaxis.api.json;

import java.util.List;

public class JsonCompositionValues {
    private Long compositionId;
    private List<JsonCompositionValuesForPath> valuesForPath;

    public JsonCompositionValues() {
    }

    public JsonCompositionValues(Long compositionId) {
        this.compositionId = compositionId;
    }

    public Long getCompositionId() {
        return compositionId;
    }

    public void setCompositionId(Long compositionId) {
        this.compositionId = compositionId;
    }

    public List<JsonCompositionValuesForPath> getValuesForPath() {
        return valuesForPath;
    }

    public void setValuesForPath(List<JsonCompositionValuesForPath> valuesForPath) {
        this.valuesForPath = valuesForPath;
    }
}
