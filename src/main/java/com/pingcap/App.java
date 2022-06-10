package com.pingcap;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:4000/test?user=root&password=&useSSL=false&useServerPrepStmts=true&allowPublicKeyRetrieval=true");

        try (Connection connection = mysqlDataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS testBug71672");
            connection.createStatement().execute("CREATE TABLE testBug71672 (id INT AUTO_INCREMENT PRIMARY KEY, ch CHAR(1) UNIQUE KEY, ct INT)");
            printGeneratedKeys(connection, "INSERT INTO testBug71672 (ch, ct) VALUES ('A', 100), ('C', 100), ('D', 100)");
            printGeneratedKeys(connection, "INSERT INTO testBug71672 (ch, ct) VALUES ('B', 2), ('C', 3), ('D', 4), ('E', 5) ON DUPLICATE KEY UPDATE ct = -1 * (ABS(ct) + VALUES(ct))");
            printGeneratedKeys(connection, "INSERT INTO testBug71672 (ch, ct) VALUES ('F', 100) ON DUPLICATE KEY UPDATE ct = -1 * (ABS(ct) + VALUES(ct))");
            printGeneratedKeys(connection, "INSERT INTO testBug71672 (ch, ct) VALUES ('B', 2), ('F', 6) ON DUPLICATE KEY UPDATE ct = -1 * (ABS(ct) + VALUES(ct))");
        }
    }

    public static void printGeneratedKeys(Connection connection, String sql) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        List<Integer> resultPKList = new LinkedList<>();
        while (rs.next()) {
            resultPKList.add(rs.getInt(1));
        }

        System.out.println(resultPKList);
    }
}
