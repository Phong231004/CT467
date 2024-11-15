package ui;

import database.JDBCUtil;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import database.JDBCUtil;  // Import JDBCUtil class
import javax.servlet.annotation.WebServlet;

@WebServlet("/quanly/XuatHoaDon")
public class XuatHoaDon extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // Get the phone number and date from the request
        String phoneNumber = request.getParameter("phone_number");
        String ngayXuat = request.getParameter("ngay_xuat");
        
        // Prepare the response
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        // Include the HTML form for user input
        request.getRequestDispatcher("/XuatHoaDon.html").include(request, response);

        try {
            // If phone number and date are provided, process the stored procedure
            if (phoneNumber != null && ngayXuat != null) {
                // Get the connection from JDBCUtil
                conn = JDBCUtil.getConnection();
                
                // Prepare the stored procedure call
                String sql = "{CALL XuatHoaDonTheoSoDienThoaiVaNgay(?, ?)}";
                stmt = conn.prepareCall(sql);
                
                // Set the parameters for the stored procedure
                stmt.setString(1, phoneNumber);
                stmt.setDate(2, Date.valueOf(ngayXuat));
                
                // Execute the stored procedure
                rs = stmt.executeQuery();
                
                // Start of result HTML
                StringBuilder resultHtml = new StringBuilder();
                resultHtml.append("<h2 style=\"font-family: 'K2D', sans-serif;\">Thông tin hóa đơn</h2>");
                
                // Display customer details in separate divs with minimal space
                if (rs.next()) {
                    resultHtml.append("<div style=\"font-family: 'K2D', sans-serif; font-size: 18px; margin-bottom: 5px;\">");
                    resultHtml.append("<strong>Họ và tên khách hàng:</strong> ").append(rs.getString("HoTenKhachHang")).append("</div>");
                    resultHtml.append("<div style=\"font-family: 'K2D', sans-serif; font-size: 18px; margin-bottom: 5px;\">");
                    resultHtml.append("<strong>Số điện thoại:</strong> ").append(rs.getString("SoDienThoaiKhachHang")).append("</div>");
                    resultHtml.append("<div style=\"font-family: 'K2D', sans-serif; font-size: 18px; margin-bottom: 5px;\">");
                    resultHtml.append("<strong>Họ tên nhân viên:</strong> ").append(rs.getString("HoTenNhanVien")).append("</div>");
                    resultHtml.append("<div style=\"font-family: 'K2D', sans-serif; font-size: 18px; margin-bottom: 5px;\">");
                    resultHtml.append("<strong>Mã phiếu xuất (ID):</strong> ").append(rs.getInt("MaPhieuXuat")).append("</div>");
                }
                
                // Table with single border around it and no inner borders
                resultHtml.append("<table style='border-collapse: collapse; width: 95%; margin: 0 auto;'>");
                resultHtml.append("<tr style='border-bottom: 2px solid #000; text-align: center;'><th>STT</th><th>Mã thiết bị</th><th>Tên thiết bị</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>");
                
                // Initialize the total variable
                double totalAmount = 0.0;
                int stt = 1;  // Serial number (STT)
                
                // Process the result set for devices (loop through the result set again for each device)
                do {
                    double thanhTien = rs.getDouble("ThanhTien");
                    totalAmount += thanhTien;  // Add to totalAmount
                    
                    resultHtml.append("<tr>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(stt).append("</td>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(rs.getInt("MaThietBi")).append("</td>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(rs.getString("TenThietBi")).append("</td>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(rs.getInt("SoLuong")).append("</td>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(rs.getDouble("DonGia")).append(" vnđ</td>");
                    resultHtml.append("<td style='border-bottom: 1px solid #ddd; text-align: center;'>").append(thanhTien).append(" vnđ</td>");
                    resultHtml.append("</tr>");
                    stt++;  // Increment STT
                } while (rs.next());  // Loop through all rows

                // Append the total row
                resultHtml.append("<tr><td colspan='5' style='text-align:right;'><strong>Tổng tiền:</strong></td>");
                resultHtml.append("<td><strong>").append(totalAmount).append(" vnđ</strong></td></tr>");
                
                resultHtml.append("</table>");
                
                // Send the result to be inserted into the div
                out.println("<script>");
                out.println("document.getElementById('invoiceResults').innerHTML = `" + resultHtml.toString() + "`;");
                out.println("</script>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close resources using JDBCUtil.closeConnection
            JDBCUtil.closeConnection(conn);
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

