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

    private final static String INSERT_REQUEST_ORDER = "INSERT_SCHEDULE";
    private final static String SELECT_REQUEST_ORDER = "SELECT_ALL_SCHEDULES";
    private final static String DELETE_REQUEST_ORDER = "DELETE_SCHEDULE";
    private final static String UPDATE_REQUEST_ORDER = "UPDATE_SCHEDULE";

    private final NetworkConfig networkConfig;
    private final ObjectMapper objectMapper;

    public ScheduleService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    public NetworkConfig getNetworkConfig() {
        return this.networkConfig;
    }

    public void insertSchedules(Schedules schedules) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();

        int scheduleId = 0;
        for (final Schedule schedule : schedules.getSchedules()) {
            final String jsonifiedSchedule = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schedule);
            final String requestId = UUID.randomUUID().toString();
            final Request request = new Request();
            request.setRequestId(requestId);
            request.setRequestOrder(INSERT_REQUEST_ORDER);
            request.setRequestContent(jsonifiedSchedule);
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
            } else if (logger.isDebugEnabled()) {
                logger.debug("Thread {} complete : {} --> {}",
                        clientRequest.getThreadName(),
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
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(SELECT_REQUEST_ORDER);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        
        if (logger.isTraceEnabled()) {
            LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        }
        
        final SelectAllSchedulesClientRequest clientRequest = new SelectAllSchedulesClientRequest(
                networkConfig,
                birthdate++, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            if (logger.isDebugEnabled()) {
                logger.debug("Thread {} complete.", joinedClientRequest.getThreadName());
            }
            return (Schedules) joinedClientRequest.getResult();
        } else {
            logger.error("No schedules found");
            return null;
        }
    }

    /**
     * Sends a schedule-related request to the server
     * 
     * @param scheduleId The ID of the schedule
     * @param requestOrder The request order (DELETE_SCHEDULE or UPDATE_SCHEDULE)
     * @param requestClass The client request class to instantiate
     * @return The result of the operation as String
     * @throws InterruptedException If the thread is interrupted
     * @throws IOException If an I/O error occurs
     */
    private String sendScheduleRequest(int scheduleId, String requestOrder, 
            Class<? extends ClientRequest> requestClass) throws InterruptedException, IOException {
        
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(requestOrder);

        String jsonContent = "{\"id\":" + scheduleId + "}";
        request.setRequestContent(jsonContent);

        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        
        ClientRequest clientRequest;
        Schedule dummySchedule = new Schedule();
        dummySchedule.setId(scheduleId);
        
        if (requestClass.equals(DeleteScheduleClientRequest.class)) {
            clientRequest = new DeleteScheduleClientRequest(
                    networkConfig, 0, request, scheduleId, requestBytes);
        } else if (requestClass.equals(UpdateScheduleClientRequest.class)) {
            clientRequest = new UpdateScheduleClientRequest(
                    networkConfig, 0, request, dummySchedule, requestBytes);
        } else {
            throw new IllegalArgumentException("Unsupported request class: " + requestClass.getName());
        }

        clientRequest.join();

        if (clientRequest.getException() != null) {
            String operation = requestClass.equals(DeleteScheduleClientRequest.class) ? "deleting" : "updating";
            throw new IOException("Error " + operation + " schedule: " + clientRequest.getException().getMessage(),
                    clientRequest.getException());
        }

        return (String) clientRequest.getResult();
    }

    public void deleteSchedule(int scheduleId) throws InterruptedException, IOException {
        sendScheduleRequest(scheduleId, DELETE_REQUEST_ORDER, DeleteScheduleClientRequest.class);
    }

    public void updateSchedule(int scheduleId) throws InterruptedException, IOException {
        sendScheduleRequest(scheduleId, UPDATE_REQUEST_ORDER, UpdateScheduleClientRequest.class);
    }
}
