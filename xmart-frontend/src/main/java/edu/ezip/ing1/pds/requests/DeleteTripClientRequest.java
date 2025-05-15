package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class DeleteTripClientRequest extends ClientRequest<Trip, String> {

    public DeleteTripClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Trip info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        return body;
    }
} 