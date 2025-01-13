package edu.ezip.ing1.pds;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.a7.A7_Grids;
import edu.ezip.ing1.pds.business.dto.Student;
import edu.ezip.ing1.pds.business.dto.Students;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainFrontEnd {

    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";
    private static final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();

    public static void main(String[] args) throws IOException, InterruptedException {
        final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        logger.debug("Load Network config file : {}", networkConfig.toString());

        final StudentService studentService = new StudentService(networkConfig);
        studentService.insertStudents();
        Students students = studentService.selectStudents();
        final AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow("id", "firstname", "name", "group");
        for (final Student student : students.getStudents()) {
            asciiTable.addRule();
            asciiTable.addRow(student.getId(), student.getFirstname(), student.getName(), student.getGroup());
        }
        asciiTable.addRule();
        asciiTable.getContext().setGrid(A7_Grids.minusBarPlusEquals());
        logger.debug("\n{}\n", asciiTable.render());
    }
}
