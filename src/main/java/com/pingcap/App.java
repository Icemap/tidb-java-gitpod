package com.pingcap;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;

import java.sql.*;
import java.util.Properties;
import java.util.function.Supplier;

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
        Connection preInsertConn = DriverManager.getConnection(dbUrl, props);
        preInsertConn.createStatement().execute("DROP TABLE IF EXISTS testReversalOfScanFlags");
        preInsertConn.createStatement().execute("CREATE TABLE testReversalOfScanFlags (f1 INT, f2 VARCHAR(255))");
        preInsertConn.createStatement().execute("INSERT INTO testReversalOfScanFlags VALUES (1,\"a\"),(2,\"b\"),(3, \"c\")");

        props.setProperty(PropertyKey.queryInterceptors.getKeyName(), IndexQueryInterceptor.class.getName());
        Connection scanningConn = DriverManager.getConnection(dbUrl, props);
        scanningConn.createStatement().executeQuery("SELECT f2 FROM testReversalOfScanFlags");
    }

    public static class IndexQueryInterceptor implements QueryInterceptor {

        public QueryInterceptor init(MysqlConnection conn, Properties props, Log log) {
            return this;
        }

        public <T extends Resultset> T preProcess(Supplier<String> sql, Query interceptedQuery) {
            return null;
        }

        public boolean executeTopLevelOnly() {
            return false;
        }

        public void destroy() {
        }

        public <T extends Resultset> T postProcess(Supplier<String> sql, Query interceptedQuery, T originalResultSet, ServerSession serverSession) {
            System.out.println(sql);

            if (serverSession.noIndexUsed()) {
                System.out.println("This query no index to used");
            } else if (serverSession.noGoodIndexUsed()) {
                System.out.println("This query has good index to used");
            } else {
                System.out.println("This query has bad index to used");
            }

            return null;
        }
    }
}
