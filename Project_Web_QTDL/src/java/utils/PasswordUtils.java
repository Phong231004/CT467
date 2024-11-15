package utils;

public class PasswordUtils {

    // Mã hóa mật khẩu bằng cách xoay vòng các ký tự và trộn chuỗi
    public static String encodePassword(String password) {
        StringBuilder encoded = new StringBuilder();
        
        // Vòng lặp từng ký tự trong mật khẩu
        for (int i = 0; i < password.length(); i++) {
            // Thay đổi ký tự bằng cách cộng thêm một giá trị (ví dụ: 3) vào mã ASCII
            char ch = (char) (password.charAt(i) + 3);
            encoded.append(ch);
        }

        // Đảo chuỗi đã được mã hóa đơn giản
        return encoded.reverse().toString();
    }

    // Hàm giải mã mật khẩu
    public static String decodePassword(String encodedPassword) {
        StringBuilder decoded = new StringBuilder(encodedPassword).reverse();

        // Giảm giá trị của mỗi ký tự để quay lại mật khẩu gốc
        for (int i = 0; i < decoded.length(); i++) {
            char ch = (char) (decoded.charAt(i) - 3);
            decoded.setCharAt(i, ch);
        }

        return decoded.toString();
    }

    // Kiểm tra mật khẩu đã mã hóa với mật khẩu đầu vào
    public static boolean checkPassword(String inputPassword, String encodedPassword) {
        String encodedInput = encodePassword(inputPassword);
        return encodedInput.equals(encodedPassword);
    }

    public static void main(String[] args) {
        String password = "password123";
        String encodedPassword = encodePassword(password);
        System.out.println("Encoded Password: " + encodedPassword);

        // Kiểm tra giải mã
        String decodedPassword = decodePassword(encodedPassword);
        System.out.println("Decoded Password: " + decodedPassword);

        // Kiểm tra mật khẩu đầu vào
        boolean isMatch = checkPassword(password, encodedPassword);
        System.out.println("Password Match: " + isMatch);
    }

    public static String hashPassword(String password) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}