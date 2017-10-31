package app;

import java.util.List;
import java.util.Scanner;

public class Menu {


    private static void printMenu() {
        System.out.println("Select option:");
        System.out.println("1: Sell product to client");
        System.out.println("2: Products sold to client");
        System.out.println("3: Top 10 products sold");
        System.out.println("0: Quit");
        System.out.println("");
    }

    public static void main(String[] args) {

        Store store = new Store();
        Scanner in  = new Scanner(System.in);
        boolean running = true;


        while(running) {
            int i = 0, clientId, productId;
            List<String> products;

            printMenu();

            switch (in.nextInt()) {
                case 1:
                    System.out.println("Product id: ");
                    productId = in.nextInt();

                    System.out.println("Client id: ");
                    clientId  = in.nextInt();

                    store.sell(productId, clientId);
                    break;
                case 2:
                    System.out.println("Client id: ");
                    clientId = in.nextInt();

                    products = store.productsSold(clientId);

                    for (String s : products) {
                        System.out.print(++i + ": ");
                        System.out.println(s);
                    }
                    break;
                case 3:
                    products = store.topTenProducts();

                    for (String s : products) {
                        System.out.print(++i + ": ");
                        System.out.println(s);
                    }
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    break;

            }
        }

    }
}
