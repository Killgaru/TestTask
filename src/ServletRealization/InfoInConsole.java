package ServletRealization;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Enumeration;

public class InfoInConsole {
    public static void printParameters(HttpServletRequest req) {
        printParameters(req, "");
    }

    public static void printParameters(HttpServletRequest req, String title) {
        System.out.println("\n******* " + title + " *******");
        Enumeration<String> names = req.getParameterNames();
        while (names.hasMoreElements()) {
            String elementName = names.nextElement();
            System.out.println(elementName + " = "
                    + Arrays.toString(req.getParameterValues(elementName)) + "\t getParameter: "
                    + req.getParameter(elementName));
        }
        System.out.println("==============");
    }
}
