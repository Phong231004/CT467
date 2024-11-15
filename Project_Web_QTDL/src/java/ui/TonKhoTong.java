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
import java.util.List;

@WebServlet("/quanly/TonKho")
public class TonKhoTong extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = JDBCUtil.getConnection();

        List<Integer> MaThietBi = new ArrayList<>();
        List<String> TenThietBi = new ArrayList<>();
        List<Double> SoLuongNhap = new ArrayList<>();
        List<Double> SoLuongXuat = new ArrayList<>();
        List<Double> SoLuongTon = new ArrayList<>();

        try (PrintWriter out = response.getWriter()) {
            // Đọc tệp HTML
            String htmlFilePath = getServletContext().getRealPath("/QuanLyTonKho.html");
            String htmlContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(htmlFilePath)));

            if (conn == null) {
                out.println("Có lỗi trong việc kết nối đến cơ sở dữ liệu!");
                return;
            }

            try (CallableStatement cstmt = conn.prepareCall("{CALL GetTonKho()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    int MTB = rs.getInt("MaThietBi");
                    String TTB = rs.getString("TenThietBi");
                    double SLN = rs.getDouble("SoLuongNhap");
                    double SLX = rs.getDouble("SoLuongXuat");
                    double SLT = rs.getDouble("SoLuongTon");

                    // Thêm dữ liệu vào các danh sách
                    MaThietBi.add(MTB);
                    TenThietBi.add(TTB);
                    SoLuongNhap.add(SLN);
                    SoLuongXuat.add(SLX);
                    SoLuongTon.add(SLT);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Có lỗi khi lấy dữ liệu từ cơ sở dữ liệu: " + e.getMessage());
            }

            // Chuyển đổi danh sách thành JSON format cho Handsontable sử dụng
            StringBuilder jsData = new StringBuilder();
            jsData.append("const data = [\n");

            for (int i = 0; i < MaThietBi.size(); i++) {
                jsData.append("[")
                        .append(MaThietBi.get(i)).append(", ")
                        .append("\"").append(TenThietBi.get(i)).append("\"").append(", ")
                        .append(SoLuongNhap.get(i)).append(", ")
                        .append(SoLuongXuat.get(i)).append(", ")
                        .append(SoLuongTon.get(i))
                        .append("],\n");
            }

            // Loại bỏ dấu phẩy cuối cùng
            if (jsData.length() > 2) {
                jsData.setLength(jsData.length() - 2);
            }
            jsData.append("];\n");

            // Chèn dữ liệu vào HTML
            htmlContent = htmlContent.replace("// Dữ liệu sẽ được chèn vào đây bởi Servlet", jsData.toString());

            out.println(htmlContent);

        } catch (IOException e) {
            e.printStackTrace();
            response.getWriter().println("Có lỗi trong quá trình đọc tệp HTML!");
        } finally {
            JDBCUtil.closeConnection(conn);
        }
    }
}
