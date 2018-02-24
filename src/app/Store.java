package app;

import bench.WorkloadGen;
import com.sun.xml.internal.ws.api.pipe.FiberContextSwitchInterceptor;
import javafx.concurrent.WorkerStateEvent;

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
            db.setAutoCommit(false);
            // manual commit to control transactions
        } catch (ClassNotFoundException|SQLException e) {

        }
    }

    public void closeConnection() {
        try {
            db.close();
        } catch (SQLException e) {

        }
    }

    public void sell(List<Integer> productIds, int clientId) {
        StringBuilder query = new StringBuilder();
        PreparedStatement ps = null;
        int invoiceId = WorkloadGen.getInvoiceId();

        query.append("INSERT INTO invoice");
        query.append("(id, client_id)");
        query.append("VALUES(?,?);");
        /* Falta decrementar o stock */
        try {
            ps = db.prepareStatement(query.toString());
            ps.setInt(1, clientId);
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
            for(Integer product : productIds) {
                int invoiceLineId = WorkloadGen.getInvoiceLineId();
                query.setLength(0);
                query.append("INSERT INTO invoiceLine");
                query.append("(id, invoice_id, product_id)");
                query.append("VALUES(?,?,?);");
                ps = db.prepareStatement(query.toString());
                ps.setInt(1, invoiceLineId);
                ps.setInt(2, invoiceId);
                ps.setInt(3, product);
                ps.executeUpdate();
            }
            query.setLength(0);
            db.commit();
        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                db.rollback();
                WorkloadGen.newRollback();
            } catch (SQLException ex) {
                //There's nothing to do about it
                ex.printStackTrace();
            }
        }
    }


    public List<String> productsSold(int clientId) {
        StringBuilder query = new StringBuilder();
        List<String> products = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        query.append("SELECT P.id,P.p_desc FROM invoiceLine AS IL ");
        query.append("INNER JOIN product AS P ON P.id = IL.product_id");
        query.append("INNER JOIN invoice AS I ON IL.invoice_id = I.id");
        query.append("WHERE I.client_id=?;");

        try {
            ps = db.prepareStatement(query.toString());
            ps.setInt(1, clientId);
            rs = ps.executeQuery();
            db.commit();
            while(rs.next()) {
                String prodDesc;
                int prodId;
                prodDesc = rs.getString("p_desc");
                prodId   = rs.getInt("id");
                products.add("Id: " + prodId + " Desc: " + prodDesc);
            }
        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                db.rollback();
                WorkloadGen.newRollback();
            } catch (SQLException ex) {
                //There's nothing to do about it
                ex.printStackTrace();
            }
        }

        return products;
    }

    public List<String> topTenProducts() {
        StringBuilder query = new StringBuilder();
        List<String> products = new ArrayList<>();
        PreparedStatement ps; ResultSet rs;

        query.append("SELECT P.id,P.p_desc, count(*) AS total ");
        query.append("FROM invoiceLine AS IL ");
        query.append("INNER JOIN product AS P ON IL.product_id = P.id ");
        query.append("GROUP BY P.id,P.p_desc ORDER BY total DESC LIMIT 10;");

        try {
            ps = db.prepareStatement(query.toString());
            rs = ps.executeQuery();
            db.commit();
            while(rs.next()) {
                String prodDesc;
                Integer prodId;
                int quantity;

                prodDesc = rs.getString("p_desc");
                prodId   = rs.getInt("id");
                quantity = rs.getInt("total");

                products.add(prodId+","+prodDesc+","+quantity);
            }
        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                db.rollback();
                WorkloadGen.newRollback();
            } catch (SQLException ex) {
                //There's nothing to do about it
                ex.printStackTrace();
            }
        }
        return products;
    }

    public void placeOrder(int productId, int supplierId) {
        StringBuilder query = new StringBuilder();
        PreparedStatement ps; ResultSet rs;
        int orderId = WorkloadGen.getOrderId();

        query.append("INSERT INTO orderT(id,supplier,items,product_id) ");
        query.append("VALUES(?,?,?,?,);");
        String insertO = query.toString();
        String searchP = "SELECT stock, max FROM product where id=?;";
        String searchO = "SELECT id from orderT where product_id=?;";
        String updateO = "UPDATE orderT SET items=? WHERE id=?;";
        try {
            ps = db.prepareStatement(searchP);
            rs = ps.executeQuery();
            int stock = 0, max = 0;
            if(rs.next()) {
                stock = rs.getInt("stock");
                max = rs.getInt("max");
            }
            ps = db.prepareStatement(searchO);
            rs = ps.executeQuery();
            if(rs.next()) {
                int id = rs.getInt("id");
                ps = db.prepareStatement(updateO);
                ps.setInt(1, max - stock);
                ps.setInt(2, id);
                ps.executeUpdate();
            } else {
                ps = db.prepareStatement(insertO);
                ps.setInt(1, orderId);
                ps.setInt(2, supplierId);
                ps.setInt(3, max - stock);
                ps.setInt(4, productId);
                ps.executeUpdate();
            }
            db.commit();
        } catch(SQLException e) {
            try {
                System.out.println(e.getMessage());
                db.rollback();
                WorkloadGen.newRollback();
            } catch (SQLException ex) {
                //There's nothing to do about it
                ex.printStackTrace();
            }
        }
    }

    public void delivered(int supplierId) {
        PreparedStatement ps; ResultSet rs;

        String searchO = "SELECT product_id,items FROM orderT WHERE supplier=?;";
        String updateP = "UPDATE product SET stock=? WHERE id=?;";
        String deleteO = "DELETE FROM orderT WHERE supplier=?;";

        try {
            ps = db.prepareStatement(searchO);
            ps.setInt(1, supplierId);
            rs = ps.executeQuery();
            while(rs.next()) {
                int items = rs.getInt("items");
                int proid = rs.getInt("product_id");
                ps = db.prepareStatement(updateP);
                ps.setInt(1, items);
                ps.setInt(2, proid);
            }
            ps = db.prepareStatement(deleteO);
            ps.setInt(1, supplierId);
            ps.executeUpdate();
            db.commit();
        } catch (SQLException e) {
            try {
                System.out.println(e.getMessage());
                db.rollback();
                WorkloadGen.newRollback();
            } catch (SQLException ex) {
                //There's nothing to do about it
                ex.printStackTrace();
            }
        }

    }
}
