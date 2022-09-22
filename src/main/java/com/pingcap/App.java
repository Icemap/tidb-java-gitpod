package com.pingcap;

import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        String dbUrl = "jdbc:mysql://localhost:4000/test?user=root&password=&useServerPrepStmts=true";

        Connection preInsertConn = DriverManager.getConnection(dbUrl);
        preInsertConn.createStatement().execute("DROP TABLE IF EXISTS testBug89948");
        preInsertConn.createStatement().execute("CREATE TABLE testBug89948 (id INT PRIMARY KEY)");

        boolean allowMQ = false;
        boolean rwBatchStmts = true;
        boolean useLTS = true;

        final Properties props = new Properties();
        props.setProperty(PropertyKey.sslMode.getKeyName(), PropertyDefinitions.SslMode.DISABLED.name());
        props.setProperty(PropertyKey.allowPublicKeyRetrieval.getKeyName(), "true");

        props.setProperty(PropertyKey.allowMultiQueries.getKeyName(), Boolean.toString(allowMQ));
        props.setProperty(PropertyKey.rewriteBatchedStatements.getKeyName(), Boolean.toString(rwBatchStmts));
        props.setProperty(PropertyKey.useLocalTransactionState.getKeyName(), Boolean.toString(useLTS));
        props.setProperty(PropertyKey.useLocalSessionState.getKeyName(), Boolean.toString(useLTS));

        Connection testConn = DriverManager.getConnection(dbUrl, props);
        testConn.setAutoCommit(false);

        PreparedStatement pstmt = testConn.prepareStatement("INSERT INTO testBug89948 VALUES (?)");
        for (int i = 1; i <= 10; i++) {
            pstmt.setInt(1, i);
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        testConn.commit();

        pstmt.close();
        testConn.close();
    }
}
