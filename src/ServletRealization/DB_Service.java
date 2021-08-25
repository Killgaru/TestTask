package ServletRealization;

import java.sql.*;
import java.util.LinkedList;

public class DB_Service {
    public static final String DB_NAME = "test_task";
    public static final String TB_DEPARTMENTS = "departments";
    public static final String TB_WORKERS = "workers";

    private Connection connection;

    public DB_Service(String USER_NAME, String PASSWORD, String URL) {
        setConnection(USER_NAME, PASSWORD, URL);
        try {
            createStartingData(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DB_Service() {
        this("Nilomy", "13542", "jdbc:mysql://localhost:3306/mysql");
    }

    private void setConnection(String USER_NAME, String PASSWORD, String URL) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            System.out.println("[S] eap! Connected!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private boolean isTableExist(String databaseName, String tableName) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT 1 FROM information_schema.tables " +
                        "WHERE table_schema = '" + databaseName + "' " +
                        "AND table_name = '" + tableName + "' " +
                        "LIMIT 1");
        return resultSet.next();
    }

    private boolean isDatabaseExist(String databaseName) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT 1 FROM information_schema.schemata " +
                        "WHERE schema_name = '" + databaseName + "' " +
                        "LIMIT 1");
        return resultSet.next();
    }

    private void createStartingData(boolean reCreate) throws SQLException {
        if (!reCreate
                && isDatabaseExist(DB_NAME)
                && isTableExist(DB_NAME, TB_DEPARTMENTS)
                && isTableExist(DB_NAME, TB_WORKERS)) {
            connection.setCatalog(DB_NAME);
            System.out.println("[S] Starting data already created.");
            return;
        }
        System.out.println("[S] Preparing starting data...");
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        if (reCreate)
            statement.addBatch("DROP DATABASE IF EXISTS " + DB_NAME);
        statement.addBatch("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        statement.executeBatch();
        connection.setCatalog(DB_NAME);
        statement = connection.createStatement();
        statement.addBatch("CREATE TABLE IF NOT EXISTS " + TB_DEPARTMENTS +
                "(id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL UNIQUE, " +
                "num_workers INT NOT NULL DEFAULT 0);");
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
        System.out.println("[S] Inserting starting data into database...");
        statement.executeBatch();
        connection.setAutoCommit(true);
        System.out.println("[S] Starting data created.");
    }

    public LinkedList<String> getTableColumns(String tableName) throws SQLException {
        ResultSet tableMetaData = connection.getMetaData()
                .getColumns(DB_NAME, null, tableName, "%");
        LinkedList<String> columnNames = new LinkedList<>();
        while (tableMetaData.next()) {
            columnNames.add(tableMetaData.getString("COLUMN_NAME"));
        }
        return columnNames;
    }

    public ResultSet getDepartments() throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_DEPARTMENTS);
        return ps.executeQuery();
    }

    public ResultSet getDepartment(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_DEPARTMENTS +
                        " WHERE id = ?");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    public ResultSet getDepartment(String name) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_DEPARTMENTS +
                        " WHERE name = ?");
        ps.setString(1, name);
        return ps.executeQuery();
    }

    public ResultSet getWorkersByDepartment(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_WORKERS +
                        " WHERE department = " +
                        "(SELECT name FROM " + TB_DEPARTMENTS +
                        " WHERE id = ?)");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    public ResultSet getWorkersByDepartment(String name) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_WORKERS +
                        " WHERE department = ?");
        ps.setString(1, name);
        return ps.executeQuery();
    }

    public ResultSet getWorkerById(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TB_WORKERS +
                        " WHERE id = ?");
        ps.setInt(1, id);
        return ps.executeQuery();
    }

    public ResultSet getMetaData(String tableName) throws SQLException {
        return connection.getMetaData().getColumns(DB_NAME, null, tableName, "%");
    }

    public void deleteRecord(String tableName, int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM " + tableName +
                        " WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private void sendDataToDB(String sql, FieldsConstructor... columns) throws SQLException {
        if (columns.length == 0) {
            return;
        }
        PreparedStatement ps = connection.prepareStatement(sql);
        int i = 1;
        for (FieldsConstructor fc : columns) {
//            if (fc.isDISABLED() || !fc.isChanged()) {
//                continue;
//            }
            if (fc.isVARCHAR()) {
                ps.setString(i, (String) fc.getValue());
            } else if (fc.isINTEGER()) {
                ps.setInt(i, (int) fc.getValue());
            } else if (fc.isDATE()) {
                ps.setDate(i, (Date) fc.getValue());
            }
            i++;
        }
        ps.executeUpdate();
    }

    public void addRecord(String tableName, FieldsConstructor... columns) {
        if (columns.length == 0) {
            return;
        }
        StringBuilder sql = new StringBuilder(),
                sqlColumns = new StringBuilder("("),
                sqlValues = new StringBuilder(" VALUES (");
//        boolean noValues = true;
        for (int i = 0; i < columns.length; i++) {
//            if (columns[i].isDISABLED() || !columns[i].isChanged()) {
//                continue;
//            }
//            noValues = false;
            sqlColumns.append(columns[i].getCOLUMN()).append(", ");
            sqlValues.append("?, ");
        }
//        if (noValues) {
//            System.out.println("No values for adding.");
//            return;
//        }
        sqlColumns.deleteCharAt(sqlColumns.lastIndexOf(",", sqlColumns.length() - 2));
        sqlValues.deleteCharAt(sqlValues.lastIndexOf(",", sqlValues.length() - 2));
        sqlColumns.append(")");
        sqlValues.append(")");
        sql.append("INSERT INTO ").append(tableName).append(sqlColumns).append(sqlValues);
        try {
            sendDataToDB(sql.toString(), columns);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeRecord(String tableName, int id, FieldsConstructor... columns) {
        if (columns.length == 0) {
            return;
        }
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
//        boolean noValues = true;
        String oldDepartmentName = null;
        String newDepartmentName = null;
        for (FieldsConstructor fs : columns) {
            if (isTB_WORKERS(fs.getTABLE()) && fs.getCOLUMN().equals("department")) {
                oldDepartmentName = getWorkerDepartment(id);
                newDepartmentName = (String) fs.getValue();
            }
//            if (fs.isDISABLED() || !fs.isChanged()) {
//                continue;
//            }
//            noValues = false;
            sql.append(fs.getCOLUMN()).append(" = ?, ");
        }
//        if (noValues) {
//            System.out.println("No values for changing.");
//            return;
//        }
        sql.deleteCharAt(sql.lastIndexOf(",", sql.length() - 2));
        sql.append("WHERE id = ").append(id);
        try {
            sendDataToDB(sql.toString(), columns);
            if (oldDepartmentName != null && newDepartmentName != null) {
                updateNumWorkers(oldDepartmentName, newDepartmentName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateNumWorkers(String... departmentsNames) throws SQLException{
        String sql = "UPDATE "+TB_DEPARTMENTS+
                " SET num_workers = (SELECT COUNT(*) FROM "+TB_WORKERS+" WHERE department = ?)" +
                " WHERE name = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (String name: departmentsNames) {
            ps.setString(1, name);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    public String getWorkerDepartment(int id) {
        try {
            ResultSet rs = getWorkerById(id);
            if (rs.next()) {
                return rs.getString("department");
            }
            throw new IllegalArgumentException("Wrong id: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getDepartmentName(int id) throws SQLException {
        ResultSet rs = getDepartment(id);
        if (rs.next()) {
            return rs.getString("name");
        }
        throw new IllegalArgumentException("Wrong id: " + id);
    }

    public int getDepartmentId(String name) throws SQLException {
        ResultSet rs = getDepartment(name);
        if (rs.next()) {
            return rs.getInt("id");
        }
        throw new IllegalArgumentException("Wrong name: " + name);
    }

    public int getDepartmentsCount() throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + TB_DEPARTMENTS);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public String[] getDepartmentsNames() {
        try {
            String[] names = new String[getDepartmentsCount()];
            ResultSet rs = getDepartments();
            for (int i = 0; rs.next(); i++) {
                names[i] = rs.getString("name");
            }
            return names;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    public boolean isEmailAssigned(String mail){
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM " + TB_WORKERS + " WHERE mail = ? ");
            ps.setString(1, mail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDepartmentExist(String departmentName) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM " + TB_DEPARTMENTS + " WHERE name = ? ");
            ps.setString(1, departmentName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isTB_DEPARTMENTS(String tableName) {
        return TB_DEPARTMENTS.equals(tableName);
    }

    public static boolean isTB_WORKERS(String tableName) {
        return TB_WORKERS.equals(tableName);
    }
}
