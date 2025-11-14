package org.example.angelbacked.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateUserRole {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/angel?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "20050202";
        
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            String sql = "UPDATE user SET role = '1' WHERE username = 'admin'";
            PreparedStatement statement = connection.prepareStatement(sql);
            int rowsAffected = statement.executeUpdate();
            System.out.println("更新了 " + rowsAffected + " 行数据");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}