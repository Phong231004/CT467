/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import database.JDBCUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@WebServlet("/quanly/Delete")
public class DeleData extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            request.getRequestDispatcher("/DeleteData.html").include(request, response);

            // Retrieve parameters from request
            String operation = request.getParameter("operation");
            String tableName = request.getParameter("tableName");
            String columns = request.getParameter("columns");
            String values = request.getParameter("values");
            String updates = request.getParameter("updates");
            String conditions = request.getParameter("conditions");

            try (Connection conn = JDBCUtil.getConnection()) {
                if (conn == null) {
                    out.println("<p>Database connection failed.</p>");
                    return;
                }

                out.println("<div style='margin-top: 20px;'>");

                switch (operation) {
                    case "insert":
                        performInsert(conn, out, tableName, columns, values);
                        break;
                    case "update":
                        performUpdate(conn, out, tableName, updates, conditions);
                        break;
                    case "delete":
                        performDelete(conn, out, tableName, conditions);
                        break;
                    default:
                        out.println("<p>Invalid operation. Use 'insert', 'update', or 'delete'.</p>");
                        break;
                }

                out.println("</div>");  // End of result area
            } catch (SQLException e) {
                out.println("<p>Error: " + e.getMessage() + "</p>");
            }
        }
    }

    private void performInsert(Connection conn, PrintWriter out, String tableName, String columns, String values) throws SQLException {

        String sql = "{CALL generic_insert(?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columns);
            stmt.setString(3, values);
            stmt.executeUpdate();
            out.println("<div id='Notification'>");
            out.println("<p style='color: green;'>Insert successful into " + tableName + ".</p>");
            out.println("<h4>Updated " + tableName + " table:</h4>");
            displayTableContents(conn, out, tableName);
            // Close the resultset div
            out.println("</div>");
        }
    }

    private void performUpdate(Connection conn, PrintWriter out, String tableName, String updates, String conditions) throws SQLException {       
        String sql = "{CALL generic_update(?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, updates);
            stmt.setString(3, conditions);
            stmt.executeUpdate();
            out.println("<div id='Notification'>");
            out.println("<p style='color: green;'>Update successful on " + tableName + " where " + conditions + ".</p>");
            out.println("<h4>Updated " + tableName + " table:</h4>");
            displayTableContents(conn, out, tableName);
            // Close the resultset div
            out.println("</div>");
        }
    }

    private void performDelete(Connection conn, PrintWriter out, String tableName, String conditions) throws SQLException {
    // Check if conditions are provided
    if (conditions == null || conditions.trim().isEmpty()) {
        out.println("<p style='color: red;'>Conditions cannot be empty.</p>");
        return;
    }

    String sql = "{CALL generic_delete(?, ?)}";
    try (CallableStatement stmt = conn.prepareCall(sql)) {
        stmt.setString(1, tableName);
        stmt.setString(2, conditions);  // Make sure conditions are not null or empty
        stmt.executeUpdate();
        
        out.println("<div id='Notification'>");
        out.println("<p style='color: green;'>Delete successful on " + tableName + " where " + conditions + ".</p>");
        out.println("<h4>Updated " + tableName + " table:</h4>");
        displayTableContents(conn, out, tableName);
        // Close the resultset div
        out.println("</div>");
    } catch (SQLException e) {
        out.println("<p>Error: " + e.getMessage() + "</p>");
    }
}
    private void displayTableContents(Connection conn, PrintWriter out, String tableName) throws SQLException {
    String query = "SELECT * FROM " + tableName;
    try (ResultSet rs = conn.createStatement().executeQuery(query)) {
        // Start the result container div with id="resultset"
        out.println("<div id='resultset'>");

        // Create the table
        out.println("<table border='1' cellpadding='5' style='border-collapse: collapse;'>");
        out.println("<tr style='background-color: #007bff; color: white;'>");

        // Print column headers
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            out.println("<th>" + rs.getMetaData().getColumnName(i) + "</th>");
        }
        out.println("</tr>");

        // Print rows
        while (rs.next()) {
            out.println("<tr>");
            for (int i = 1; i <= columnCount; i++) {
                out.println("<td>" + rs.getString(i) + "</td>");
            }
            out.println("</tr>");
        }
        out.println("</table>");

        // Close the resultset div
        out.println("</div>");
    }
} 
}
