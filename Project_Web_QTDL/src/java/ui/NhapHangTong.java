package ui;

import database.JDBCUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/quanly/NhapHangTong")
public class NhapHangTong extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = null;

        // Lists to hold revenue data
        List<Integer> month = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        List<String> TenNhaCungCap = new ArrayList<>();
        List<Double> TongTienNhap = new ArrayList<>();
        List<String> maTB = new ArrayList<>();
        List<String> tenThietBi = new ArrayList<>();
        List<Double> TongNhapHang = new ArrayList<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR); // Get current year

        try {
            conn = JDBCUtil.getConnection();
            if (conn == null) {
                throw new SQLException("Có lỗi trong việc kết nối đến cơ sở dữ liệu!");
            }

            // Read HTML file
            String htmlFilePath = getServletContext().getRealPath("/QuanLyTongNhapHang.html");
            String htmlContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(htmlFilePath)));

            // Retrieve monthly revenue data
            try (CallableStatement cstmt = conn.prepareCall("{CALL BangNhapTheoThangTrongNam(?)}")) {
                cstmt.setInt(1, currentYear);
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    month.add(rs.getInt("Thang"));
                    revenues.add(rs.getDouble("TongNhapHang"));
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu doanh thu theo tháng: " + e.getMessage(), e);
            }

            // Retrieve revenue for the last 12 months
            try (CallableStatement cstmt = conn.prepareCall("{CALL TinhTienNhapTheoNhaCungCap()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    TenNhaCungCap.add(rs.getString("TenNhaCungCap")); // Lấy tên nhà cung cấp
                    TongTienNhap.add(rs.getDouble("TongTienNhap")); // Tổng tiền nhập từ nhà cung cấp
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu doanh thu theo nhà cung cấp: " + e.getMessage(), e);
            }

            // Retrieve revenue by product
            try (CallableStatement cstmt = conn.prepareCall("{CALL TinhTongNhapHangTheoSanPham()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    maTB.add(rs.getString("MaThietBi")); 
                    tenThietBi.add(rs.getString("TenThietBi")); 
                    TongNhapHang.add(rs.getDouble("TongNhapHang"));
                }
            } catch (SQLException e) {
                throw new SQLException("Có lỗi khi lấy dữ liệu doanh thu theo sản phẩm: " + e.getMessage(), e);
            }

            // Insert data into HTML for JavaScript
            StringBuilder jsData = new StringBuilder();
            jsData.append("<script>\n")
                  .append("const month = ").append(month).append(";\n")
                  .append("const revenues = ").append(revenues).append(";\n")
                  .append("const TenNhaCungCap = ").append(TenNhaCungCap.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("const TongTienNhap = ").append(TongTienNhap).append(";\n")
                  .append("const maTB = ").append(maTB).append(";\n")
                  .append("const tenThietBi = ").append(tenThietBi.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"))).append(";\n")
                  .append("const TongNhapHang = ").append(TongNhapHang.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))).append(";\n")
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
