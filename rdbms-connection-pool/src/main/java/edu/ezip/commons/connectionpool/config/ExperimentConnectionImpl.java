package edu.ezip.commons.connectionpool.config;

import edu.ezip.commons.connectionpool.config.impl.ConnectionPoolImpl;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

public class ExperimentConnectionImpl {
    public static void main(String[] args) throws SQLException, UnsupportedEncodingException {
        ConnectionPoolImpl.getInstance("mysql");
    }
}
