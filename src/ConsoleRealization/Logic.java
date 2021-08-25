package ConsoleRealization;

import java.sql.*;
import java.util.LinkedList;

public class Logic {
    public static final String USER_NAME = "Nilomy";
    public static final String PASSWORD = "13542";
    public static final String URL = "jdbc:mysql://localhost:3306/mysql";

    public static final String DB_NAME = "test_task";
    public static final String TB_DEPARTMENTS = "departments";
    public static final String TB_WORKERS = "workers";

    static Connection connection;
    static Statement statement;
    static ResultSet workersMetaData;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            statement = connection.createStatement();
            System.out.println("[S] eap! Connected!");
            setWorkersMetaData();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    static void setWorkersMetaData() throws SQLException {
        workersMetaData = connection.getMetaData().getColumns(DB_NAME, null, TB_WORKERS, "%");
    }

    static int getWorkersMetaData_i(String columnName, String parameter) throws SQLException {
        int out = -3333;
        while (workersMetaData.next()) {
            if (!columnName.equals(workersMetaData.getString("COLUMN_NAME"))) {
                continue;
            }
            out = workersMetaData.getInt(parameter);
            break;
        }
        workersMetaData.beforeFirst();
        return out;
    }

    static String getWorkersMetaData_s(String columnName, String parameter) throws SQLException {
        String out = "";
        while (workersMetaData.next()) {
            if (!columnName.equals(workersMetaData.getString("COLUMN_NAME"))) {
                continue;
            }
            out = workersMetaData.getString(parameter);
            break;
        }
        workersMetaData.beforeFirst();
        return out;
    }

    static LinkedList<String> getWorkersColumnNames() throws SQLException {
        LinkedList<String> columnNames = new LinkedList<>();
        while (workersMetaData.next()) {
            columnNames.add(workersMetaData.getString("COLUMN_NAME"));
        }
        workersMetaData.beforeFirst();
        return columnNames;
    }

    static int getColumnDataType(String tableName, String columnName) throws SQLException {
        ResultSet column = connection.getMetaData().
                getColumns(DB_NAME, null, tableName, columnName);
        if (column.next()) {
            return column.getInt("DATA_TYPE");
        }
        throw new IllegalArgumentException("Column " + columnName + " or table " + tableName + " not exists.");
    }

    static int getColumnSize(String tableName, String columnName) throws SQLException {
        ResultSet column = connection.getMetaData().
                getColumns(DB_NAME, null, tableName, columnName);
        if (column.next()) {
            return column.getInt("COLUMN_SIZE");
        }
        throw new IllegalArgumentException("Column " + columnName + " or table " + tableName + " not exists.");
    }

    static LinkedList<String> getChangeableColumnsNames(String tableName) throws SQLException {
        LinkedList<String> list = new LinkedList<>();
        ResultSet columns = connection.getMetaData().
                getColumns(DB_NAME, null, tableName, "%");
        while (columns.next()) {
            if (columns.getString("IS_AUTOINCREMENT").equals("YES")) {
                continue;
            }
            list.add(columns.getString("COLUMN_NAME"));
        }
        if (tableName.equals(TB_DEPARTMENTS)) {
            list.remove("num_workers");
        }
        return list;
    }


    static void createStartingData() {
        createStartingData(true);
    }

    static void createStartingData(boolean reCreate) {
        try {
            connection.setAutoCommit(false);
            System.out.println("[S] Preparing starting data...");
            if (reCreate)
                statement.addBatch("DROP DATABASE IF EXISTS " + DB_NAME);
            statement.addBatch("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            statement.executeBatch();
            connection.setCatalog(DB_NAME);
            statement = connection.createStatement();
            statement.addBatch("CREATE TABLE IF NOT EXISTS " + TB_DEPARTMENTS +
                    "(id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL UNIQUE, " +
                    "num_workers INT NOT NULL);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS " + TB_WORKERS +
                    "(id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "department VARCHAR(50) NOT NULL, " +
                    "speciality VARCHAR(50), " +
                    "mail VARCHAR(80) NOT NULL UNIQUE, " +
                    "assignment_date DATE, " +
                    "completed_tasks INT, " +
                    "CONSTRAINT fk_department_name " +
                    "FOREIGN KEY (department) " +
                    "REFERENCES " + TB_DEPARTMENTS + " (name) " +
                    "ON UPDATE CASCADE " +
                    "ON DELETE CASCADE " +
                    ");");
            statement.addBatch("INSERT INTO " + TB_DEPARTMENTS + "(name, num_workers) " +
                    "values " +
                    "('air', 3), " +
                    "('earth', 2), " +
                    "('water', 1), " +
                    "('dragons', 0);");
            statement.addBatch("INSERT INTO " + TB_WORKERS +
                    "(name, department, speciality, mail, assignment_date, completed_tasks) " +
                    "value " +
                    "('Aria', 'air', 'lightning', 'aria@mail.com', '2018-09-25', 16), " +
                    "('Mirin', 'air', 'cloud', 'mirin@smail.net', '2018-11-06', 14), " +
                    "('Ren', 'air', 'rain', 'ren@mail.com', '2018-10-22', 23), " +
                    "('Turin', 'earth', 'mountain', 'turin@smail.net', '2018-05-30', 34), " +
                    "('Ren', 'earth', 'flatland', 'renny@mail.com', '2018-04-19', 28), " +
                    "('Aquaris', 'water', 'oceans', 'aquaris@mail.net', '2019-01-17', 8);");
            statement.executeBatch();
            System.out.println("[S] Inserting starting data into database...");
            connection.setAutoCommit(true);
            System.out.println("[S] Starting data created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
