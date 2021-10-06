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
import java.util.TreeMap;

public class Trader{

    private String ID = UUID.randomUUID().toString();
    //can be negative balance
    private double balance;
    // inventory Product : quantity
    private Map<String, Double> inventory = new HashMap<String, Double>();

    public Trader(String ID, double balance){
        this.ID = ID;
        this.balance = balance;
    }

    public String getID(){
        return this.ID;
    }
    
    public double getBalance(){
        return this.balance;
    }

    public double importProduct(String product, double amount){
        if (product == null || amount <= 0.0 ){
            return -1.0;
        }

        double newAmount;
       // newAmount = this.inventory.get(product) + amount;
        newAmount = this.inventory.getOrDefault(product,0.0) + amount;
        this.inventory.put(product, newAmount);
        return this.inventory.get(product);
    }

    
    public double exportProduct(String product, double amount){
        if (product == null || amount <= 0.0){
            return -1.0;
        }
        double newAmount;

        if (this.inventory.containsKey(product) && this.inventory.get(product) >= amount) {
            newAmount = this.inventory.get(product) - amount;
            if (newAmount == 0){
                this.inventory.remove(product);
                return 0.0;
            }
            else{
                this.inventory.put(product, newAmount);
                return this.inventory.get(product);

            }
        }
        else{
            return -1.0;
        }
    }

    public double getAmountStored(String product){
        if (product == null){
            return -1.0;
        }
        if (this.inventory.get(product) == null){
            return 0.0;
        }

        return this.inventory.get(product);
    }

    public List<String> getProductsInInventory(){

        List<String> list = new ArrayList<String>();
        for (String key : this.inventory.keySet()){
            list.add(key);
        }

        Collections.sort(list);
        return list;
    }

    public double adjustBalance(double change){
        // balance can be negative
        this.balance = this.balance + change;
        return this.balance;
    }

    public String toString(){
        String newOutput = "";
        // TreeMap to store values of HashMap
        TreeMap<String, Double> sorted = new TreeMap<>();
 
        // Copy all data from hashMap into TreeMap
        sorted.putAll(this.inventory);

        for (Map.Entry<String, Double> product : sorted.entrySet()) {
            String key = product.getKey();
            Double value = product.getValue();
            newOutput += key + ": " + String.format("%.2f",value) + ", ";
        }
        if (newOutput.length() > 0){
            newOutput = newOutput.substring(0, newOutput.length() - 2);
        }
        newOutput = this.ID + ": $" + String.format("%.2f", this.balance) + " {" + newOutput + "}" ;
        return newOutput;
    }

    public static void writeTraders(List<Trader> traders , String path) {
        try{  
            if (traders == null){
                return;
            }
            File f = new File(path);
            PrintWriter p = new PrintWriter(f);
        //PrintWriter f = ew OutputStreamWriter(new FileOutputStream(path), UTF8);
            for (Trader element : traders) {
                // or just "US-ASCII" ??
                // byte[] b = element.toString().getBytes(StandardCharsets.US_ASCII);
                // String s = new String(b, StandardCharsets.US_ASCII);
                // convert to string rep and write it back to file
                // for (byte item : b){
                //     p.print(item);
                // }
                p.println(element);
            }
            p.close();
        }
        catch ( FileNotFoundException e ) {
            return;
        }
    }

    public static void writeTradersBinary(List<Trader> traders, String path) {
        try{
            if (traders == null){
                return;
            }
            File f = new File(path);
            PrintWriter p = new PrintWriter(f);
            //PrintWriter f = ew OutputStreamWriter(new FileOutputStream(path), UTF8);
         
            for (Trader element : traders) {
                byte[] b = element.toString().getBytes(StandardCharsets.UTF_8);

                // convert to string rep and write it back to file
                for (byte item : b){
                    p.print(item);
                }
                p.print("\u001F");
            }
            p.close();
        }
        catch ( FileNotFoundException e ) {
            return;
        }
    }

}
