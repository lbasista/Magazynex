package pl.lbasista.magazynex.data;

import androidx.room.Entity;
import androidx.room.Ignore;
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
    public int applicationCategoryId; //Kategoria
    @Ignore
    public String applicationName; //Nazwa kategorii

    public Product(String barcode, String name, int quantity, String producer, boolean favourite, int applicationCategoryId, String description, String imageUri) {
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
        this.producer = producer;
        this.favourite = favourite;
        this.applicationCategoryId = applicationCategoryId;
        this.description = description;
        this.imageUri = imageUri;
    }

    @Ignore
    public Product() {}
}