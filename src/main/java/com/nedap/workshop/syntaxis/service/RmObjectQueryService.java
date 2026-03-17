package com.nedap.workshop.syntaxis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nedap.archie.query.RMObjectWithPath;
import com.nedap.archie.query.RMPathQuery;
import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import com.nedap.archie.rmobjectvalidator.APathQueryCache;
import com.nedap.workshop.syntaxis.api.json.JsonCompositionValuesForPath;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class RmObjectQueryService {
    public List<JsonCompositionValuesForPath> queryPaths(List<String> requestedPaths, Composition composition) throws JsonProcessingException {
        APathQueryCache queryCache = new APathQueryCache(false);
        List<JsonCompositionValuesForPath> valuesForPaths = new ArrayList<>();

        // Remove duplicate paths by converting to a Set. By using a LinkedHashSet the order of paths is maintained.
        for (String path : new LinkedHashSet<>(requestedPaths)) {
            RMPathQuery query = queryCache.getApathQuery(path);
            List<RMObjectWithPath> rmObjectsWithPath = query.findList(ArchieRMInfoLookup.getInstance(), composition);

            // Adding all the objects found at a specific path in the RMObject to valuesForPath
            JsonCompositionValuesForPath valuesForSinglePath = new JsonCompositionValuesForPath(path);
            for (RMObjectWithPath objectWithPath : rmObjectsWithPath) {
                valuesForSinglePath.addValue(objectWithPath.getObject());
            }

            valuesForPaths.add(valuesForSinglePath);
        }

        return valuesForPaths;
    }
}
