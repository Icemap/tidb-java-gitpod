package com.pingcap;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.protocol.ServerSessionStateController;

import java.sql.*;
import java.util.Properties;

import static com.mysql.cj.protocol.ServerSessionStateController.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        String dbUrl = "jdbc:mysql://localhost:4001/test?user=root&password=&useSSL=false&useServerPrepStmts=true&allowPublicKeyRetrieval=true";
        Properties props = new Properties();
        props.setProperty(PropertyKey.sslMode.getKeyName(), PropertyDefinitions.SslMode.DISABLED.name());
        props.setProperty(PropertyKey.allowPublicKeyRetrieval.getKeyName(), "true");
        props.setProperty(PropertyKey.trackSessionState.getKeyName(), "true");
        props.setProperty(PropertyKey.characterEncoding.getKeyName(), "latin1");
        Connection stateChangeConn = DriverManager.getConnection(dbUrl, props);

        ((MysqlConnection) stateChangeConn).getServerSessionStateController()
                .addSessionStateChangesListener(stateChange -> {
                    System.out.println("State change effect");
                    for (ServerSessionStateController.SessionStateChange change : stateChange.getSessionStateChangesList()) {
                        switch (change.getType()) {
                            case SESSION_TRACK_SYSTEM_VARIABLES:
                                System.out.println("State change type is SESSION_TRACK_SYSTEM_VARIABLES");
                                break;
                            case SESSION_TRACK_SCHEMA:
                                System.out.println("State change type is SESSION_TRACK_SCHEMA");
                                break;
                            case SESSION_TRACK_STATE_CHANGE:
                                System.out.println("State change type is SESSION_TRACK_STATE_CHANGE");
                                break;
                        }
                    }
                });

        System.out.println("\n=== Track Session State ===");
        stateChangeConn.createStatement().executeUpdate("SET NAMES utf8mb4");
    }
}
