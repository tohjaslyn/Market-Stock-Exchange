import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.List;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Market{
    
    private List<Order> sellBook = new ArrayList<Order>();
    private List<Order> buyBook = new ArrayList<Order>();
    private List<Trade> allTrades = new ArrayList<Trade>();
    // add the order ID count

    public Market(){
    }

    public boolean cancelSellOrder(String order){
        if (order == null){
            return false;
        }
        for ( Order sellOrder : this.sellBook ){
            if (order.equals(sellOrder.getID()) ){
                
                sellOrder.close();
                sellBook.remove(sellOrder);
                return true;
            }
        }
        return false;
    }

    public boolean cancelBuyOrder(String order){
        if (order == null){
            return false;
        }
        for ( Order buyOrder : this.buyBook ){
           if (order.equals(buyOrder.getID()) ){
                buyOrder.close();
                buyBook.remove(buyOrder);
                return true;
            }
        }
        return false;
    }

    public List<Order> getSellBook(){
        //return in temporal order
        Collections.sort(this.sellBook, new Comparator<Order>() {
        @Override public int compare(Order order1, Order order2) {
        int c = 0 ;
        c = order1.getID().compareTo(order2.getID());
        return c;
        }
        }) ;
        return this.sellBook;
    }

    public List<Order> getBuyBook(){
        //return in temportal order
        Collections.sort(this.buyBook, new Comparator<Order>() {
        @Override public int compare(Order order1, Order order2) {
        int c = 0 ;
        c = order1.getID().compareTo(order2.getID());
        return c;
        }
        }) ;
        return this.buyBook;
    }

    public List<Trade> getTrades(){
        return this.allTrades;
    }

    public static List<Trade> filterTradesByProduct ( List<Trade> trades, String product){
        if (trades == null || product == null) {
            return null;
        }
        List<Trade> newTrade = new ArrayList<Trade>();
        for (Trade trade: trades){
            if (product == trade.getProduct()){
                newTrade.add(trade);
            }
        }
        return newTrade;
    }

    public static List<Trade> filterTradesByTrader (List<Trade> trades, Trader trader){
        if (trades == null || trader == null) {
            return null;
        }
        List<Trade> newTrade = new ArrayList<Trade>();
        for (Trade trade: trades){
            if (trade.involvesTrader(trader)){
                newTrade.add(trade);
            }
        }
        return newTrade;
    }


    public List<Trade> placeSellOrder(Order order){

        //process a sell order using price-time priority
        if (order == null || order.isBuy() == true){
            return null;
        }
        if (order.getTrader().getAmountStored(order.getProduct()) < order.getAmount()){ 
            return null;
        }
        //or every trade, close its finished orders, remove finished orders from their respective book, 
        //increase the balance of the seller (amount of product * price of unit), 
        //decrease the balance of the buyer, send the product to the buyer, 
        //update the amount for any unfinished orders, and add the trade to the returned list.
        List<Trade> trade = new ArrayList<Trade>();
        if ( buyBook.size() > 0 ){
            Collections.sort(buyBook, new Comparator<Order>() {
            @Override public int compare(Order order1, Order order2) {
            int c = 0 ;

            if (order1.getPrice() < order2.getPrice()) 
                c = 1;
            if (order1.getPrice() > order2.getPrice()) 
                c = -1;
            if (order1.getPrice() == order2.getPrice()) 
                c = 0;

            if (c == 0)
                c = order1.getID().compareTo(order2.getID());
            return c;
            }
            }) ;

            List<String> buyOrderList = new ArrayList<String>();
            // orderList contains many buy orders from buyBook
            for ( Order buyOrder : buyBook ){
                // if buy order has a higher or equal price to the order
                if ( !order.isClosed() && (order.getPrice() <= buyOrder.getPrice()) && ( order.getProduct().equals(buyOrder.getProduct()) ) ){
                    if (order.getAmount() <= buyOrder.getAmount()){
                        // adjust balance
                        order.getTrader().adjustBalance( buyOrder.getPrice() * order.getAmount());
                        buyOrder.getTrader().adjustBalance(-buyOrder.getPrice() * order.getAmount());
                        
                        // send product to buyer
                        order.getTrader().exportProduct(order.getProduct(), order.getAmount());
                        buyOrder.getTrader().importProduct(order.getProduct(), order.getAmount());
                        
                        // decrease buy order amount
                        buyOrder.adjustAmount(-order.getAmount());
                        order.adjustAmount(-order.getAmount());

                        //close order
                        order.close();
                        if (buyOrder.getAmount() == order.getAmount()){
                            buyOrder.close();
                            buyOrderList.add(buyOrder.getID());
                        }
                        trade.add(new Trade( order.getProduct(), order.getAmount(), buyOrder.getPrice(), order, buyOrder));
                        this.allTrades.add(new Trade( order.getProduct(), order.getAmount(), buyOrder.getPrice(), order, buyOrder));
    
                    }
                    else {
                        order.getTrader().adjustBalance( buyOrder.getPrice() * buyOrder.getAmount());
                        buyOrder.getTrader().adjustBalance(-buyOrder.getPrice() * buyOrder.getAmount());
                        
                        // if order amt is > buy order or equals
                        order.getTrader().exportProduct(order.getProduct(), buyOrder.getAmount());
                        buyOrder.getTrader().importProduct(order.getProduct(), buyOrder.getAmount());            
                        
                        buyOrder.adjustAmount(-buyOrder.getAmount());
                        order.adjustAmount(-buyOrder.getAmount());

                        buyOrderList.add(buyOrder.getID());
                        buyOrder.close();
                        trade.add(new Trade( buyOrder.getProduct(), buyOrder.getAmount(), buyOrder.getPrice(), order, buyOrder));
                        this.allTrades.add(new Trade( buyOrder.getProduct(), buyOrder.getAmount(), buyOrder.getPrice(), order, buyOrder));
                    }      
            
                }
            }
            //remove from buyBook
            for ( String orderID : buyOrderList){
                cancelBuyOrder(orderID);
            }
        }
        if (order.getAmount() == 0){
            order.close();
        }
        if (!order.isClosed() ) { 
            order.getTrader().exportProduct(order.getProduct(), order.getAmount());
            sellBook.add(order);
        }
        return trade;
    }

   public List<Trade> placeBuyOrder(Order order){

        //process a sell order using price-time priority

        if (order == null || order.isBuy() == false){
            return null;
        } 

        //or every trade, close its finished orders, remove finished orders from their respective book, 
        //increase the balance of the seller (amount of product * price of unit), 
        //decrease the balance of the buyer, send the product to the buyer, 
        //update the amount for any unfinished orders, and add the trade to the returned list.
        List<Trade> trade = new ArrayList<Trade>();

        if ( sellBook.size() > 0 ){

            Collections.sort(sellBook, new Comparator<Order>() {
            @Override public int compare(Order order1, Order order2) {
            int c = 0 ;

            if (order1.getPrice() < order2.getPrice()) 
                c = -1;
            if (order1.getPrice() > order2.getPrice()) 
                c = 1;
            if (order1.getPrice() == order2.getPrice()) 
                c = 0;

            if (c == 0)
                c = order1.getID().compareTo(order2.getID());
            return c;
            }
            }) ;

            List<String> sellOrderList = new ArrayList<String>();
            // orderList contains many buy orders from buyBook
            for ( Order sellOrder : sellBook ){
                
                // if buy order has a higher or equal price to the order
                if ( !sellOrder.isClosed() && (sellOrder.getPrice() <= order.getPrice()) && ( sellOrder.getProduct().equals(order.getProduct()) ) ){
                    if (sellOrder.getAmount() < order.getAmount()){
                        
                        // adjust balance
                        sellOrder.getTrader().adjustBalance( sellOrder.getPrice() * sellOrder.getAmount());
                        order.getTrader().adjustBalance(-sellOrder.getPrice() * sellOrder.getAmount());
                        
                        // send product to buyer
                        sellOrder.getTrader().exportProduct(sellOrder.getProduct(), sellOrder.getAmount());
                        order.getTrader().importProduct(sellOrder.getProduct(), sellOrder.getAmount());
                        
                        // decrease buy order amount
                        order.adjustAmount(-sellOrder.getAmount());
                        sellOrder.adjustAmount(-sellOrder.getAmount());

                        sellOrderList.add(sellOrder.getID());
                        sellOrder.close();
                        trade.add(new Trade( sellOrder.getProduct(), sellOrder.getAmount(), sellOrder.getPrice(), sellOrder, order));
                        this.allTrades.add(new Trade( sellOrder.getProduct(), sellOrder.getAmount(), sellOrder.getPrice(), sellOrder, order));
    
                    }
                    else {
                        sellOrder.getTrader().adjustBalance( sellOrder.getPrice() * order.getAmount());
                        order.getTrader().adjustBalance(-sellOrder.getPrice() * order.getAmount());
                        
                        // if order amt is > buy order
                        //sellOrder.getTrader().exportProduct(sellOrder.getProduct(), order.getAmount());
                        order.getTrader().importProduct(sellOrder.getProduct(), order.getAmount());            
                        
                        if(sellOrder.getAmount() == order.getAmount()){

                            sellOrderList.add(sellOrder.getID());
                            sellOrder.close();
                            order.close();
                        }
                        order.adjustAmount(-order.getAmount());
                        sellOrder.adjustAmount(-order.getAmount());
                        order.close();
                        
                        trade.add(new Trade( order.getProduct(), order.getAmount(), sellOrder.getPrice(), sellOrder, order));
                        this.allTrades.add(new Trade( order.getProduct(), order.getAmount(), sellOrder.getPrice(), sellOrder, order));
                    }                  
                }
            }

            for ( String orderID : sellOrderList){
                cancelSellOrder(orderID);
            }
        }
        if (order.getAmount() == 0){
            order.close();
        }
        if ( !order.isClosed() ) { 
            buyBook.add( order );
        }
        
        return trade;
    }

    // public static void main(String[] args){
    //     Trader one = new Trader("trader1", 100);
    //     Trader c = new Trader("trader2", 100);
    //     Trader d = new Trader("trader3", 100.0);
    //     // Trader e = new Trader("trader4", 100);
    //     // Trader g = new Trader("trader5",100);
    //     one.importProduct("ABC", 100.0);
    //     //Order(String product, boolean buy, double amount, double price, Trader trader, String id, boolean close)
    //     Order h = new Order("ABC", true, 10 , 7.50, c, "0003", false);
    //     Order i = new Order("ABC", true, 5 , 10, d, "0003", false);
    //     // Order j = new Order("ABC", true, 15 , 5, e, "0003", false);
    //     // Order k = new Order("ABC", true, 20 , 7.50, g, "0003", false);
    //     Market a = new Market();
    //     a.buyBook.add(h);
    //     a.buyBook.add(i);
    //     // a.buyBook.add(j);
    //     // a.buyBook.add(k);
    //     Order b = new Order("ABC", false, 100 , 6.50, one, "0001", false);

    //     //a.buyBook.add(f);
    //     //a.buyBook.add(b);
    //     // a.sellBook.add(b);
    //     //System.out.println(a.getBuyBook());
    //     //a.placeSellOrder(h);
    //     a.placeSellOrder(b);
    //     //a.buyBook.put("ABC", new ArrayList<Order>());
    //     //a.buyBook.get("ABC").add(b);
    //     System.out.println(a.getSellBook());
    //     System.out.println(a.getBuyBook());
    //     System.out.println(a.getTrades());
    //     // Order ha = new Order("ABC", false, 6.0 , 1.50, c, "0004", false);
    //     // a.placeSellOrder(ha);

    //     // //a.cancelSellOrder(b.getID());
    //     // System.out.println(a.getSellBook());
    //     // System.out.println(a.getBuyBook());
    //     // System.out.println(a.getTrades());
    // }   

}

