package ServletRealization.Servlets;

import ServletRealization.DB_Service;
import ServletRealization.InfoInConsole;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

@WebServlet(name = "sTable", urlPatterns = "/table")
public class Table extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InfoInConsole.printParameters(req, "Table Get");

        DB_Service service = new DB_Service();
        String table = req.getParameter("table");
        if (table == null) {
            setTableData(req, service, DB_Service.TB_DEPARTMENTS);
        } else if (!DB_Service.isTB_DEPARTMENTS(table) && !DB_Service.isTB_WORKERS(table)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if (req.getParameter("list") != null) {
            actionList(req, service, table);
        } else if (req.getParameter("cancel") != null) {
            actionCancel(req, service, table);
        } else {
            actionCancel(req, service, table);
        }
        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/jsp/tableBuilder.jsp");
        requestDispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InfoInConsole.printParameters(req, "Table Post");

        DB_Service service = new DB_Service();
        if (req.getParameter("delete") != null) {
            System.out.println("Delete action");
            try {
                if (isRequestFromTable(req, DB_Service.TB_DEPARTMENTS)) {
                    service.deleteRecord(DB_Service.TB_DEPARTMENTS, getSelectedId(req));
                } else if (isRequestFromTable(req, DB_Service.TB_WORKERS)) {
                    service.deleteRecord(DB_Service.TB_WORKERS, getSelectedId(req));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        doGet(req, resp);
    }

    private void actionList(HttpServletRequest req, DB_Service service, String tableName) {
        System.out.println("List action");
        if (DB_Service.isTB_DEPARTMENTS(tableName)) {
            setTableData(req, service, DB_Service.TB_WORKERS);
        } else if (DB_Service.isTB_WORKERS(tableName)) {
            setTableData(req, service, DB_Service.TB_DEPARTMENTS);
            try {
                String departmentName = req.getParameter("department");
                int departmentId = service.getDepartmentId(departmentName);
                req.setAttribute("selected_id", departmentId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void actionCancel(HttpServletRequest req, DB_Service service, String tableName) {
        reSetParameters(req, "selected_id");
        if (DB_Service.isTB_DEPARTMENTS(tableName)) {
            setTableData(req, service, DB_Service.TB_DEPARTMENTS);
        } else if (DB_Service.isTB_WORKERS(tableName)) {
            setTableData(req, service, DB_Service.TB_WORKERS, req.getParameter("department"));
        }
    }

    private void setTableData(HttpServletRequest req, DB_Service service, String tableName) {
        setTableData(req, service, tableName, "");
    }

    private void setTableData(HttpServletRequest req, DB_Service service, String tableName, String departmentName) {
        try {
            LinkedList<String> columnNames = service.getTableColumns(tableName);
            ResultSet rs;
            if (DB_Service.isTB_DEPARTMENTS(tableName)) {
                rs = service.getDepartments();
            } else if (DB_Service.isTB_WORKERS(tableName)) {
                if (departmentName.equals("")) {
                    int id = getSelectedId(req);
                    departmentName = service.getDepartmentName(id);
                }
                rs = service.getWorkersByDepartment(departmentName);
                req.setAttribute("department", departmentName);
            } else {
                throw new IllegalArgumentException("Wrong tableName: " + tableName);
            }
            LinkedList<LinkedList<String>> tableData = prepareTableData(rs, columnNames);
            req.setAttribute("columnNames", columnNames);
            req.setAttribute("tableData", tableData);
            req.setAttribute("table", tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private LinkedList<LinkedList<String>> prepareTableData(ResultSet rs, LinkedList<String> columnNames) {
        LinkedList<LinkedList<String>> data = new LinkedList<>();
        try {
            while (rs.next()) {
                data.addLast(new LinkedList<>());
                for (String name : columnNames) {
                    data.getLast().add(rs.getString(name));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private boolean isRequestFromTable(HttpServletRequest req, String tableName) {
        return tableName.equals(req.getParameter("table"));
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
}
