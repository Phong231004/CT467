package database;

import java.sql.*;
import utils.PasswordUtils; // Nhớ import PasswordUtils

public class LoginUser {
    public static boolean validateLogin(String username, String password) {
        try (Connection connection = JDBCUtil.getConnection()) {
            String query = "SELECT password FROM Users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                // Sử dụng PasswordUtils để kiểm tra mật khẩu đã mã hóa
                if (PasswordUtils.checkPassword(password, storedPassword)) {
                    return true; // Đăng nhập thành công
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Đăng nhập thất bại
    }

    public static void main(String[] args) {
        // Giả sử đây là dữ liệu từ forms
        String username = "user123";
        String password = "password123";

        boolean success = validateLogin(username, password);
        if (success) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed. Please check your username and password.");
        }
    }
}
