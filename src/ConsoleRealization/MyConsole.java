package ConsoleRealization;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.regex.Pattern;

public class MyConsole extends Logic {
    private static String currentDepartmentName;
    private static final Scanner INPUT = new Scanner(System.in);

    private static PreparedStatement preparedStatement;

    // new
    private static void showDepartments() throws SQLException {
        StringBuffer message = new StringBuffer("\t\tDepartments:\n");
        ResultSet dep = statement.executeQuery(
                "SELECT * FROM " + TB_DEPARTMENTS);
        while (dep.next()) {
            message.append("id: ").append(dep.getInt("id")).
                    append(", name: ").append(dep.getString("name")).
                    append(", workers: ").append(dep.getInt("num_workers")).append("\n");
        }
        System.out.println(message);
    }

    // new
    private static boolean showDepartment(String nameDepartment) throws SQLException {
        StringBuffer message = new StringBuffer();
        ResultSet dep = statement.executeQuery(
                "SELECT * FROM " + TB_DEPARTMENTS + " WHERE name = '" + nameDepartment + "'");
        while (dep.next()) {
            message.append("id: ").append(dep.getInt("id")).
                    append(", name: ").append(dep.getString("name")).
                    append(", workers: ").append(dep.getInt("num_workers"));
        }
        if (message.length() == 0) {
            System.out.println("Department " + nameDepartment + " not exists.");
            return false;
        }
        System.out.println(message.insert(0, "\t\tDepartment info:\n"));
        return true;
    }

    // new
    private static void chooseDepartment() throws SQLException {
        while (true) {
            showDepartments();
            printActions("", new menu[]{menu.Add, menu.Exit});
            System.out.println("Enter department id or action code:");
            String code = INPUT.next();
            // Check code and do actions
            if (menu.checkItem(code)) {
                switch (menu.getItem(code)) {
                    case Add:
                        addDepartment();
                        break;
                    case Exit:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong code!");
                        break;
                }
                continue;
            }
            // Check id and go on
            int id;
            try {
                id = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                System.out.println("Wrong id!");
                continue;
            }
            if (!setCurrentDepartmentName(id)) {
                System.out.println("No departments with this id: " + id);
                continue;
            }
            departmentActions(id);
        }
    }

