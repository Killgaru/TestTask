package ServletRealization.Servlets;

import ServletRealization.DB_Service;
import ServletRealization.FieldsConstructor;
import ServletRealization.InfoInConsole;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;

@WebServlet(name = "sFields", urlPatterns = "/fields")
public class Fields extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp, new LinkedList<>());
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp, LinkedList<FieldsConstructor> fields) throws ServletException, IOException {
        InfoInConsole.printParameters(req, "Fields Get");

        Map<String, String[]> parametersMap = req.getParameterMap();
        DB_Service service = new DB_Service();
        String table = req.getParameter("table");
        if (table == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (DB_Service.isTB_WORKERS(table)) {
            req.setAttribute("departmentsNames", service.getDepartmentsNames());
        }
        boolean selectInvalidFields = false;
        if (fields.size() > 0) {
            selectInvalidFields = true;
        }
        if (parametersMap.containsKey("add")) {
            System.out.println("Adding action");
            reSetParameters(req, "table", "department", "selected_id", "add");
            if (!selectInvalidFields) {
                req.setAttribute("fields", prepareFields(service, table));
            }
        } else if (parametersMap.containsKey("change")) {
            System.out.println("Change action");
            reSetParameters(req, "table", "department", "selected_id", "change");
            int id = getSelectedId(req);
            if (!selectInvalidFields) {
                req.setAttribute("fields", prepareFields(service, table, id));
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (selectInvalidFields) {
            req.setAttribute("fields", fields);
        }

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/jsp/fieldsBuilder.jsp");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InfoInConsole.printParameters(req, "Fields Post");

        DB_Service service = new DB_Service();
        String tableName = req.getParameter("table");
        StringBuilder redirectPath = new StringBuilder(req.getContextPath());
        redirectPath.append("/table?selected_id=").append(req.getParameter("selected_id")).
                append("&table=").append(req.getParameter("table"));
        if (tableName == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if (DB_Service.isTB_WORKERS(tableName)) {
            redirectPath.append("&department=").append(req.getParameter("department"));
        }
        if (req.getParameter("add") != null) {
            if (!actionAdd(req, resp, service, tableName)) {
                return;
            }
        } else if (req.getParameter("change") != null) {
            if (!actionChange(req, resp, service, tableName)) {
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        resp.sendRedirect(redirectPath.toString());
    }

    private boolean actionAdd(HttpServletRequest req,HttpServletResponse resp, DB_Service service, String tableName) throws ServletException, IOException {
        System.out.println("Add action");
        LinkedList<FieldsConstructor> fields = prepareFields(service, tableName);
        if (!setValueItems(req, fields)) {
            doGet(req, resp, fields);
            return false;
        }
        removeDisabledAndNoChangedItems(fields);
        if (fields.size() > 0) {
            service.addRecord(tableName, fields.toArray(new FieldsConstructor[0]));
        } else {
            System.out.println("No values for adding.");
        }
        return true;
    }

    private boolean actionChange(HttpServletRequest req, HttpServletResponse resp,DB_Service service, String tableName) throws ServletException, IOException {
        System.out.println("Change action");
        LinkedList<FieldsConstructor> fields = prepareFields(service, tableName, getSelectedId(req));
        if (!setValueItems(req, fields)) {
            doGet(req, resp, fields);
            return false;
        }
        removeDisabledAndNoChangedItems(fields);
        if (fields.size() > 0) {
            service.changeRecord(tableName, getSelectedId(req), fields.toArray(new FieldsConstructor[0]));
        } else {
            System.out.println("No values for changing.");
        }
        return true;
    }

    private void removeDisabledAndNoChangedItems(LinkedList<FieldsConstructor> fields) {
        int i = 0;
        while (i < fields.size()) {
            if (fields.get(i).isDISABLED() || !fields.get(i).isChanged()) {
                fields.remove(i);
                continue;
            }
            i++;
        }
    }

    private boolean setValueItems(HttpServletRequest req, LinkedList<FieldsConstructor> fields) {
        boolean valid = true;
        for (FieldsConstructor fc : fields) {
            if (!fc.isDISABLED()) {
                if (!fc.setValue(req.getParameter(fc.getCOLUMN()), true)) {
                    valid = false;
                }
            }
/*            if (fc.isINTEGER()) {
                fc.setValue(Integer.parseInt(req.getParameter(fc.getCOLUMN())));
            } else if (fc.isVARCHAR()) {
                fc.setValue(req.getParameter(fc.getCOLUMN()));
            } else if (fc.isDATE()) {
                fc.setValue(Date.valueOf(req.getParameter(fc.getCOLUMN())));
            }*/
        }
        return valid;
    }

    private LinkedList<FieldsConstructor> prepareFields(DB_Service service, String tableName, int id) {
        LinkedList<FieldsConstructor> data = prepareFields(service, tableName);
        try {
            ResultSet values = null;
            if (DB_Service.isTB_DEPARTMENTS(tableName)) {
                values = service.getDepartment(id);
            } else if (DB_Service.isTB_WORKERS(tableName)) {
                values = service.getWorkerById(id);
            }
            if (values.next()) {
                for (FieldsConstructor f : data) {
                    if (f.isINTEGER()) {
                        f.setValue(values.getInt(f.getCOLUMN()));
                    } else if (f.isVARCHAR()) {
                        f.setValue(values.getString(f.getCOLUMN()));
                    } else if (f.isDATE()) {
                        f.setValue(values.getDate(f.getCOLUMN()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private LinkedList<FieldsConstructor> prepareFields(DB_Service service, String tableName) {
        LinkedList<FieldsConstructor> data = new LinkedList<>();
        try {
            ResultSet rs = service.getMetaData(tableName);
            while (rs.next()) {
                int type = rs.getInt("DATA_TYPE");
                if (type == Types.INTEGER) {
                    data.addLast(
                            new FieldsConstructor<Integer>(
                                    tableName,
                                    rs.getString("COLUMN_NAME"),
                                    type,
                                    rs.getInt("COLUMN_SIZE"),
                                    rs.getString("IS_NULLABLE")));
                } else if (type == Types.VARCHAR) {
                    data.addLast(
                            new FieldsConstructor<String>(
                                    tableName,
                                    rs.getString("COLUMN_NAME"),
                                    type,
                                    rs.getInt("COLUMN_SIZE"),
                                    rs.getString("IS_NULLABLE")));
                } else if (type == Types.DATE) {
                    data.addLast(
                            new FieldsConstructor<Date>(
                                    tableName,
                                    rs.getString("COLUMN_NAME"),
                                    type,
                                    rs.getInt("COLUMN_SIZE"),
                                    rs.getString("IS_NULLABLE")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private int getSelectedId(HttpServletRequest req) {
        String id = req.getParameter("selected_id");
        return Integer.parseInt(id);
    }

    private void reSetParameters(HttpServletRequest req, String... parameters) {
        for (String p : parameters) {
            String parameter = req.getParameter(p);
            if (parameter != null) {
                req.setAttribute(p, parameter);
            }
        }
    }

    private void reSetAllParameters(HttpServletRequest req) {
        reSetParameters(req, req.getParameterMap().keySet().toArray(new String[0]));
    }
}
