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
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.business.dto.Trips;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.InsertTripClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllTripsClientRequest;

public class TripService {

    private final static String LoggingLabel = "FrontEnd - TripService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_TRIP";
    final String selectRequestOrder = "SELECT_ALL_TRIPS";

    private final NetworkConfig networkConfig;

    public TripService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertTrip(Trip trip) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        
        final ObjectMapper objectMapper = new ObjectMapper();
        final String jsonifiedTrip = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(trip);
        logger.trace("Trip with its JSON face: {}", jsonifiedTrip);
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(insertRequestOrder);
        request.setRequestContent(jsonifiedTrip);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

        final InsertTripClientRequest clientRequest = new InsertTripClientRequest(
                networkConfig, 0, request, trip, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            
            if (joinedClientRequest.getException() != null) {
                Exception exception = joinedClientRequest.getException();
                logger.error("Error inserting trip: {}", exception.getMessage());
                throw new IOException("Error inserting trip: " + exception.getMessage(), exception);
            }
            
            logger.debug("Thread {} complete. Trip insertion successful.", joinedClientRequest.getThreadName());
        }
    }

    public Trips selectTrips() throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        final SelectAllTripsClientRequest clientRequest = new SelectAllTripsClientRequest(
                networkConfig, 0, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} complete.", joinedClientRequest.getThreadName());
            return (Trips) joinedClientRequest.getResult();
        } else {
            logger.error("No trips found");
            return null;
        }
    }
} 