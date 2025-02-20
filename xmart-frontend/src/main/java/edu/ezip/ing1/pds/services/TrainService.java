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
import edu.ezip.ing1.pds.requests.InsertTrainClientRequest;
import edu.ezip.ing1.pds.requests.SelectAllTrainsClientRequest;

public class TrainService {

    private final static String LoggingLabel = "FrontEnd - TrainService";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String trainsToBeInserted = "trains-to-be-inserted.yaml";

    final String insertRequestOrder = "INSERT_TRAIN";
    final String selectRequestOrder = "SELECT_ALL_TRAINS";

    private final NetworkConfig networkConfig;

    public TrainService(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void insertTrains() throws InterruptedException, IOException {
        final Deque<ClientRequest> clientRequests = new ArrayDeque<>();
        final Trains trains = ConfigLoader.loadConfig(Trains.class, trainsToBeInserted);

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

        while (!clientRequests.isEmpty()) {
            final ClientRequest clientRequest = clientRequests.pop();
            clientRequest.join();
            final Train train = (Train) clientRequest.getInfo();
            logger.debug("Thread {} complete : {} {} --> {}",
                    clientRequest.getThreadName(),
                    train.getTrainId(), train.getTrainStatusId(), train.getTrackElementId(),
                    clientRequest.getResult());
        }
    }

    public Trains selectTrains() throws InterruptedException, IOException {
        final String requestId = UUID.randomUUID().toString();
        final Request request = new Request();
        request.setRequestId(requestId);
        request.setRequestOrder(selectRequestOrder);

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        final byte[] requestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(request);
        LoggingUtils.logDataMultiLine(logger, Level.TRACE, requestBytes);

        final SelectAllTrainsClientRequest clientRequest = new SelectAllTrainsClientRequest(
                networkConfig,
                0, // Since trainId isn't used in the select operation
                request,
                null,
                requestBytes);

        //clientRequest.start(); // Make sure the request is started
        clientRequest.join();  // Wait for completion

        if (clientRequest.getResult() != null) {
            return (Trains) clientRequest.getResult();
        }

        logger.error("No trains found");
        return null;
    }
}
