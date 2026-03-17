package com.nedap.workshop.syntaxis.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nedap.workshop.syntaxis.api.json.JsonComposition;
import com.nedap.workshop.syntaxis.api.json.JsonCompositionValues;
import com.nedap.workshop.syntaxis.domain.Composition;
import com.nedap.workshop.syntaxis.repository.ClientRepository;
import com.nedap.workshop.syntaxis.repository.CompositionRepository;
import com.nedap.workshop.syntaxis.service.RmObjectQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compositions")
public class CompositionController {
    @Autowired
    private CompositionRepository compositionRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RmObjectQueryService queryService;

    @GetMapping("/values_for_paths")
    public ResponseEntity<Map<Long, JsonCompositionValues>> getValuesForPathsBatch(@RequestParam List<Long> ids, @RequestParam List<String> paths) throws JsonProcessingException {
        List<Composition> compositions = compositionRepository.findAllById(ids);
        Map<Long, JsonCompositionValues> result = new HashMap<>();

        for (Composition composition : compositions) {
            JsonCompositionValues jsonCompositionValues = new JsonCompositionValues(composition.getId());
            jsonCompositionValues.setValuesForPath(queryService.queryPaths(paths, composition.getComposition()));
            result.put(composition.getId(), jsonCompositionValues);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/by_client_id/{id}")
    public ResponseEntity<List<JsonComposition>> byClientId(@PathVariable("id") Long clientId) {
        var client = clientRepository.findById(clientId);
        if (client.isEmpty()) return ResponseEntity.notFound().build();

        var compositions = compositionRepository.findAllByClient(client.get());
        return ResponseEntity.ok(
                compositions.stream()
                        .map(JsonComposition::new)
                        .toList());
    }
}
