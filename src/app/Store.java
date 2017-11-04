package app;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Store {

    private static final String URL = "jdbc:postgresql://localhost/invoices";
    private static final String USERNAME = "miguel";
    private static final String PASSWORD = "";

    private Connection db;

    public Store() {

        /*
        *   Since this is used for performance testing,
        *   there is no need to open and close connection
        *   for each query because in this case we know
        *   the connection won't timeout.
        */
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException|SQLException e) {

        }
    }

    public void closeConnection() {
        try {
            db.close();
        } catch (SQLException e) {

        }
    }

    public void sell(int productId, int clientId) {
        StringBuilder query = new StringBuilder();
        PreparedStatement ps = null;

        query.append("INSERT INTO invoice");
        query.append("(client_id, product_id)");
        query.append("VALUES(?,?);");

        try {
            ps = db.prepareStatement(query.toString());
            ps.setInt(1, clientId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            //There's nothing to do about it
            //So we throw unchecked exception
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    public List<String> productsSold(int clientId) {
        StringBuilder query = new StringBuilder();
        List<String> products = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        query.append("SELECT product.product_id, product_desc ");
        query.append("FROM invoice JOIN product ");
        query.append("ON invoice.product_id=product.product_id ");
        query.append("WHERE invoice.client_id=?;");
        try {
            ps = db.prepareStatement(query.toString());
            ps.setInt(1, clientId);
            rs = ps.executeQuery();

            while(rs.next()) {
                String prodDesc;
                int prodId;
                prodDesc = rs.getString("product_desc");
                prodId   = rs.getInt("product_id");
                products.add("Id: " + prodId + " Desc: " + prodDesc);
            }
        } catch (SQLException e) {
            //There's nothing to do about it
            //So we throw unchecked exception
            e.printStackTrace();
            throw new RuntimeException();
        }

        return products;
    }

    public List<String> topTenProducts() {
        StringBuilder query = new StringBuilder();
        List<String> products = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        query.append("SELECT product_id, count(*) AS total ");
        query.append("FROM invoice ");
        query.append("GROUP BY product_id ");
        query.append("ORDER BY total DESC LIMIT 10;");

        try {
            ps = db.prepareStatement(query.toString());
            rs = ps.executeQuery();

            while(rs.next()) {
                Integer prodId;
                int quantity;

                prodId   = rs.getInt("product_id");
                quantity = rs.getInt("total");

                products.add(" Quantity: " + quantity + " Desc: " + prodId);
            }
        } catch (SQLException e) {
            //There's nothing to do about it
            //So we throw unchecked exception
            e.printStackTrace();
            throw new RuntimeException();
        }

        return products;
    }
}
