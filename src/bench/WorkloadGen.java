package bench;


import app.Store;

import java.util.Random;

public class WorkloadGen extends Thread {

    private static long ultima = -1;

    private static long iaa = 0, tra = 0, c = 0;
    private static boolean start = false;

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
