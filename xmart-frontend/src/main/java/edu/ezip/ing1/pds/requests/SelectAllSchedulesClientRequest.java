package edu.ezip.ing1.pds.requests;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class SelectAllSchedulesClientRequest extends ClientRequest<Object, Schedules> {

    public SelectAllSchedulesClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Object info, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public Schedules readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Schedule[] scheduleArray = mapper.readValue(body, Schedule[].class);

        Schedules schedules = new Schedules();
        for (Schedule schedule : scheduleArray) {
            schedules.add(schedule);
        }
        return schedules;
    }
}
