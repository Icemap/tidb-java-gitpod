package com.pingcap;

import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertyKey;

import java.sql.*;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{
    private final static String sha2User = "sha2user";
    private final static String sha2Password = "sha2password";
    private final static String createUserDBURL = "jdbc:mysql://localhost:4001/test?user=root&password=&useSSL=false&useServerPrepStmts=true&allowPublicKeyRetrieval=true";
    private final static String loginDBURL = "jdbc:mysql://localhost:4001";
    private final static String authPlugin = "caching_sha2_password";

    public static void main( String[] args ) throws SQLException {
        System.out.println("1. create user with caching_sha2_password by no password");
        testCachingSHA2Password(sha2User, "");
        System.out.println("2. create user with caching_sha2_password by password exists");
        testCachingSHA2Password(sha2User, sha2Password);
    }

    private static void testCachingSHA2Password(String createUser, String createPassword) throws SQLException {
        // Root user connection props
        Properties props = new Properties();
        props.setProperty(PropertyKey.sslMode.getKeyName(), PropertyDefinitions.SslMode.DISABLED.name());
        props.setProperty(PropertyKey.allowPublicKeyRetrieval.getKeyName(), "true");
        Connection createUserConn = DriverManager.getConnection(createUserDBURL, props);

        // Using `createUserConn` to create `sha2User` by no password
        String createUserSQL = "CREATE USER IF NOT EXISTS '" + createUser + "' IDENTIFIED WITH " + authPlugin;
        if (!createPassword.isEmpty()) {
            createUserSQL += " BY '" + createPassword + "'";
        }
        createUserConn.createStatement().execute(createUserSQL);
        createUserConn.createStatement().execute("GRANT ALL ON *.* TO '" + createUser + "'");
        createUserConn.createStatement().execute("FLUSH PRIVILEGES");

        // Login with caching_sha2_password no password
        Properties sha2props = new Properties();
        sha2props.setProperty(PropertyKey.sslMode.getKeyName(), PropertyDefinitions.SslMode.DISABLED.name());
        sha2props.setProperty(PropertyKey.allowPublicKeyRetrieval.getKeyName(), "true");
        sha2props.setProperty(PropertyKey.defaultAuthenticationPlugin.getKeyName(), authPlugin);
        sha2props.setProperty(PropertyKey.USER.getKeyName(), createUser);
        sha2props.setProperty(PropertyKey.PASSWORD.getKeyName(), createPassword);

        try (Connection sha2passwordConn = DriverManager.getConnection(loginDBURL, sha2props)){
            ResultSet testRs = sha2passwordConn.createStatement().executeQuery("SELECT CURRENT_USER()");
            while (testRs.next()) {
                System.out.println(testRs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            createUserConn.createStatement().execute("DROP USER IF EXISTS " + sha2User);
        }
    }
}

