package database;
import java.sql.*;

import utils.PasswordUtils;

public class RegisterUser {
    
    public static boolean isUsernameExists(String username) {
        // Hàm kiểm tra username đã tồn tại trong cơ sở dữ liệu hay chưa
        try (Connection connection = JDBCUtil.getConnection()) {
            String query = "SELECT COUNT(*) FROM Users WHERE username = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean register(String username, String password, String email, String phone) {
        if (isUsernameExists(username)) {
            System.out.println("Username already exists.");
            return false;
        }

        try (Connection connection = JDBCUtil.getConnection()) {
            String query = "INSERT INTO Users (username, password, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
//            stmt.setString(2, password); // Lưu mật khẩu (bạn có thể mã hóa mật khẩu ở đây)
            String encodedPassword = PasswordUtils.encodePassword(password);
            stmt.setString(2, encodedPassword);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("User registered successfully!");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        // Giả sử đây là dữ liệu từ form
        String username = "user123";
        String password = "password123";
        String confirmPassword = "password123";
        String email = "user@example.com";
        String phone = "0123456789";
        
        // Kiểm tra mật khẩu và xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }
        
        // Gọi hàm đăng ký
        boolean success = register(username, password, email, phone);
        if (success) {
            System.out.println("Registration complete!");
        } else {
            System.out.println("Registration failed.");
        }
    }
}
