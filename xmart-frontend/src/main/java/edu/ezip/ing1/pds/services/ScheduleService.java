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
import edu.ezip.ing1.pds.requests.DeleteScheduleClientRequest;
import edu.ezip.ing1.pds.requests.InsertScheduleClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllSchedulesClientRequest;
import edu.ezip.ing1.pds.requests.UpdateScheduleClientRequest;

public class ScheduleService {

    private final static String LoggingLabel = "FrontEnd - ScheduleService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_SCHEDULE";
    final String selectRequestOrder = "SELECT_ALL_SCHEDULES";
    final String deleteRequestOrder = "DELETE_SCHEDULE";
    final String updateRequestOrder = "UPDATE_SCHEDULE";

    private final NetworkConfig networkConfig;

    public ScheduleService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertSchedules(Schedules schedules) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();

        int scheduleId = 0;
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

            final InsertScheduleClientRequest clientRequest = new InsertScheduleClientRequest(
                    networkConfig, scheduleId++, request, schedule, requestBytes);
            clientRequests.push(clientRequest);
        }

        Exception lastException = null;
        while (!clientRequests.isEmpty()) {
            final ClientRequest clientRequest = clientRequests.pop();
            clientRequest.join();
            final Schedule schedule = (Schedule) clientRequest.getInfo();
            if (clientRequest.getException() != null) {
                lastException = clientRequest.getException();
                logger.error("Error in thread {}: {}",
                        clientRequest.getThreadName(),
                        lastException.getMessage());
            } else {
                logger.debug("Thread {} complete : {} {} --> {}",
                        clientRequest.getThreadName(),
                        schedule.getTrackElement(),
                        schedule.getTrip(),
                        clientRequest.getResult());
            }
        }

        if (lastException != null) {
            if (lastException instanceof IOException) {
                throw (IOException) lastException;
            } else {
                throw new IOException("Error inserting schedule: " + lastException.getMessage(), lastException);
            }
        }
    }

    public Schedules selectSchedules() throws InterruptedException, IOException {
        int birthdate = 0;
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
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

    public boolean isTrackElementInUse(int trackElementId) throws InterruptedException, IOException {
        Schedules schedules = selectSchedules();

        if (schedules != null && schedules.getSchedules() != null) {
            for (Schedule schedule : schedules.getSchedules()) {
                if (schedule.getTrackElement() != null
                        && schedule.getTrackElement().getId() == trackElementId) {
                    return true;
                }
            }
        }

        return false;
    }

    public void deleteSchedule(int scheduleId) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(deleteRequestOrder);

        String jsonContent = "{\"id\":" + scheduleId + "}";
        request.setRequestContent(jsonContent);

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final DeleteScheduleClientRequest clientRequest = new DeleteScheduleClientRequest(
                networkConfig, 0, request, scheduleId, requestBytes);

        logger.debug("Sending delete request for scheduleId: {}", scheduleId);
        logger.debug("Request content: {}", jsonContent);
        clientRequest.join();

        if (clientRequest.getException() != null) {
            logger.error("Error deleting schedule: {}", clientRequest.getException().getMessage());
            throw new IOException("Error deleting schedule: " + clientRequest.getException().getMessage(),
                    clientRequest.getException());
        }

        String result = (String) clientRequest.getResult();
        logger.debug("Delete schedule result: {}", result);
    }

    public void UpdateSchedule(int scheduleId, boolean stop) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(updateRequestOrder);

        String jsonContent = "{\"id\":" + scheduleId + ", \"stop\":" + stop + "}";
        request.setRequestContent(jsonContent);

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        Schedule dummySchedule = new Schedule();
        dummySchedule.setId(scheduleId);

        final UpdateScheduleClientRequest clientRequest = new UpdateScheduleClientRequest(
                networkConfig, 0, request, dummySchedule, requestBytes);

        clientRequest.join();

        if (clientRequest.getException() != null) {
            throw new IOException("Error updating schedule status: " + clientRequest.getException().getMessage(),
                    clientRequest.getException());
        }

        String result = (String) clientRequest.getResult();
        logger.debug("Update schedule status result: {}", result);
    }
}
