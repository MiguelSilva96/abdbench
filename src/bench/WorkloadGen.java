package bench;


import app.Store;

import java.util.Random;

public class WorkloadGen extends Thread {
    /*Just for testing without serial*/
    private static int invoiceId = 0;
    private static int invoiceLineId = 0;
    private static int orderId = 0;
    /*********************************/
    private static int rollbacks = 0;

    private static long ultima = -1;

    private static long iaa = 0, tra = 0, c = 0;
    private static boolean start = false;

    public synchronized static void newRollback() { rollbacks++; }
    public synchronized static int getInvoiceId() {
        return invoiceId++;
    }
    public synchronized static int getInvoiceLineId() {
        return invoiceLineId++;
    }
    public synchronized static int getOrderId() { return orderId++; }

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
        System.out.println(rollbacks/(double)c);

    }

    public static synchronized void partida() {
        start = true;
    }


    @Override
    public void run() {
        Store store = new Store();
        Random rand = new Random();
        int productId, clientId, query;


        while(true) {
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


    public static void main(String args[]) throws Exception {
        int threads = 4;

        //Some variable to keep track of times and throughput

        while(threads-- > 0)
            (new WorkloadGen()).start();

        Thread.sleep(5000);

        partida();

        Thread.sleep(10000);

        imprime();

        System.exit(0);
        //Print benchmark info or export to csv file

    }

}
