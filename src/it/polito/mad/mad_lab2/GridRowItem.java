package it.polito.mad.mad_lab2;

import android.os.Parcel;
import android.os.Parcelable;

public class GridRowItem implements Parcelable{
	private int id;
	private String image;
	private String title;
	private boolean element;	/* Elemento o oggetto? */
	private int count;			/* Conta occorrenze oggetto */
	
    public GridRowItem(Parcel source) {
    	boolean[] tmp = new boolean[1];
    	id = source.readInt();
    	image = source.readString();
    	title = source.readString();
    	count = source.readInt();
    	source.readBooleanArray(tmp);
    	element = tmp[0];
    }
	
	public GridRowItem(int id, String image, String title, boolean element, int count) {
	    this.id = id;
	    this.image = image;
	    this.title = title;
	    this.element = element;
	    this.count = count;
	}

	public GridRowItem(int id, String image, String title, boolean element) {
	    this.id = id;
	    this.image = image;
	    this.title = title;
	    this.element = element;
	    this.count = 255;
	}
	
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getImage() {
		return this.image;
	}

	public String getTitle() {
		return this.title;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isElement() {
		return element;
	}

	public void setElement(boolean element) {
		this.element = element;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	public void increment() {
		count++;
	}
	
	public void decrement() {
		--count;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof GridRowItem){
			return this.id == ((GridRowItem) o).id;
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(title);
		dest.writeString(image);
		dest.writeInt(count);
		dest.writeBooleanArray(new boolean[] {element});
	}
	
	public static final Parcelable.Creator<GridRowItem> CREATOR = new Parcelable.Creator<GridRowItem>() {

		@Override
		public GridRowItem createFromParcel(Parcel in) {
			return new GridRowItem(in);
		}

		@Override
		public GridRowItem[] newArray(int size) {
			return new GridRowItem[size];
		}
		
	};
}