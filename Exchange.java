import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.List;
import java.io.*;
import java.nio.charset.*;
import java.util.Comparator;
import java.util.Arrays;
import java.lang.StringBuilder;

public class Exchange{

    List<String> traderID = new ArrayList<String>();
    List<Trader> traders = new ArrayList<Trader>();
    Market market = new Market();
    int orderID = 0;

    public Exchange(){
    }

    public void run(){
        Scanner sc = new Scanner(System.in);
        String s;
        while (sc.hasNextLine()) { 
            s = sc.nextLine();
            if (s.toUpperCase().equals("EXIT")){
                System.out.println("$ Have a nice day.");
                return;
            }

            String[] input = s.split(" ");
            String command = input[0].toUpperCase();
            String Id = "";
            if (input.length > 1 ){
                Id = input[1];
            }

            if (command.equals("ADD")) {
                double balance = Double.parseDouble(input[2]);
                if (!traderID.contains(Id)) {
                    if ( balance >= 0) {
                        Trader newTrader = new Trader(Id, balance);
                        traders.add(newTrader);
                        traderID.add(Id);
                        System.out.println("$ Success.");
                    }
                    else{
                        System.out.println("$ Initial balance cannot be negative.");
                    }
                    
                }
                else{
                    System.out.println("$ Trader with given ID already exists.");
                }
            }
            ArrayList<String> idCommands = new ArrayList<String>(Arrays.asList("ADD", "BALANCE","INVENTORY","AMOUNT","SELL","BUY", "IMPORT","EXPORT"));

            if (idCommands.contains(command)){
                if ( traderID.contains(Id) ){ 
                    for (Trader trader : traders ){
                        if (trader.getID().equals(Id)){
                            if (command.equals("BALANCE")){
                                System.out.println("$ $" + String.format("%.2f", trader.getBalance()) );
                            }

                            if (command.equals("INVENTORY")){
                                if (trader.getProductsInInventory().size() > 0){
                                    for (String a : trader.getProductsInInventory() ) {
                                        System.out.println("$ " + a);
                                    }
                                }
                                else{
                                System.out.println("$ Trader has an empty inventory.");
                                }
                            }
                            
                            if (command.equals("AMOUNT")){
                                String product = input[2];
                                if (trader.getAmountStored(product) > 0.0){
                                    System.out.println("$ " + String.format("%.2f", trader.getAmountStored(product)) );
                                }
                                else{
                                    System.out.println("$ Product not in inventory.");
                                }
                            
                            }
                            // sell id product amount price
                            if (command.equals("SELL")){
                                String product = input[2];
                                double amount = Double.parseDouble(input[3]);
                                double price = Double.parseDouble(input[4]);
                                
                                StringBuilder sb = new StringBuilder();
                                sb.append(Integer.toHexString(orderID));
                                while (sb.length() < 4) {
                                    sb.insert(0, '0'); // pad with leading zero if needed
                                }
                                String hex = sb.toString();

                                Order sellOrder = new Order(product, false, amount, price, trader, hex );
                                List <Trade> sellList = market.placeSellOrder(sellOrder);
                                orderID += 1;
                                
                                if ( sellList == null ){
                                    orderID -= 1;
                                    System.out.println("$ Order could not be placed onto the market.");
                                }

                                else if (sellOrder.isClosed()){
                                    System.out.println("$ Product sold in entirety, trades as follows:");
                                    for ( Trade trade : sellList ) {
                                        System.out.println(trade);
                                    }
                                }
                                else if (!sellOrder.isClosed() && sellList.size() > 0 ){
                                    System.out.println("$ Product sold in part, trades as follows:");
                                    for ( Trade trade : sellList ) {
                                        System.out.println(trade);
                                    }
                                }
                                else if (!sellOrder.isClosed()){
                                    System.out.println("$ No trades could be made, order added to sell book.");
                                }
                            }

                            // buy id product amount price
                            if (command.equals("BUY")){
                                String product = input[2];
                                double amount = Double.parseDouble(input[3]);
                                double price = Double.parseDouble(input[4]);

                                StringBuilder sb = new StringBuilder();
                                sb.append(Integer.toHexString(orderID));
                                while (sb.length() < 4) {
                                    sb.insert(0, '0'); // pad with leading zero if needed
                                }
                                String hex = sb.toString();

                                Order buyOrder = new Order(product, true, amount, price, trader, hex );
                                List <Trade> buyList = market.placeBuyOrder(buyOrder);
                                orderID += 1;

                                if ( buyList == null ){
                                    orderID -= 1;
                                    System.out.println("$ Order could not be placed onto the market.");
                                }

                                else if (buyOrder.isClosed()){
                                    System.out.println("$ Product bought in entirety, trades as follows:");
                                    for ( Trade trade : buyList ) {
                                        System.out.println(trade);
                                    }
                                }
                                else if (!buyOrder.isClosed() && buyList.size() > 0){
                                    System.out.println("$ Product bought in part, trades as follows:");
                                    for ( Trade trade : buyList ) {
                                        System.out.println(trade);
                                    }
                                }
                                else if (!buyOrder.isClosed()){
                                    System.out.println("$ No trades could be made, order added to buy book.");
                                }
                            }

                            if (command.equals("IMPORT")){
                                String product = input[2];
                                double amount = Double.parseDouble(input[3]);
                                double output = trader.importProduct(product, amount);
                                if (output > 0) {
                                    System.out.println("$ Trader now has " + String.format("%.2f", output) + " units of " + product + ".");
                                }
                                else{
                                    System.out.println("$ Could not import product into market.");
                                }

                            }

                            if (command.equals("EXPORT")){
                                String product = input[2];
                                double amount = Double.parseDouble(input[3]);
                                double output = trader.exportProduct(product, amount);
                                if (output > 0) {
                                    System.out.println("$ Trader now has " + String.format("%.2f", output) + " units of " + product + ".");
                                }
                                else if (output == 0 ){
                                    System.out.println("$ Trader now has no units of " + product + ".");
                                }
                                else{
                                    System.out.println("$ Could not export product out of market.");
                                }

                            }
                        }
                    }
                }
                else{
                    System.out.println("$ No such trader in the market.");
                }
            }
            

            
            if ( command.equals("CANCEL") ){
                String sellOrBuy = input[1].toUpperCase();
                String orderId = input[2];

                if ( sellOrBuy.equals("SELL") ) {
                    if ( market.cancelSellOrder(orderId)) {
                        System.out.println("$ Order successfully cancelled.");
                    }
                    else{
                        System.out.println("$ No such order in sell book.");
                    }
                }
                 if ( sellOrBuy.equals("BUY") ) {
                    if ( market.cancelBuyOrder(orderId)) {
                        System.out.println("$ Order successfully cancelled.");
                    }
                    else{
                        System.out.println("$ No such order in buy book.");
                    }
                }
            }

            if ( command.equals("ORDER") ){
                String orderId = input[1];

                List<Order> sellList = market.getSellBook();
                List<Order> buyList = market.getBuyBook();             

                if ( sellList.size() ==0 && buyList.size() == 0){
                    System.out.println("$ No orders in either book in the market.");
                    continue;
                } 

                sellList.addAll(buyList);
                List <String> output = new ArrayList<String>();

                for (Order orders : sellList){
                    output.add(orders.getID());

                    if (orders.getID().equals(orderId) ){
                        System.out.println("$ " + orders.toString());
                    }
                }
                if ( ! output.contains(orderId) ) {
                    System.out.println("$ Order is not present in either order book.");
                }
            }

            if (command.equals("TRADERS")){
                if (traderID.size() == 0) {
                    System.out.println("$ No traders in the market.");
                }
                else{
                    System.out.print("$ ");
                    for (String traderId : traderID){
                        System.out.println(traderId);
                    }
                }
            }

            if (command.equals("TRADES")){
                
                
                if (input.length > 1 && ( input[1].toUpperCase().equals("TRADER") ) ){
                    String traderId = input[2];

                    if (!traderID.contains(traderId)) {
                        System.out.println("$ No such trader in the market.");
                    }
                    else{
                        List<Trade> output = new ArrayList<Trade>();
                        for (Trader trader: traders ){

                            if (trader.getID() == traderId){
                                output = Market.filterTradesByTrader(market.getTrades(), trader);
                            }
                        }

                        if (output.size() == 0){
                            System.out.println("$ No trades have been completed by trader.");
                        }
                        else{
                            for (Trade trade: output){
                                System.out.println(trade);
                            }
                        }
                        
                    }
                }
                else if (input.length > 1 && ( input[1].toUpperCase().equals("PRODUCT") ) ){
                    String product = input[2];
                    List<Trade> output = Market.filterTradesByProduct(market.getTrades(), product);
                    if (output.size() == 0){
                        System.out.println("$ No trades have been completed with given product.");
                    }
                    else{
                        for (Trade trade: output){
                            System.out.println(trade);
                        }
                    }
                }
                else{
                    if (market.getTrades().size() != 0) {
                        System.out.print("$ ");
                        for ( Trade trades: market.getTrades()){
                            System.out.println(trades);
                        }
                    }
                    else{
                        System.out.println("$ No trades have been completed.");
                    }
                }
            }

            if (command.equals("BOOK")){

                if (input[1].toUpperCase().equals("SELL")){

                    if ( market.getSellBook().size() == 0 ){
                        System.out.println("$ The sell book is empty.");
                    }
                    else{
                        System.out.print("$ ");
                        for (Order order: market.getSellBook() ){
                            System.out.println(order);
                        }
                    }
                }
                else if (input[1].toUpperCase().equals("BUY")){

                    if ( market.getBuyBook().size() == 0 ) {
                        System.out.println("$ The buy book is empty.");
                    }
                    else{ 
                        System.out.print("$ ");
                        for (Order order: market.getBuyBook() ){
                            System.out.println(order);
                        }
                    }
                }
            }

            if (command.equals("SAVE")){
                String traderPath = input[1];
                String tradePath = input[2];

                try{ 
                    Collections.sort(traders, new Comparator<Trader>() {
                    @Override public int compare(Trader trader1, Trader trader2) {
                    int c = 0 ;
                    c = trader1.getID().compareTo(trader2.getID());
                    return c;
                    }
                    }) ;

                    Trader.writeTraders(traders, traderPath);
                    Trade.writeTrades( market.getTrades(), tradePath);
                    System.out.println("$ Success.");
                }
                catch (Exception e){
                    System.out.println("$ Unable to save logs to file.");
                }
                
            }

            if (command.equals("BINARY")){
                String traderPath = input[1];
                String tradePath = input[2];

                try{ 
                    Collections.sort(traders, new Comparator<Trader>() {
                    @Override public int compare(Trader trader1, Trader trader2) {
                    int c = 0 ;
                    c = trader1.getID().compareTo(trader2.getID());
                    return c;
                    }
                    }) ;

                    Trader.writeTradersBinary(traders, traderPath);
                    Trade.writeTradesBinary( market.getTrades(), tradePath);
                    System.out.println("$ Success.");
                }
                catch (Exception e){
                    System.out.println("$ Unable to save logs to file.");
                }
            }
        }
        System.out.println("$ Have a nice day.");
        return;



    }
    
    public static void main(String[] args){
        Exchange newExchange = new Exchange();
        newExchange.run();
    }
}
