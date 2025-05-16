package edu.ezip.ing1.pds.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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

        while (!clientRequests.isEmpty()) {
            final ClientRequest clientRequest = clientRequests.pop();
            clientRequest.join();
            final Schedule schedule = (Schedule) clientRequest.getInfo();
            if (clientRequest.getException() != null) {
                Exception exception = clientRequest.getException();
                logger.error("Error in thread {}: {}",
                        clientRequest.getThreadName(),
                        exception.getMessage());
                throw new IOException("Error inserting schedule: " + exception.getMessage(), exception);
            } else {
                logger.debug("Thread {} complete : {} --> {}",
                        clientRequest.getThreadName(),
                        schedule.getTrip(),
                        clientRequest.getResult());
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

    
    public void deleteSchedulesByTripId(int tripId) throws InterruptedException, IOException {
        logger.debug("Deleting all schedules for trip ID: {}", tripId);
        
        
        Schedules allSchedules = this.selectSchedules();
        if (allSchedules != null && allSchedules.getSchedules() != null) {
            List<Integer> schedulesToDelete = new ArrayList<>();
            
            
            for (Schedule schedule : allSchedules.getSchedules()) {
                if (schedule.getTrip() != null && schedule.getTrip().getId() == tripId) {
                    schedulesToDelete.add(schedule.getId());
                }
            }
            
            logger.debug("Found {} schedules to delete for trip ID {}", schedulesToDelete.size(), tripId);
            
           
            for (Integer scheduleId : schedulesToDelete) {
                try {
                    deleteSchedule(scheduleId);
                } catch (Exception e) {
                    logger.error("Error deleting schedule ID {}: {}", scheduleId, e.getMessage());
                    
                }
            }
            
            logger.debug("Completed deletion of schedules for trip ID {}", tripId);
        } else {
            logger.debug("No schedules found to delete for trip ID {}", tripId);
        }
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
