package bench;

import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class Populate {

    private static final String URL = "jdbc:postgresql://localhost/invoices";
    private static final String USERNAME = "miguel";
    private static final String PASSWORD = "";
    public  static final int    CLIENTS  = 1024;
    public  static final int    PRODUCTS = 4096;

    private static Connection db;

    public static void createTables() {

        StringBuilder query = new StringBuilder();
        PreparedStatement ps;

        try {
            /* Create client table */
            query.append("CREATE TABLE client(");
            query.append("id integer NOT NULL,");
            query.append("client_name varchar(32) NOT NULL,");
            query.append("client_addr text NOT NULL);");

            ps = db.prepareStatement(query.toString());
            ps.executeUpdate();

            /* Create product table */
            query.setLength(0);
            query.append("CREATE TABLE product(");
            query.append("id integer NOT NULL,");
            query.append("stock integer NOT NULL,");
            query.append("min integer NOT NULL,max integer NOT NULL,");
            query.append("p_desc varchar(32) NOT NULL);");

            ps = db.prepareStatement(query.toString());
            ps.executeUpdate();

            /* Create invoice table */
            query.setLength(0);
            query.append("CREATE TABLE invoice(");
            query.append("id integer NOT NULL,");
            query.append("client_id integer NOT NULL);");

            ps = db.prepareStatement(query.toString());
            ps.executeUpdate();

            /* Create invoiceLine table */
            query.setLength(0);
            query.append("CREATE TABLE invoiceLine(");
            query.append("id integer NOT NULL,");
            query.append("invoice_id integer NOT NULL,");
            query.append("product_id integer NOT NULL);");

            ps = db.prepareStatement(query.toString());
            ps.executeUpdate();

            /* Create order table */
            query.setLength(0);
            query.append("CREATE TABLE orderT(");
            query.append("id integer NOT NULL,");
            query.append("supplier integer NOT NULL,");
            query.append("items integer NOT NULL,");
            query.append("product_id integer NOT NULL);");

            ps = db.prepareStatement(query.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error creating tables");
        }

    }

    public static void insertClients() {

        StringBuilder query = new StringBuilder();
        for(int i = 0; i < CLIENTS; i++) {
            PreparedStatement ps;

            query.setLength(0);
            query.append("INSERT INTO client");
            query.append("(id, client_name, client_addr)");
            query.append("VALUES(").append(i).append(",");
            query.append("'client").append(i);
            query.append("','address").append(i);
            query.append("');");

            try {
                ps = db.prepareStatement(query.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error: failed to add client");
                System.exit(3);
            }
        }

    }

    public static void insertProducts() {

        StringBuilder query = new StringBuilder();
        Random rand = new Random();

        for(int i = 0; i < PRODUCTS; i++) {
            PreparedStatement ps;
            int min = rand.nextInt(512);
            int max = rand.nextInt(1024) + min;
            int stock = rand.nextInt((max - min) + 1) + min;

            query.setLength(0);
            query.append("INSERT INTO product");
            query.append("(id, stock, min, max, p_desc) VALUES");
            query.append("(").append(i).append(",");
            query.append(stock).append(",").append(min);
            query.append(",").append(max).append(",");
            query.append("'product").append(i).append("');");

            try {
                ps = db.prepareStatement(query.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error: failed to add product");
                System.exit(4);
            }
        }

    }
    
    public static void main(String[] args) {

        /* Initialize connection */
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error: unable to connect to db");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println("Error: unable to load driver class!");
            System.exit(2);
        }

        /* Initialize and populate tables */
        createTables();
        insertClients();
        insertProducts();

        /* Close connection */
        try {
            db.close();
        } catch (SQLException e) {
            System.out.println("Error: unable to close connection");
        }
    }

}



/*


Comandos para postgres:

--- *Terminal 1*
--- initdb -D dados
--- postgres -D dados/ -k.

--- *Terminal 2*
--- createdb -h localhost invoices
--- psql -h localhost invoices

SE ESTIVER A CORRER -p 12345


*/

/*
	statements alternativas
	NAO RECOMENDADO

	"insert into invoices values("+ no +")"
    Vulneravel a sql injections

    RECOMENDADO

    ps.setInt(1, x);
    ps.setDouble(2, y);---

    Link para obter generated keys:
    https://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc

    Directoria dos logs transacionais e de erro etc

    dados/
        pg_xlog/ -> WAL log
        pg_log/ -> msgs...
   ____________________
   postgresql.conf
    log_checkpoints = on
        aparece no log me mensagens informaçao
        sobre os checkpoints

   mudanças para checkpoint_timeout
   default: 5min
   testes: 30s

*/