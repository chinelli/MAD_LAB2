package it.polito.mad.mad_lab2;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;

public class FornitureItem implements Parcelable,Comparable<FornitureItem>{
	private Context ctx;
	private int id;						/* ID oggetto */
	private int idDB;					/* ID oggetto DB */
	private Point center;				/* Punto centrale della stanza */
	private boolean collided = false;	/* Collisione */
	private Integer height;				/* Altezza */
	private String image;				/* Nome immagine (Vista alto) */
	private Integer length;				/* Larghezza */
	private Integer rotation;			/* Rotazione */
	private boolean wall = false;		/* Muri */
	
	public FornitureItem(Parcel source) {
		boolean[] tmp = new boolean[2];
    	id = source.readInt();
    	idDB = source.readInt();
    	center = source.readParcelable(Point.class.getClassLoader());
    	height = source.readInt();
    	source.readBooleanArray(tmp);
    	collided = tmp[0];
    	image = source.readString();
    	length = source.readInt();
    	rotation = source.readInt();
    	wall = tmp[1];
	}
	
	public FornitureItem(Context ctx, int id, Point center, Integer length, Integer height,
			Integer rotation, String image, int idDB) {
		this.ctx = ctx;
		this.id = id;
		this.center = center;
		this.length = length;
		this.height = height;
		this.rotation = rotation;
		this.image = image;
		this.idDB = idDB;
	}
	
	public Drawable getImage(){
		Drawable d = null;
		
		try {
			InputStream ims = ctx.getAssets().open(image);
			d = Drawable.createFromStream(ims, null);
		} catch (IOException e) {
			return null;
		}
	    
	    return d;
	}
	
	public void setImage(String image) {
		this.image = image;
	}
	
  	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getHeight() {
		return height;
	}


	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getRotation() {
		return rotation;
	}

	public void setRotation(Integer rotation) {
		this.rotation = rotation;
	}


	public boolean isCollided() {
		return collided;
	}

	public void setCollided(boolean collided) {
		this.collided = collided;
	}

	public int getIdDB() {
		return idDB;
	}

	public void setIdDB(int idDB) {
		this.idDB = idDB;
	}

	public boolean isWall() {
		return wall;
	}

	public void setWall(boolean wall) {
		this.wall = wall;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(idDB);
		dest.writeParcelable(center, flags);
		dest.writeInt(height);
		dest.writeInt(id);
		dest.writeBooleanArray(new boolean[] {collided, wall});
		dest.writeString(image);
		dest.writeInt(length);
		dest.writeInt(rotation);
	}
	
	public static final Parcelable.Creator<FornitureItem> CREATOR = new Parcelable.Creator<FornitureItem>() {

		@Override
		public FornitureItem createFromParcel(Parcel in) {
			return new FornitureItem(in);
		}

		@Override
		public FornitureItem[] newArray(int size) {
			return new FornitureItem[size];
		}
		
	};

	@Override
	public int compareTo(FornitureItem another) {
		if (this.center.equals(another.getCenter()))
			return 0;
		return -1;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FornitureItem) {
			FornitureItem fi = (FornitureItem) o;
			return (this.compareTo(fi)==0);
		}
		else
			return false;
		
	}
}