    // new
    private static boolean setCurrentDepartmentName(int departmentId) throws SQLException {
        preparedStatement = connection.prepareStatement(
                "SELECT name FROM " + TB_DEPARTMENTS + " WHERE id = ?");
        preparedStatement.setInt(1, departmentId);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            currentDepartmentName = rs.getString(1);
            return true;
        }
        return false;
    }

    // new
    private static void departmentActions(int idDepartment) throws SQLException {
        while (true) {
            if (!showDepartment(currentDepartmentName)) {
                return;
            }
            printActions("Worker ", new menu[]{menu.Delete, menu.Change, menu.List, menu.Back, menu.Exit});
            System.out.println("Enter action code:");
            String code = INPUT.next();
            if (menu.checkItem(code)) {
                switch (menu.getItem(code)) {
                    case Change:
                        changeDepartment(idDepartment);
                        break;
                    case List:
                        int i = chooseWorker();
                        if (i == 0) {
                            break;
                        }
                        return;
                    case Delete:
                        deleteDepartment(idDepartment);
                    case Back:
                        return;
                    case Exit:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong code!");
                        break;
                }
                continue;
            }
            System.out.println("Wrong code!");
        }
    }

    // new
    private static int chooseWorker() throws SQLException {
        while (true) {
            showWorkers();
            printActions("Departments ", new menu[]{menu.Add, menu.List, menu.Back, menu.Exit});
            System.out.println("Enter worker id or action code:");
            String code = INPUT.next();
            // Check code and do actions
            if (menu.checkItem(code)) {
                switch (menu.getItem(code)) {
                    case Add:
                        addWorker();
                        break;
                    case List:
                        return 1;
                    case Back:
                        return 0;
                    case Exit:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong code!");
                }
                continue;
            }
            int id;
            try {
                id = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                System.out.println("Wrong id!");
                continue;
            }
            if (workerActions(id) == 0) {
                continue;
            }
            return 1;
        }
    }

    // new
    private static void showWorkers() throws SQLException {
        showWorkers(currentDepartmentName);
    }

    // new
    private static void showWorkers(String departmentName) throws SQLException {
        preparedStatement = connection.prepareStatement(
                "SELECT * FROM " + TB_WORKERS + " WHERE department = ?");
        preparedStatement.setString(1, departmentName);
        ResultSet rs = preparedStatement.executeQuery();
        StringBuffer forPrint = new StringBuffer("\t\tDepartment: " + departmentName + "\n");
        while (rs.next()) {
            for (String column : getWorkersColumnNames()) {
                forPrint.append(column).append(":\t").append(rs.getString(column)).append("\n");
            }
            forPrint.append("\n");
        }
        System.out.println(forPrint);
    }

    // new
    private static int workerActions(int idWorker) throws SQLException {
        while (true) {
            if (!showWorker(idWorker)) {
                return 0;
            }
            printActions("Department ", new menu[]{menu.Delete, menu.Change, menu.List, menu.Back, menu.Exit});
            System.out.println("Enter action code:");
            String code = INPUT.next();
            if (menu.checkItem(code)) {
                switch (menu.getItem(code)) {
                    case Change:
                        changeWorker(idWorker);
                        break;
                    case Delete:
                        deleteWorker(idWorker);
                    case List:
                        return 1;
                    case Back:
                        return 0;
                    case Exit:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Wrong code!");
                        break;
                }
                continue;
            }
            System.out.println("Wrong code!");
        }
    }

    // new
    private static boolean showWorker(int idWorker) throws SQLException {
        LinkedList<String> columns = getWorkersColumnNames();
        preparedStatement = connection.prepareStatement(
                "SELECT * FROM " + TB_WORKERS + " WHERE id = ? AND department = ?");
        preparedStatement.setInt(1, idWorker);
        preparedStatement.setString(2, currentDepartmentName);
        ResultSet rs = preparedStatement.executeQuery();
        StringBuffer forPrint = new StringBuffer();
        while (rs.next()) {
            for (String column : columns) {
                forPrint.append(column).append(":\t").append(rs.getString(column)).append("\n");
            }
            forPrint.append("\n");
        }
        if (forPrint.length() == 0) {
            System.out.println("No workers with id: " + idWorker + " in department " + currentDepartmentName);
            return false;
        }
        System.out.println(forPrint.insert(0, "\t\tWorker info:\n"));
        return true;
    }

    // new
    private static boolean setColumnValueInPrepStat(Scanner sc, int index, String tableName, String columnName)
            throws SQLException {
        switch (getColumnDataType(tableName, columnName)) {
            case Types.VARCHAR:
                String input = sc.next();
                if (input.length() > getColumnSize(tableName, columnName)) {
                    System.out.println("Input too long! Try again:");
                    return false;
                }
                if (columnName.equals("mail") && !Pattern.matches("\\w+@\\w+\\.\\w+", input)) {
                    System.out.println("Wrong e-mail! Try again:");
                    return false;
                }
                if (columnName.equals("department")) {
                    ResultSet rs = statement.executeQuery("SELECT name FROM " + TB_DEPARTMENTS +
                            " WHERE name ='" + input + "'");
                    if (!rs.next()) {
                        System.out.println("Not exist department with name " + input);
                        return false;
                    }
                }
                preparedStatement.setString(index, input);
                break;
            case Types.INTEGER:
                if (!sc.hasNextInt()) {
                    sc.next();
                    System.out.println("Input not integer! Try again:");
                    return false;
                }
                preparedStatement.setInt(index, sc.nextInt());
                break;
            case Types.DATE:
                try {
                    preparedStatement.setDate(index, Date.valueOf(sc.next()));
                } catch (IllegalArgumentException e) {
                    System.out.println("Wrong date. Use format yyyy-mm-dd and try again:");
                    return false;
                }
                break;
            default:
                System.out.println("Ooops! Something goes wrong...");
                return false;
        }
        return true;
    }

    // new
    private static void showChangeableColumns(String table, int id) throws SQLException {
        LinkedList<String> columns = getChangeableColumnsNames(table);
        StringBuilder sql = new StringBuilder("SELECT ");
        for (String col : columns) {
            sql.append(col).append(", ");
        }
        sql.delete(sql.length() - 2, sql.length() - 1).append("FROM ").append(table).append(" WHERE id = ?");
        preparedStatement = connection.prepareStatement(sql.toString());
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            StringBuffer forPrint = new StringBuffer();
            for (int i = 1; i <= columns.size(); i++) {
                forPrint.append(i).append("\t")
                        .append(columns.get(i - 1)).append(": ")
                        .append(rs.getString(i)).append("\n");
            }
            System.out.println(forPrint);
        } else {
            System.out.println("Ooops! Something goes wrong...");
        }

    }

    // new
    private static void addDepartment() throws SQLException {
        add(TB_DEPARTMENTS);
    }

    // new
    private static void addWorker() throws SQLException {
        add(TB_WORKERS);
        updateWorkersCount();
    }

    // new
    private static void add(String table) throws SQLException {
        LinkedList<String> list = getChangeableColumnsNames(table);
        if (TB_DEPARTMENTS.equals(table)) {
            list.add("num_workers");
        }
        if (TB_WORKERS.equals(table)) {
            list.remove("department");
            list.addLast("department");
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append("(");
        StringBuilder sqlVal = new StringBuilder("VALUES(");
        for (String col : list) {
            sql.append(col).append(",");
            sqlVal.append("?,");
        }
        sqlVal.delete(sqlVal.length() - 1, sqlVal.length()).append(")");
        sql.delete(sql.length() - 1, sql.length()).append(") ").append(sqlVal);
        preparedStatement = connection.prepareStatement(sql.toString());
        if (TB_DEPARTMENTS.equals(table)) {
            list.remove("num_workers");
        }
        if (TB_WORKERS.equals(table)) {
            list.remove("department");
        }
        System.out.println("\tEnter parameters:");
        int i;
        for (i = 1; i <= list.size(); i++) {
            do {
                System.out.print(list.get(i - 1) + ": ");
            } while (!setColumnValueInPrepStat(INPUT, i, table, list.get(i - 1)));
        }
        if (TB_DEPARTMENTS.equals(table)) {
            preparedStatement.setInt(i, 0);
        }
        if (TB_WORKERS.equals(table)) {
            preparedStatement.setString(i, currentDepartmentName);
        }
        preparedStatement.executeUpdate();
        System.out.println("[S] Record added successful.");
    }

    // new
    private static void deleteDepartment(int idDepartment) throws SQLException {
        delete(TB_DEPARTMENTS, idDepartment);
    }

    // new
    private static void deleteWorker(int idWorker) throws SQLException {
        delete(TB_WORKERS, idWorker);
        updateWorkersCount();
    }

    // new
    private static void updateWorkersCount(String... departments) throws SQLException {
        connection.setAutoCommit(false);
        preparedStatement = connection.prepareStatement("UPDATE " + TB_DEPARTMENTS +
                " SET num_workers = " +
                "(SELECT COUNT(*) FROM " + TB_WORKERS +
                " WHERE department = ?)" +
                " WHERE name = ?");
        preparedStatement.setString(1, currentDepartmentName);
        preparedStatement.setString(2, currentDepartmentName);
        preparedStatement.addBatch();
        for (String depName : departments) {
            preparedStatement.setString(1, depName);
            preparedStatement.setString(2, depName);
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        connection.setAutoCommit(true);
    }

    // new
    private static void delete(String table, int id) throws SQLException {
        preparedStatement = connection.prepareStatement(
                "DELETE FROM " + table + " WHERE id = ?");
        preparedStatement.setInt(1, id);
        preparedStatement.execute();
        System.out.println("[S] Deleted.");
    }

    // new
    private static void changeDepartment(int idDepartment) throws SQLException {
        change(TB_DEPARTMENTS, idDepartment);
        preparedStatement = connection.prepareStatement("SELECT name FROM " + TB_DEPARTMENTS + " WHERE id = ?");
        preparedStatement.setInt(1, idDepartment);
        ResultSet name = preparedStatement.executeQuery();
        if (name.next()) {
            currentDepartmentName = name.getString("name");
        } else {
            System.out.println("[S] Ooops! Something goes wrong...");
        }
    }

    // new
    private static void changeWorker(int idWorker) throws SQLException {
        String changedColumnName = change(TB_WORKERS, idWorker);
        if (changedColumnName.equals("department")) {
            ResultSet rs = statement.executeQuery("SELECT department FROM " + TB_WORKERS +
                    " WHERE id ='" + idWorker + "'");
            if (rs.next()) {
                updateWorkersCount(rs.getString(1));
            } else {
                System.out.println("[S] Ooops! Something goes wrong...");
            }
        } else {
            updateWorkersCount();
        }
    }

    // new
    private static String change(String table, int id) throws SQLException {
        LinkedList<String> list = getChangeableColumnsNames(table);
        String column;
        while (true) {
            System.out.println("Enter parameter number for changing or action code.");
            showChangeableColumns(table, id);
            printActions("", new menu[]{menu.Back, menu.Exit});
            String code = INPUT.next();
            if (menu.checkItem(code)) {
                switch (menu.getItem(code)) {
                    case Back:
                        return "";
                    case Exit:
                        System.exit(0);
                    default:
                        System.out.println("Wrong code! Try again.");
                }
                continue;
            }
            int i;
            try {
                i = Integer.parseInt(code) - 1;
            } catch (NumberFormatException e) {
                System.out.println("You're entered not integer value! Try again.");
                continue;
            }
            if (i >= 0 && i < list.size()) {
                column = list.get(i);
                break;
            }
            System.out.println("You're entered wrong number! Try again.");
        }
        preparedStatement = connection.prepareStatement("UPDATE " + table + " SET " + column + " = ? WHERE id = ?");
        do {
            System.out.println("Enter new value for " + column + ":");
        } while (!setColumnValueInPrepStat(INPUT, 1, table, column));
        preparedStatement.setInt(2, id);
        preparedStatement.executeUpdate();
        System.out.println("[S] Changing successful.");
        return column;
    }

    // new
    private enum menu {
        Add("-1"),
        Delete("-2"),
        Change("-3"),
        List("-4"),
        Back("-9"),
        Exit("0");

        private final String code;
        private static boolean cachedItemFlag;
        private static String cachedCode;
        private static menu cachedItem = Add;

        menu(String code) {
            this.code = code;
        }

        public static boolean checkItem(String code) {
            if (code.equals(cachedCode)) {
                return cachedItemFlag;
            }
            cachedCode = code;
            for (menu m : menu.values()) {
                if (m.code.equals(code)) {
                    cachedItem = m;
                    cachedItemFlag = true;
                    return true;
                }
            }
            cachedItemFlag = false;
            return false;
        }

        public static menu getItem(String code) {
            if (checkItem(code)) {
                return cachedItem;
            }
            throw new IllegalArgumentException("Not exist menu element with code: " + code);
        }
    }

    // new
    private static void printActions(String listName, menu[] menuItems) {
        StringBuffer actions = new StringBuffer("\t\tActions:\n");
        for (menu m : menuItems) {
            if (m == menu.List) {
                actions.append(m.code).append("\t").append(listName).append(m).append("\n");
            } else {
                actions.append(m.code).append("\t").append(m).append("\n");
            }
        }
        System.out.print(actions);
    }

    public static void start() {
        try {
//            createStartingData();
            connection.setCatalog(DB_NAME);
            statement = connection.createStatement();
            chooseDepartment();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
