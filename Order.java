public class Order{

    private String product;
    private boolean buy = false;
    private double amount;
    private double price;
    private Trader trader;
    private String id;
    private boolean close = false;
    

    public Order(String product, boolean buy, double amount, double price, Trader trader, String id){
        this.product = product;
        this.buy = buy;
        this.amount = amount;
        this.price = price;
        this.trader = trader;
        this.id = id;
    }

    public String getProduct(){
        return this.product;
    }

    public boolean isBuy(){
        return this.buy;
    }

    public double getAmount(){
        return this.amount;
    }

    public Trader getTrader(){
        return this.trader;
    }

    public void close(){
        //closes the order????
        this.close = true;
    }   

    public boolean isClosed(){
        //returns true if order has been closed
        return this.close;
    }

    public double getPrice(){
        return this.price;
    }

    public String getID(){
        return this.id;
    }

    public void adjustAmount(double change){
        // no need to return since void
        double newAmount = 0.0;
        newAmount = this.amount + change;
        if (newAmount > 0) {
            this.amount += change;
        }
    }  

    public String toString(){
        
        String output = "";
        String buySell = ""; 

        if (this.isBuy()){
            buySell = "BUY";
        }
        else{
            buySell = "SELL";
        }
        //Format: ID: [BUY/SELL] AMOUNTxPRODUCT @ $PRICE
        output += this.id + ": " + buySell + " " + String.format("%.2f", this.amount) + "x" + this.product + " @ " + "$" + String.format("%.2f", this.price); 
        return output;
        
    }
}
