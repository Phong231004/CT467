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
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/quanly/QuanLyTong")
public class QuanLyTong extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        // Lists to hold revenue data
        List<Integer> monthDoanhThuTheoThang = new ArrayList<>();
        List<Double> revenuesDoanhThuTheoThang = new ArrayList<>();
        List<String> tenTinh = new ArrayList<>();
        List<Integer> soLuongKhachHang = new ArrayList<>();
        List<String> maTB = new ArrayList<>();
        List<String> tenThietBi = new ArrayList<>();
        List<Double> doanhThu = new ArrayList<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR); // Get current year

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) {
                throw new SQLException("Có lỗi trong việc kết nối đến cơ sở dữ liệu!");
            }

            // Read HTML file
            String htmlFilePath = getServletContext().getRealPath("/QuanLyTongDoanhThu.html");
            String htmlContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(htmlFilePath)));

            // Retrieve monthly revenue data
            try (CallableStatement cstmt = conn.prepareCall("{CALL BangDoanhThuTheoThangTrongNam()}")) {
                rs = cstmt.executeQuery();
                while (rs.next()) {
                    monthDoanhThuTheoThang.add(rs.getInt("Thang"));
                    revenuesDoanhThuTheoThang.add(rs.getDouble("TongDoanhThu"));
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu doanh thu theo tháng: " + e.getMessage(), e);
            }

            // Retrieve customer data by province from the view
            try {
                stmt = conn.createStatement();
                String sql = "SELECT * FROM SoLuongKhachHangTheoTinh";
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    tenTinh.add(rs.getString("Tinh"));
                    soLuongKhachHang.add(rs.getInt("SoLuongKhachHang"));
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu khách hàng theo tỉnh: " + e.getMessage(), e);
            }

            // Retrieve revenue by product
            try (CallableStatement cstmt = conn.prepareCall("{CALL TinhTongDoanhThuTheoSanPham()}")) {
                rs = cstmt.executeQuery();
                while (rs.next()) {
                    maTB.add(rs.getString("MaThietBi"));
                    tenThietBi.add(rs.getString("TenThietBi"));
                    doanhThu.add(rs.getDouble("TongDoanhThu"));
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu doanh thu theo sản phẩm: " + e.getMessage(), e);
            }

            // Insert data into HTML for JavaScript
            StringBuilder jsData = new StringBuilder();
            jsData.append("<script>\n")
                  .append("const monthDoanhThuTheoThang = ").append(monthDoanhThuTheoThang).append(";\n")
                  .append("const revenuesDoanhThuTheoThang = ").append(revenuesDoanhThuTheoThang).append(";\n")
                  .append("const tenTinh = ").append(tenTinh.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("const soLuongKhachHang = ").append(soLuongKhachHang.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("const tenThietBi = ").append(tenThietBi.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("const doanhThu = ").append(doanhThu.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("</script>\n");

            // Replace placeholder in HTML with the generated JavaScript data
            htmlContent = htmlContent.replace("<!-- Dữ liệu sẽ được chèn vào đây bởi Servlet -->", jsData.toString());

            // Write the final HTML content to the response
            try (PrintWriter out = response.getWriter()) {
                out.println(htmlContent);
            }
        } catch (IOException e) {
            throw new ServletException("Có lỗi trong quá trình đọc tệp HTML!", e);
        } catch (SQLException e) {
            e.printStackTrace(); // Consider logging this instead
            response.getWriter().println("Có lỗi xảy ra trong quá trình xử lý yêu cầu. Vui lòng thử lại sau.");
        } finally {
            JDBCUtil.closeConnection(conn);
        }
    }
}
