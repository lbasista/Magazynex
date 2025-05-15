package pl.lbasista.magazynex.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.Nullable;

@Entity
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id; //ID Produktu

    public String barcode; //Kod kreskowy

    public String name; //Nazwa produktu
    public String producer; //Producent
    public int quantity; //Ilość
    public boolean favourite; //Ulubione
    public String imageUri; //Scieżka do zdjęcia
    public String description; //Opis

    public Product(String barcode, String name, int quantity, String producer, boolean favourite, String description, String imageUri) {
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
        this.producer = producer;
        this.favourite = favourite;
        this.description = description;
        this.imageUri = imageUri;
    }
}