package pl.lbasista.magazynex.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id; //ID Produktu

    public String name; //Nazwa produktu

    public String producer; //Producent

    public int quantity; //Ilość

    public Product(String name, int quantity, String producer) {
        this.name = name;
        this.quantity = quantity;
        this.producer = producer;
    }
}