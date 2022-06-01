package com.pingcap;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws SQLException {
        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost:4000/test?user=root");
        try (Connection connection = mysqlDataSource.getConnection()) {
            ResultSet rs = connection.createStatement().executeQuery("SELECT VERSION()");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
