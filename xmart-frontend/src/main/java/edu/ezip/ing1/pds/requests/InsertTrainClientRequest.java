package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class InsertTrainClientRequest extends ClientRequest<Train, Train> {

    public InsertTrainClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Train info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Train readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(body, Train.class);
    }
} 