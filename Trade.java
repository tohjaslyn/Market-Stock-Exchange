import java.util.List;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Trade{


    private String product;
    private double amount;
    private double price;
    private Order sellOrder;
    private Order buyOrder;

    public Trade(String product, double amount, double price, Order sellOrder, Order buyOrder){
        this.product = product;
        this.amount = amount;
        this.price = price;
        this.sellOrder = sellOrder;
        this.buyOrder = buyOrder;
    }

    public String getProduct() {
        return this.product;
    }

    public double getAmount(){
        return this.amount;
    }

    public Order getSellOrder() {
        return this.sellOrder;
    }

    public Order getBuyOrder(){
        return this.buyOrder;
    }

    public double getPrice(){
        return this.price;
    }

    public String toString(){
        String output = "";
        // Format: SELLER->BUYER: AMOUNTxPRODUCT for PRICE

        output += this.sellOrder.getTrader().getID() + "->" + this.buyOrder.getTrader().getID() + ": " + String.format("%.2f", this.amount) + "x" + this.product + " for $" + String.format("%.2f", this.price) + ".";

        return output;
    }

    public boolean involvesTrader(Trader trader){

        if (this.getSellOrder().getTrader() == trader || this.getBuyOrder().getTrader() == trader) {
            return true;
        }
        return false;
    }

    public static void writeTrades(List<Trade> trades, String path) {
        if (trades == null){
            return;
        }
        try{
            File f = new File(path);
            PrintWriter p = new PrintWriter(f);
        //PrintWriter f = ew OutputStreamWriter(new FileOutputStream(path), UTF8);

            for (Trade element : trades) {
                // or just "US-ASCII" ??
                // byte[] b = element.toString().getBytes(StandardCharsets.US_ASCII);
                // convert to string rep and write it back to file
                // p.println(Arrays.toString(b));
                // for (byte item : b){
                //     p.print(item);
                // }
                p.println(element);
            }
            p.close();
        }
        catch (FileNotFoundException e){
            return;
        }
    }

    public static void writeTradesBinary(List<Trade> trades, String path) {
        if (trades == null){
            return;
        }
        try{ 
            File f = new File(path);
            PrintWriter p = new PrintWriter(f);
        //PrintWriter f = ew OutputStreamWriter(new FileOutputStream(path), UTF8);

            for (Trade element : trades) {
                byte[] b = element.toString().getBytes(StandardCharsets.UTF_8);
                // convert to string rep and write it back to file
                //p.println(Arrays.toString(b));
                for (byte item : b){
                    p.print(item);
                }
                p.println();
            }
            p.close();
        }
        catch (FileNotFoundException e){
            return;
        }
    }
}

