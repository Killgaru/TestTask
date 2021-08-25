package ServletRealization;

import java.util.ArrayList;
import java.util.HashMap;

class DisabledColumns {
    private static final HashMap<String, ArrayList<String>> disabledColumns = new HashMap<>();

    static {
        add(DB_Service.TB_DEPARTMENTS, "id");
        add(DB_Service.TB_DEPARTMENTS, "num_workers");
        add(DB_Service.TB_WORKERS, "id");
    }

    public static void add(String tableName, String columnName) {
        if (disabledColumns.containsKey(tableName)) {
            if (!disabledColumns.get(tableName).contains(columnName)) {
                disabledColumns.get(tableName).add(columnName);
            }
        } else {
            disabledColumns.put(tableName, new ArrayList<>());
            disabledColumns.get(tableName).add(columnName);
        }
    }

    public static void remove(String tableName, String columnName) {
        if (contains(tableName, columnName)) {
            disabledColumns.get(tableName).remove(columnName);
        }
    }

    public static boolean contains(String tableName, String columnName) {
        if (disabledColumns.containsKey(tableName)) {
            return disabledColumns.get(tableName).contains(columnName);
        }
        return false;
    }

}
