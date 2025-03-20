package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.InsertScheduleClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllSchedulesClientRequest;

public class ScheduleService {

    private final static String LoggingLabel = "FrontEnd - ScheduleService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_SCHEDULE";
    final String selectRequestOrder = "SELECT_ALL_SCHEDULES";

    private final NetworkConfig networkConfig;

    public ScheduleService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertSchedules(Schedules schedules) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();

        int birthdate = 0;
        for (final Schedule schedule : schedules.getSchedules()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final String jsonifiedSchedule = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schedule);
            logger.trace("Schedule with its JSON face: {}", jsonifiedSchedule);
            final String requestId = UUID.randomUUID().toString();
            final Request request = new Request();
            request.setRequestId(requestId);
            request.setRequestOrder(insertRequestOrder);
            request.setRequestContent(jsonifiedSchedule);
            objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

            final InsertScheduleClientRequest clientRequest = new InsertScheduleClientRequest(networkConfig, birthdate++, request, schedule, requestBytes);
            clientRequests.push(clientRequest);
        }

        while (!clientRequests.isEmpty()) {
            final ClientRequest clientRequest = clientRequests.pop();
            clientRequest.join();
            final Schedule schedule = (Schedule) clientRequest.getInfo();
            logger.debug("Thread {} complete: {} --> {}",
                    clientRequest.getThreadName(),
                    schedule.getId(),
                    clientRequest.getResult());
        }
    }

    public Schedules selectSchedules() throws InterruptedException, IOException {
        int birthdate = 0;
        final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        final SelectAllSchedulesClientRequest clientRequest = new SelectAllSchedulesClientRequest(
                networkConfig,
                birthdate++, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} complete.", joinedClientRequest.getThreadName());
            return (Schedules) joinedClientRequest.getResult();
        } else {
            logger.error("No schedules found");
            return null;
        }
    }
}
