package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Student;
import service.MyLogger;

import java.sql.*;

public class DbConnectivityClass {
    final static String SQL_SERVER_URL = "jdbc:mysql://csc311courseservermaz.mariadb.database.azure.com";//update this server name
    private static String dbName = null;
    private static String DB_URL = "jdbc:mysql://csc311courseservermaz.mariadb.database.azure.com/" + dbName;//update this database name
    final String USERNAME = "csc311admin@csc311courseservermaz";
    final String PASSWORD = "DataBase@123";
    private final ObservableList<Student> data = FXCollections.observableArrayList();
    // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.

    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String db) {
        dbName = db;
        DB_URL = "jdbc:mysql://csc311courseservermaz.mariadb.database.azure.com/" + dbName;

    }

    public ObservableList<Student> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                MyLogger.makeLog("No data");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String level = resultSet.getString("year");
                data.add(new Student(id, first_name, last_name, major, email, level));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void connectToDatabase() {
        boolean hasRegisteredUsers = false;

        String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + dbName;
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT(10) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "first_name VARCHAR(200) NOT NULL," +
                "last_name VARCHAR(200) NOT NULL," +
                "major VARCHAR(200)," +
                "email VARCHAR(200) NOT NULL UNIQUE," +
                "year VARCHAR(200))";
        String countUsersSQL = "SELECT COUNT(*) FROM users";

        try {
            // Create database if not exists
            try (Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate(createDatabaseSQL);
            }

            // Create users table if not exists and check for registered users
            try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                 Statement statement = conn.createStatement()) {
                statement.executeUpdate(createTableSQL);

                ResultSet resultSet = statement.executeQuery(countUsersSQL);
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    hasRegisteredUsers = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String level = resultSet.getString("year");

                MyLogger.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Major: " + major + ", year: " + level);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void listAllUsers() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String level = resultSet.getString("year");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");

                MyLogger.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Level: " + level + ", Major: " + major + ", Email: " + email);
            }

            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(Student student) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "INSERT INTO users (first_name, last_name, major, email, year) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, student.getFirstName());
            preparedStatement.setString(2, student.getLastName());
            preparedStatement.setString(3, student.getMajor());
            preparedStatement.setString(4, student.getEmail());
            preparedStatement.setString(5, student.getYear());
            int row = preparedStatement.executeUpdate();
            if (row > 0) {
                MyLogger.makeLog("A new user was inserted successfully.");
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editUser(int id, Student p) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "UPDATE users SET first_name=?, last_name=?, major=?, email=?, year=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getMajor());
            preparedStatement.setString(4, p.getEmail());
            preparedStatement.setString(5, p.getYear());
            preparedStatement.setInt(6, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(Student student) {
        if (student == null) {
            return;
        }
        int id = student.getId();
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Method to retrieve id from database where it is auto-incremented.
    public int retrieveId(Student p) {
        connectToDatabase();
        int id;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            id = resultSet.getInt("id");
            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        MyLogger.makeLog(String.valueOf(id));
        return id;
    }
}