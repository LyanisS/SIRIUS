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
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.requests.DeleteTrainClientRequest;
import edu.ezip.ing1.pds.requests.InsertTrainClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllTrainsClientRequest;
import edu.ezip.ing1.pds.requests.UpdateTrainClientRequest;

public class TrainService {

    private final static String LoggingLabel = "FrontEnd - TrainService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);

    final String insertRequestOrder = "INSERT_TRAIN";
    final String selectRequestOrder = "SELECT_ALL_TRAINS";

    private final NetworkConfig networkConfig;

    public TrainService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertTrains(Trains trains) throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();

        int trainId = 0;
        for (final Train train : trains.getTrains()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final String jsonifiedTrain = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(train);
            logger.trace("Train with its JSON face : {}", jsonifiedTrain);
            final String requestId = UUID.randomUUID().toString();
            final Request request = new Request();
            request.setRequestId(requestId);
            request.setRequestOrder(insertRequestOrder);
            request.setRequestContent(jsonifiedTrain);
            objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);

            final InsertTrainClientRequest clientRequest = new InsertTrainClientRequest(
                    networkConfig, trainId++, request, train, requestBytes);
            clientRequests.push(clientRequest);
        }

        Exception lastException = null;
        while (!clientRequests.isEmpty()) {
            final ClientRequest clientRequest = clientRequests.pop();
            clientRequest.join();
            final Train train = (Train) clientRequest.getInfo();
                    if (clientRequest.getException() != null) {
                lastException = clientRequest.getException();
                logger.error("Error in thread {}: {}", 
                    clientRequest.getThreadName(), 
                    lastException.getMessage());
            } else {
                logger.debug("Thread {} complete : {} {} {} --> {}",
                        clientRequest.getThreadName(),
                        train.getId(), train.getStatus(), train.getTrackElement(),
                        clientRequest.getResult());
            }
        }
        
    
        if (lastException != null) {
            if (lastException instanceof IOException) {
                throw (IOException) lastException;
            } else {
                throw new IOException("Error inserting train: " + lastException.getMessage(), lastException);
            }
        }
    }

    public Trains selectTrains() throws InterruptedException, IOException {
        int birthdate = 0;
        final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte []  requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        final SelectAllTrainsClientRequest clientRequest = new SelectAllTrainsClientRequest(
                networkConfig,
                birthdate++, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} complete.", joinedClientRequest.getThreadName());
            return (Trains) joinedClientRequest.getResult();
        } else {
            logger.error("No trains found");
            return null;
        }
    }

    public boolean isTrackElementInUse(int trackElementId) throws InterruptedException, IOException {
        Trains trains = selectTrains();
        
        if (trains != null && trains.getTrains() != null) {
            for (Train train : trains.getTrains()) {
                if (train.getTrackElement() != null && 
                    train.getTrackElement().getId() == trackElementId) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void deleteTrain(int trainId) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("DELETE_TRAIN");
        
    
        String jsonContent = "{\"id\":" + trainId + "}";
        request.setRequestContent(jsonContent);
        
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        
        
        final DeleteTrainClientRequest clientRequest = new DeleteTrainClientRequest(
                networkConfig, 0, request, trainId, requestBytes);
        
        clientRequest.join();
        
        if (clientRequest.getException() != null) {
            throw new IOException("Error deleting train: " + clientRequest.getException().getMessage(), 
                                clientRequest.getException());
        }
        
    
        String result = (String) clientRequest.getResult();
        logger.debug("Delete train result: {}", result);
    }
    
    public void updateTrainStatus(int trainId, int statusId) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("UPDATE_TRAIN_STATUS");
        
    
        String jsonContent = "{\"id\":" + trainId + ", \"statusId\":" + statusId + "}";
        request.setRequestContent(jsonContent);
        
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        
    
        Train dummyTrain = new Train();
        dummyTrain.setId(trainId);
        
        
        final UpdateTrainClientRequest clientRequest = new UpdateTrainClientRequest(
                networkConfig, 0, request, dummyTrain, requestBytes);
        
        clientRequest.join();
        
    
        if (clientRequest.getException() != null) {
            throw new IOException("Error updating train status: " + clientRequest.getException().getMessage(), 
                                clientRequest.getException());
        }
        
        
        String result = (String) clientRequest.getResult();
        logger.debug("Update train status result: {}", result);
    }
    
}
