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

    public void insertTrain(Train train) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(insertRequestOrder);
        
    
        String jsonifiedTrain = objectMapper.writeValueAsString(train);
        request.setRequestContent(jsonifiedTrain);
        

        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        logger.trace("Request bytes: {}", new String(requestBytes));

        final InsertTrainClientRequest clientRequest = new InsertTrainClientRequest(
                networkConfig, 0, request, train, requestBytes);
        
        clientRequest.join();

        if (clientRequest.getException() != null) {
            throw new IOException("Erro to insert train: " + clientRequest.getException().getMessage(), 
                                clientRequest.getException());
        }

        Train result = clientRequest.getResult();
        if (result != null) {
            train.setId(result.getId());
            logger.debug("Thread {} complete : {} --> {}",
                    clientRequest.getThreadName(),
                    train.getId(),
                    result);
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
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);
        final SelectAllTrainsClientRequest clientRequest = new SelectAllTrainsClientRequest(
                networkConfig,
                birthdate++, request, null, requestBytes);
        clientRequests.push(clientRequest);

        if (!clientRequests.isEmpty()) {
            final ClientRequest joinedClientRequest = clientRequests.pop();
            joinedClientRequest.join();
            logger.debug("Thread {} complete", joinedClientRequest.getThreadName());
            return (Trains) joinedClientRequest.getResult();
        } else {
            logger.error("No trains found");
            return null;
        }
    }

    public boolean isTrainInUse(int trainId) throws InterruptedException, IOException {
        Trains trains = selectTrains();
        
        if (trains != null && trains.getTrains() != null) {
            for (Train train : trains.getTrains()) {
                if (train.getId() == trainId) {
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
            throw new IOException("Error delete train: " + clientRequest.getException().getMessage(), 
                                clientRequest.getException());
        }
        
        String result = (String) clientRequest.getResult();
        logger.debug("Delete train: {}", result);
    }
    
    public void updateTrainStatus(Train train) throws InterruptedException, IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder("UPDATE_TRAIN_STATUS");
        
        String jsonContent = objectMapper.writeValueAsString(train);
        request.setRequestContent(jsonContent);
        
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        
        final UpdateTrainClientRequest clientRequest = new UpdateTrainClientRequest(
                networkConfig, 0, request, train, requestBytes);
        
        clientRequest.join();
        
        if (clientRequest.getException() != null) {
            throw new IOException("Erreur to update train status: " + clientRequest.getException().getMessage(), 
                                clientRequest.getException());
        }
        
        String result = (String) clientRequest.getResult();
        logger.debug("Update train status: {}", result);
    }
}
