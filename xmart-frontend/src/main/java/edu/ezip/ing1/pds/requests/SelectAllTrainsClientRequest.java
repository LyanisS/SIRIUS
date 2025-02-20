package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class SelectAllTrainsClientRequest extends ClientRequest<Object, Trains> {

    public SelectAllTrainsClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Trains readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Parse the JSON array directly from response body
        Train[] trainArray = mapper.readValue(body, Train[].class);
        
        // Create and populate Trains object
        Trains trains = new Trains();
        for (Train train : trainArray) {
            trains.add(train);
        }
        return trains;
    }

}
