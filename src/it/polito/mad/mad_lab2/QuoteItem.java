package it.polito.mad.mad_lab2;

import android.os.Parcel;
import android.os.Parcelable;

public class QuoteItem implements Parcelable{
	private int id;					/* ID oggetto */
	private String image;			/* Immagine */
	private String name;			/* Nome oggetto */
	private String descr;			/* Descrizione oggetto */
	private float price;			/* Prezzo */
	private int idFornitureItem;	/* ID oggetto nella mappa */
	private int count = 0;			/* Conta pezzi nel carello */
	
    public QuoteItem(Parcel source) {
    	id = source.readInt();
    	image = source.readString();
    	name = source.readString();
    	descr = source.readString();
    	price = source.readFloat();
    	idFornitureItem = source.readInt();
    	count = source.readInt();
    }
    
	public QuoteItem(int id, String image, String name, String descr, float price, int idFornitureItem) {
    	this.id = id;
    	this.image = image;
    	this.name = name;
    	this.descr = descr;
    	this.price = price;
    	this.idFornitureItem = idFornitureItem;
    }
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getIdFornitureItem() {
		return idFornitureItem;
	}

	public void setIdFornitureItem(int idFornitureItem) {
		this.idFornitureItem = idFornitureItem;
	}
	
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	
	public int getCount() {
		return count;
	}

	public void increment() {
		count++;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(descr);
		dest.writeString(image);
		dest.writeString(name);
		dest.writeFloat(price);
		dest.writeInt(idFornitureItem);
		dest.writeInt(count);
	}
	
	public static final Parcelable.Creator<QuoteItem> CREATOR = new Parcelable.Creator<QuoteItem>() {

		@Override
		public QuoteItem createFromParcel(Parcel in) {
			return new QuoteItem(in);
		}

		@Override
		public QuoteItem[] newArray(int size) {
			return new QuoteItem[size];
		}
		
	};
}
