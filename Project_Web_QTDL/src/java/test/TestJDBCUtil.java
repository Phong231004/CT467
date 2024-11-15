package test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import database.JDBCUtil;
import database.JDBCUtil;

public class TestJDBCUtil {

	public static void main(String[] args) {
		try {
			//Buoc 1 
			Connection connection = JDBCUtil.getConnection();
			
			//Buoc 2: Tao ra doi tuong statement
			Statement st = connection.createStatement();
			
			if(st!=null){
                            System.out.println("Ket noi CSDL thanh cong");
                        }
			JDBCUtil.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
