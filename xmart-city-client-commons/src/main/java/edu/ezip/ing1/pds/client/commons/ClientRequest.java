package edu.ezip.ing1.pds.client.commons;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.commons.LoggingUtils;
import edu.ezip.ing1.pds.commons.Request;
import edu.ezip.ing1.pds.commons.Response;

public abstract class ClientRequest<N, S> implements Runnable {

    private final Socket socket = new Socket();
    private final Thread self;
    private final NetworkConfig networkConfig;
    private static final String threadNamePrfx = "client_request";
    private InputStream instream;
    private OutputStream outstream;
    private final byte[] bytes;
    private static final int maxTimeLapToGetAClientReplyInMs = 5000;
    private static final int timeStepMs = 300;
    private final String threadName;
    private final static String LoggingLabel = "C l i e n t - R e q u e s t";
    private final Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final BlockingDeque<Integer> waitArtifact = new LinkedBlockingDeque<Integer>(1);
    private final Request request;
    private final N info;
    private S result;
    private Exception exception;

    public ClientRequest(final NetworkConfig networkConfig,
            final int myBirthDate,
            final Request request,
            final N info,
            final byte[] bytes) throws IOException {
        this.networkConfig = networkConfig;
        final StringBuffer threadNameBuffer = new StringBuffer();
        threadNameBuffer.append(threadNamePrfx).append("★").append(String.format("%04d", myBirthDate));
        threadName = threadNameBuffer.toString();
        this.bytes = bytes;
        this.request = request;
        this.info = info;
        self = new Thread(this, threadNameBuffer.toString());
        self.start();
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(networkConfig.getIpaddress(), networkConfig.getTcpport()));
            instream = socket.getInputStream();
            outstream = socket.getOutputStream();

            LoggingUtils.logDataMultiLine(logger, Level.DEBUG, bytes);
            outstream.write(bytes);

            int timeout = maxTimeLapToGetAClientReplyInMs;
            while (0 == instream.available() && 0 < timeout) {
                waitArtifact.pollFirst(timeStepMs, TimeUnit.MILLISECONDS);
                timeout -= timeStepMs;
            }
            if (0 > timeout) {
                return;
            }

            final byte[] inputData = new byte[instream.available()];
            logger.trace("Bytes read = {}", inputData.length);
            instream.read(inputData);
            LoggingUtils.logDataMultiLine(logger, Level.TRACE, inputData);

            final ObjectMapper mapper = new ObjectMapper();
            final Response response = mapper.readValue(inputData, Response.class);
            logger.debug("Response = {}", response.toString());

            result = readResult(response.responseBody);

        } catch (IOException e) {
            logger.error("Connection fails, exception tells {} — {}", e.getMessage(), e.getClass());
            this.exception = e;
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.exception = e;
        }
    }

    public abstract S readResult(final String body) throws IOException;

    public void join() throws InterruptedException {
        self.join();
    }

    public final String getThreadName() {
        return threadName;
    }

    public final N getInfo() {
        return info;
    }

    public final S getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

    public void deleteSchedule(final int scheduleId) {
        logger.info("Deleting schedule with ID: {}", scheduleId);

    }

    public void updateSchedule(final int scheduleId, final S updatedSchedule) {
        logger.info("Updating schedule with ID: {}", scheduleId);

    }
}
