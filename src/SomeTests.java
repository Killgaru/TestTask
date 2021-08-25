import ServletRealization.FieldsConstructor;

import java.sql.*;
import java.util.Arrays;

public class SomeTests {
    public static final String USER_NAME = "Nilomy";
    public static final String PASSWORD = "13542";
    public static final String URL = "jdbc:mysql://localhost:3306/mysql";

    public static final String DB_NAME = "test_task";
    public static final String TB_DEPARTMENTS = "departments";
    public static final String TB_WORKERS = "workers";

    static Connection connection;
    static Statement statement;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            connection.setCatalog(DB_NAME);
            statement = connection.createStatement();
            System.out.println("[S] eap! Connected!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException{
        SomeTests st = new SomeTests();
        st.test11();
//        st.test10(1,2,3);
//        LinkedList<Integer> myList = new LinkedList<>();
//        myList.add(9);
//        st.test10(myList.toArray(new Integer[]{}));
//        allWorkersMetaParameters();
//        Integer aa =5, bb=7;
//        new SomeTests().test6(aa, bb);
//        System.out.println("aaaa: "+ aa);

    }

    private void test8() {
        FieldsConstructor<Integer> fieldsConstructor1 = new FieldsConstructor<>("table", "column", Types.INTEGER, 10, false);
        FieldsConstructor<String> fieldsConstructor2 = new FieldsConstructor<>("table", "column", Types.INTEGER, 10, false);
        fieldsConstructor1.setValue(25);
        fieldsConstructor2.setValue("some value");
        FieldsConstructor[] array = new FieldsConstructor[]{fieldsConstructor1, fieldsConstructor2};
        for (FieldsConstructor fc : array) {
            System.out.println(fc.getValue());
        }
    }

