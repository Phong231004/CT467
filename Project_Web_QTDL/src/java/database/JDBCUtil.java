/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.*;
import com.mysql.cj.jdbc.Driver;
import java.util.logging.Level;
import java.util.logging.Logger;
public class JDBCUtil {
    public static Connection getConnection(){
        Connection c = null;
        try {
            // DK mysql voiws DriverManager
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            //Cac thong so
            String url = "jdbc:mySQL://localhost:3306/ComputerStore";
            String username ="root";
            String password ="ThanhPhong@123";
            //Tao ket noi
            c = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(JDBCUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return c;
    }
    public static void closeConnection(Connection c){
        try{
            if(c!=null){
                c.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
