package ui;

import database.JDBCUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

@WebServlet("/quanly/HienThiThietBiServlet")
public class HienThiThietBiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String searchText = request.getParameter("deviceName");
        String sortOption = request.getParameter("sortOption");
        String producerName = request.getParameter("producerName");

        // Output HTML content and form
        request.getRequestDispatcher("/HienThiThietBi.html").include(request, response);

        try {
            out.println("<div id='resultArea'>");  // Start result area
            if (action != null) {
                switch (action) {
                    case "search":
                        searchDevice(out, searchText);
                        break;
                    case "sort":
                        sortDevices(out, sortOption);
                        break;
                    case "filter":
                        filterByProducer(out, producerName);
                        break;
                    default:
                        displayAllDevices(out);
                        break;
                }
            } else {
                displayAllDevices(out);
            }
            out.println("</div>");  // End result area
        } finally {
            out.close();
        }
    }

    private void displayAllDevices(PrintWriter out) {
        try (Connection conn = JDBCUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ThietBi")) {

            out.println("<h3 style=\"margin-left: 450px; font-size: 24px;\">Danh sách tất cả thiết bị</h3>");
            out.println("<table border='1' width='94%' style='margin-left: 30px; margin-right: 30px;'>");
            out.println("<tr style='background-color: #007bff; color: white;'><th>ID</th><th>Tên</th><th>Giá</th></tr>");

            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("MaThietBi") + "</td>");
                out.println("<td>" + rs.getString("TenThietBi") + "</td>");
                out.println("<td>" + rs.getDouble("GiaThanh") + " vnđ" + "</td>");
                out.println("</tr>");
            }

            out.println("</table>");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchDevice(PrintWriter out, String deviceName) {
        try (Connection conn = JDBCUtil.getConnection()) {
            String sql = "CALL TimThietBiTheoTen(?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceName);

            ResultSet rs = stmt.executeQuery();
            // In tiêu đề "Kết quả tìm kiếm" với định dạng như yêu cầu
            out.println("<h3 style=\"margin-left: 450px; font-size: 24px;\">Kết quả tìm kiếm</h3>");

            // Tạo bảng với viền và các cột phù hợp
            out.println("<table border='1' width='94%' style='margin-left: 30px; margin-right: 30px;'>");
            out.println("<tr style='background-color: #007bff; color: white;'><th>ID</th><th>Tên</th><th>Thông số</th><th>Giá</th><th>Số lượng tồn kho</th></tr>");

            // Duyệt qua ResultSet và in ra từng hàng của bảng
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("MaThietBi") + "</td>");
                out.println("<td>" + rs.getString("TenThietBi") + "</td>");
                out.println("<td>" + rs.getString("ThongSoKT") + "</td>");
                out.println("<td>" + rs.getDouble("GiaThanh") + "</td>");
                out.println("<td>" + rs.getDouble("SoLuong") + "</td>");
                out.println("</tr>");
            }

            // Đóng thẻ bảng
            out.println("</table>");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void sortDevices(PrintWriter out, String sortCriteria) {
        String query = "";
        switch (sortCriteria) {
            case "Giá tăng dần":
                query = "SELECT * FROM ThietBiGiaTangDan";
                break;
            case "Giá giảm dần":
                query = "SELECT * FROM ThietBiGiaGiamDan";
                break;
            case "Bán chạy nhất":
                query = "SELECT * FROM ThietBiBanChayNhat";
                break;
        }

        try (Connection conn = JDBCUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // In tiêu đề "Danh sách thiết bị" với tiêu chí sắp xếp và định dạng
            out.println("<h3 style=\"margin-left: 450px; font-size: 24px;\">Danh sách thiết bị (" + sortCriteria + "):</h3>");

            // Tạo bảng với viền và các cột phù hợp
            out.println("<table border='1' width='94%' style='margin-left: 30px; margin-right: 30px;'>");
            out.println("<tr style='background-color: #007bff; color: white;'><th>ID</th><th>Tên</th><th>Giá</th></tr>");

            // Duyệt qua ResultSet và in ra từng hàng của bảng
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("MaThietBi") + "</td>");
                out.println("<td>" + rs.getString("TenThietBi") + "</td>");
                out.println("<td>" + rs.getDouble("GiaThanh") + "</td>");
                out.println("</tr>");
            }

            // Đóng thẻ bảng
            out.println("</table>");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void filterByProducer(PrintWriter out, String producerName) {
        try (Connection conn = JDBCUtil.getConnection()) {
            String getProducerIdQuery = "SELECT MaNSX FROM NhaSanXuat WHERE TenNSX = ?";
            PreparedStatement producerStmt = conn.prepareStatement(getProducerIdQuery);
            producerStmt.setString(1, producerName);
            ResultSet rsProducer = producerStmt.executeQuery();

            if (rsProducer.next()) {
                int producerId = rsProducer.getInt("MaNSX");

                String sql = "CALL LocThietBiTheoNSX(?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, producerId);

                ResultSet rs = stmt.executeQuery();
                                // In tiêu đề "Kết quả lọc theo nhà sản xuất" với tên nhà sản xuất và định dạng
                out.println("<h3 style=\"margin-left: 420px; font-size: 24px;\">Kết quả lọc theo nhà sản xuất " + producerName + ":</h3>");

                // Tạo bảng với viền và các cột phù hợp
                out.println("<table border='1' width='94%' style='margin-left: 30px; margin-right: 30px;'>");
                out.println("<tr style='background-color: #007bff; color: white;'><th>ID</th><th>Tên</th><th>Thông số</th><th>Giá</th><th>Số lượng</th></tr>");

                // Duyệt qua ResultSet và in ra từng hàng của bảng
                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("MaThietBi") + "</td>");
                    out.println("<td>" + rs.getString("TenThietBi") + "</td>");
                    out.println("<td>" + rs.getString("ThongSoKT") + "</td>");
                    out.println("<td>" + rs.getDouble("GiaThanh") + "</td>");
                    out.println("<td>" + rs.getInt("SoLuong") + "</td>");
                    out.println("</tr>");
                }

                // Đóng thẻ bảng
                out.println("</table>");

            } else {
                out.println("<p>Không tìm thấy nhà sản xuất với tên" + producerName + "</p>");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
