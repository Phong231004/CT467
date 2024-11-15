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
import java.util.stream.Collectors;

@WebServlet("/quanly/ThongKe")
public class ThongKeTongHopServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = JDBCUtil.getConnection();

        List<Integer> yearsDoanhThu = new ArrayList<>();
        List<Double> revenuesDoanhThu = new ArrayList<>();
        List<Integer> yearsNhapHang = new ArrayList<>();
        List<Double> revenuesNhapHang = new ArrayList<>();
        List<Integer> yearsLoiNhuan = new ArrayList<>();
        List<Double> revenuesLoiNhuan = new ArrayList<>();
        List<String> tenThietBi = new ArrayList<>();
        List<Double> doanhThu = new ArrayList<>();

        try (PrintWriter out = response.getWriter()) {
            // Đọc tệp HTML
            String htmlFilePath = getServletContext().getRealPath("/index.html");
            String htmlContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(htmlFilePath)));

            if (conn == null) {
                out.println("Có lỗi trong việc kết nối đến cơ sở dữ liệu!");
                return;
            }

            // Lấy dữ liệu doanh thu
            try (CallableStatement cstmt = conn.prepareCall("{CALL BangDoanhThuTheoNam()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    int year = rs.getInt("Nam");
                    double totalRevenue = rs.getDouble("TongDoanhThu");
                    yearsDoanhThu.add(year);
                    revenuesDoanhThu.add(totalRevenue);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Có lỗi khi lấy dữ liệu doanh thu: " + e.getMessage());
            }

            // Lấy dữ liệu nhập hàng
            try (CallableStatement cstmt = conn.prepareCall("{CALL BangNhapTheoNam()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    int year = rs.getInt("Nam");
                    double totalNhap = rs.getDouble("TongNhap");
                    yearsNhapHang.add(year);
                    revenuesNhapHang.add(totalNhap);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Có lỗi khi lấy dữ liệu nhập hàng: " + e.getMessage());
            }

            // Lấy dữ liệu lợi nhuận
            try (CallableStatement cstmt = conn.prepareCall("{CALL BangLoiNhuan()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    int year = rs.getInt("Nam");
                    double totalLoiNhuan = rs.getDouble("LoiNhuan");
                    yearsLoiNhuan.add(year);
                    revenuesLoiNhuan.add(totalLoiNhuan);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Có lỗi khi lấy dữ liệu lợi nhuận: " + e.getMessage());
            }

            // Tổng mỗi sản phẩm xuất
            try (CallableStatement cstmt = conn.prepareCall("{CALL ThongKeSanPhamDaXuat()}")) {
                ResultSet rs = cstmt.executeQuery();
                while (rs.next()) {
                    String tenThietBiItem = rs.getString("TenThietBi");
                    double doanhThuItem = rs.getDouble("TongXuat");
                    tenThietBi.add(tenThietBiItem);
                    doanhThu.add(doanhThuItem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("Có lỗi khi lấy dữ liệu sản phẩm đã xuất: " + e.getMessage());
            }

            // Chèn dữ liệu cho biểu đồ
            StringBuilder jsData = new StringBuilder();
            jsData.append("const yearsDoanhThu = ").append(yearsDoanhThu).append(";\n");
            jsData.append("const revenuesDoanhThu = ").append(revenuesDoanhThu).append(";\n");
            jsData.append("const yearsNhapHang = ").append(yearsNhapHang).append(";\n");
            jsData.append("const revenuesNhapHang = ").append(revenuesNhapHang).append(";\n");
            jsData.append("const yearsLoiNhuan = ").append(yearsLoiNhuan).append(";\n");
            jsData.append("const revenuesLoiNhuan = ").append(revenuesLoiNhuan).append(";\n");
            
            // Chuyển đổi danh sách thành định dạng JSON
            String tenThietBiJson = tenThietBi.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"));
            String doanhThuJson = doanhThu.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));

            jsData.append("const tenThietBi = ").append(tenThietBiJson).append(";\n");
            jsData.append("const doanhThu = ").append(doanhThuJson).append(";\n");

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