    /*
                                Output for test1
    [S] eap! Connected!
    Statement executeQuery 3000 times:								2057 ms
    PreparedStatement (recreate in loop) executeQuery 3000 times:	1889 ms
    PreparedStatement (create before loop) executeQuery 3000 times:	1140 ms
    PreparedStatement (recreate before loop) executeQuery 3000 times:	1119 ms

    Process finished with exit code 0

    Вывод: для повышения быстродействия лучше создать заранее PreparedStatement
    и подставлять различные запросы через connection.

    PS. Подставлять можно только значения. Названия таблиц, колонок и т.д. - нельзя.
    */
    public static void test1() throws SQLException {
        String sql = "SELECT * FROM " + TB_WORKERS + " WHERE id > 3";
        String sqlp = "SELECT * FROM " + TB_WORKERS + " WHERE id > ?";
        int count = 3000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            statement.executeQuery(sql);
        }
        System.out.println("Statement executeQuery " + count + " times:\t\t\t\t\t\t\t\t" + (System.currentTimeMillis() - startTime) + " ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlp);
            preparedStatement.setInt(1, 3);
            preparedStatement.executeQuery();
        }
        System.out.println("PreparedStatement (recreate in loop) executeQuery " + count + " times:\t" + (System.currentTimeMillis() - startTime) + " ms");
        startTime = System.currentTimeMillis();
        PreparedStatement preparedStatement2 = connection.prepareStatement(sqlp);
        for (int i = 0; i < count; i++) {
            preparedStatement2.setInt(1, 3);
            preparedStatement2.executeQuery();
        }
        System.out.println("PreparedStatement (create before loop) executeQuery " + count + " times:\t" + (System.currentTimeMillis() - startTime) + " ms");
        startTime = System.currentTimeMillis();
        preparedStatement2 = connection.prepareStatement(sqlp);
        for (int i = 0; i < count; i++) {
            preparedStatement2.setInt(1, 2);
            preparedStatement2.executeQuery();
        }
        System.out.println("PreparedStatement (recreate before loop) executeQuery " + count + " times:\t" + (System.currentTimeMillis() - startTime) + " ms");
    }

    enum menu {
        ACTION1,
        ACTION2,
        ACTION3;



    }

    static void test2() {
        System.out.println(menu.ACTION1);
        menu.ACTION1.name();
    }

    static void test3(String columnNamePattern) {
        System.out.println("**************************");
        try {
            ResultSet rs = connection.getMetaData().getColumns(DB_NAME, null, TB_DEPARTMENTS, columnNamePattern);
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void test4(String... strings) {
        if (strings == null) {
            System.out.println("It null");
        } else if (strings.length == 0) {
            System.out.println("no strings");
        } else {
            System.out.println(Arrays.deepToString(strings));
        }
    }

    static void test5() throws SQLException{
        ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT 1 FROM information_schema.tables " +
                        "WHERE table_schema = '"+DB_NAME+"' " +
                        "AND table_name = '"+TB_WORKERS+"' " +
                        "LIMIT 1");
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }

    private static void allWorkersMetaParameters() throws SQLException {
        String[] listNamesOfParameters = new String[]{
                "TABLE_CAT",
                "TABLE_SCHEM",
                "TABLE_NAME",
                "COLUMN_NAME",
                "DATA_TYPE",
                "TYPE_NAME",
                "COLUMN_SIZE",
                "BUFFER_LENGTH",
                "DECIMAL_DIGITS",
                "NUM_PREC_RADIX",
                "NULLABLE",
                "REMARKS",
                "COLUMN_DEF",
                "SQL_DATA_TYPE",
                "SQL_DATETIME_SUB",
                "CHAR_OCTET_LENGTH",
                "ORDINAL_POSITION",
                "IS_NULLABLE",
                "SCOPE_CATALOG",
                "SCOPE_SCHEMA",
                "SCOPE_TABLE",
                "SOURCE_DATA_TYPE",
                "IS_AUTOINCREMENT",
                "IS_GENERATEDCOLUMN"
        };
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getColumns(DB_NAME, null, TB_WORKERS, "%");
        while (rs.next()) {
            for (int i = 1; i <= listNamesOfParameters.length; i++) {
                System.out.println(i + " - " + listNamesOfParameters[i - 1] + ": " + rs.getString(i));
            }
            System.out.println("--------------------");
        }
    }

    private void test6(Integer a, Integer valB) {
        System.out.println("Before function: " + a);
        System.out.println("From function: " + forTest6(a, valB));
        System.out.println("After function: " + a);
        Integer[] array = new Integer[1];
        array[0] = a;
        System.out.println("a = "+a+";\tarray[0] = " + array[0]);
        /*array =*/ forTest6(array);
        System.out.println("a = "+a+";\tarray[0] = " + array[0]);
    }

    private Integer[] forTest6(Integer[] arr) {
        arr[0] = 77;
        System.out.println("arr[0] = " + arr[0]);
        return arr;
    }

    private Integer forTest6(Integer a, Integer b) {
        a+=b;
        System.out.println("In function: " + a);
        return a;
    }

    private void test7() {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+ TB_DEPARTMENTS+"(name, num_workers) VALUES(?, ?)");
            ps.setInt(1, 1234);
            ps.setString(2, "30");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void test9() {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+ TB_DEPARTMENTS+"(name, num_workers) VALUES(?, ?)");
            ps.setInt(1, 1234);
            ps.setString(2, "ahaha");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void test10(Integer... myI) {
        if (myI == null) {
            System.out.println("myI == null");
            return;
        }
        if (myI.length == 0) {
            System.out.println("myI.length == 0");
            return;
        }
        System.out.println("myI = " + Arrays.toString(myI));
    }

    private void test11() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("something and more, and more, and more, ...");
        System.out.println(stringBuilder);
        System.out.println(stringBuilder.length());
        System.out.println(stringBuilder.lastIndexOf(","));
        System.out.println(stringBuilder.lastIndexOf(",", stringBuilder.length()-6));
    }
}
