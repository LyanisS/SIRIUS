package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class UpdateScheduleClientRequest extends ClientRequest<Schedule, String> {

    public UpdateScheduleClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Schedule info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        return body;
    }
}
