package ServletRealization;

import java.sql.Date;
import java.sql.Types;
import java.util.regex.Pattern;

public class FieldsConstructor<T> {
    private final String TABLE;
    private final String COLUMN;
    private final int DATA_TYPE;
    private final int COLUMN_SIZE;
    private final boolean NULLABLE;
    private final boolean DISABLED;
    private T value;
    private String invalidValue;
    private String invalidMessage;
    private boolean changed;

    public FieldsConstructor(String table, String column, int DATA_TYPE, int COLUMN_SIZE,
                             boolean IS_NULLABLE) {
        TABLE = table;
        COLUMN = column;
        this.DATA_TYPE = DATA_TYPE;
        this.COLUMN_SIZE = COLUMN_SIZE;
        this.NULLABLE = IS_NULLABLE;
        this.DISABLED = DisabledColumns.contains(table, column);
    }

    public FieldsConstructor(String table, String column, int DATA_TYPE, int COLUMN_SIZE,
                             String IS_NULLABLE) {
        TABLE = table;
        COLUMN = column;
        this.DATA_TYPE = DATA_TYPE;
        this.COLUMN_SIZE = COLUMN_SIZE;
        this.NULLABLE = convertYesNoToBoolean(IS_NULLABLE);
        this.DISABLED = DisabledColumns.contains(table, column);
    }

    public static boolean convertYesNoToBoolean(String yesNo) {
        return "YES".equals(yesNo);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean setValue(String value, boolean validate) {
        if (validate) {
            if (validate(value)) {
                return true;
            }
        } else {
            if (this.value instanceof String) {
                setValue((T) value);
                return true;
            }
        }
        invalidValue = value;
        System.out.println("Invalid value: " + invalidValue + "; message: " + invalidMessage);
        return false;
    }

    private boolean validate(String value) {
        if (isINTEGER()) {
            try {
                T validValue = (T) Integer.valueOf(value);
                if (setChanged(validValue)) {
                    setValue(validValue);
                }
                return true;
            } catch (NumberFormatException e) {
                invalidMessage = "Please enter integer number.";
                e.printStackTrace();
            }
        } else if (isVARCHAR()) {
            if (value.length() <= COLUMN_SIZE) {
                DB_Service service = new DB_Service();
                if (DB_Service.isTB_WORKERS(TABLE)) {
                    if (COLUMN.equals("mail")) {
                        if (!Pattern.matches("\\w+@\\w+\\.\\w+", value)) {
                            invalidMessage = "Wrong e-mail. Please enter correct e-mail address.";
                        }
                        T validValue = (T) value;
                        if (setChanged(validValue) && service.isEmailAssigned(value)) {
                            invalidMessage = "This e-mail is taken. Try another.";
                        } else {
                            setValue(validValue);
                            return true;
                        }
                    } else if (COLUMN.equals("department")) {
                        if (service.isDepartmentExist(value)) {
                            T validValue = (T) value;
                            if (setChanged(validValue)) {
                                setValue(validValue);
                            }
                            return true;
                        } else {
                            invalidMessage = "Please select department from list.";
                        }
                    } else {
                        T validValue = (T) value;
                        if (setChanged(validValue)) {
                            setValue(validValue);
                        }
                        return true;
                    }
                } else if (DB_Service.isTB_DEPARTMENTS(TABLE)) {
                    T validValue = (T) value;
                    if (setChanged(validValue)){
                        if (COLUMN.equals("name") && service.isDepartmentExist(value)) {
                            invalidMessage = "This name is taken. Try another.";
                        } else {
                            setValue(validValue);
                            return true;
                        }
                    } else {
                        return true;
                    }
                } else {
                    invalidMessage = "Something goes wrong...";
                }
            } else {
                invalidMessage = "Please enter shortly value.";
            }
        } else if (isDATE()) {
            try {
                T validValue = (T) Date.valueOf(value);
                if (setChanged(validValue)) {
                    setValue(validValue);
                }
                return true;
            } catch (Exception e) {
                invalidMessage = "Please enter correct date.";
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getTABLE() {
        return TABLE;
    }

    public String getCOLUMN() {
        return COLUMN;
    }

    public int getDATA_TYPE() {
        return DATA_TYPE;
    }

    public int getCOLUMN_SIZE() {
        return COLUMN_SIZE;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public String getInvalidMessage() {
        return invalidMessage;
    }

    public boolean isNULLABLE() {
        return NULLABLE;
    }

    public boolean isDISABLED() {
        return DISABLED;
    }

    public boolean isVARCHAR() {
        return DATA_TYPE == Types.VARCHAR;
    }

    public boolean isINTEGER() {
        return DATA_TYPE == Types.INTEGER;
    }

    public boolean isDATE() {
        return DATA_TYPE == Types.DATE;
    }

    public boolean isValid() {
        return invalidValue == null;
    }

    private boolean setChanged(T validValue) {
        changed = isChanged(validValue);
        return changed;
    }

    private boolean isChanged(T validValue) {
        return !validValue.equals(value);
    }

    public boolean isChanged() {
        return changed;
    }
}

