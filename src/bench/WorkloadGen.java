package bench;


import app.Store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class WorkloadGen extends Thread {
    private static final String URL = "jdbc:postgresql://localhost/invoices";
    private static final String USERNAME = "miguel";
    private static final String PASSWORD = "";
    private static long ultima = -1;
    private static long iaa = 0, tra = 0, c = 0;
    private static boolean start = false;


    public boolean running = true;

    private static synchronized void regista(long antes, long depois) {


        long tr = depois-antes;

        long anterior = ultima;
        ultima = depois;
        if (anterior < 0 || !start)
            return;

        long ia = depois - anterior;
        iaa += ia;
        tra += tr;
        c++;
    }

    public static synchronized void imprime() {
        double trm = (tra/1e9d)/c;
        double debito = 1/((iaa/1e9d)/c);

        System.out.println("debito = "+debito+" tps, tr = "+trm+" s");

    }

    public static synchronized void partida() {
        start = true;
    }



    @Override
    public void run() {
        Store store = new Store();
        Random rand = new Random();
        int productId, clientId, query;


        while(running) {
            productId = rand.nextInt(Populate.PRODUCTS)|
                        rand.nextInt(Populate.PRODUCTS);
            clientId  = rand.nextInt(Populate.CLIENTS)|
                        rand.nextInt(Populate.CLIENTS);

            query = rand.nextInt(3);

            long antes = System.nanoTime();
            if(query == 0)
                store.sell(productId, clientId);
            else if(query == 1)
                store.productsSold(clientId);
            else
                store.topTenProducts();

            long depois = System.nanoTime();
            regista(antes, depois);

        }
    }

    public static void clearInvoices() {
        Connection db;
        String query;
        PreparedStatement ps;
        /*  Load driver class */
        try {
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            query = "DELETE FROM invoice where invoice_id>=0;";
            ps = db.prepareStatement(query);
            ps.executeUpdate();
            db.close();
        } catch (ClassNotFoundException|SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        int max_threads = 32;
        int threads, current;
        WorkloadGen[] workers = new WorkloadGen[max_threads];

        for(threads = 1; threads <= max_threads; threads *= 2) {
            current = 0;
            while (current < threads) {
                workers[current++] = new WorkloadGen();
                workers[current-1].start();
            }
            Thread.sleep(5000);
            partida();
            Thread.sleep(10000);
            System.out.printf("%d threads:\n", threads);
            imprime();
            for(int i = 0; i < threads; i++) {
                workers[i].running = false;
                workers[i].join();
            }
            clearInvoices();
        }

        System.exit(0);
    }

}
