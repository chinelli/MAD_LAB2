package it.polito.mad.mad_lab2;

public class CatObjectItem {
	private int id;
	private String image;
	private String textRow1;
	private String textRow2;
	private String textRow3;
	
	public CatObjectItem(int id, String image, String textRow1, String textRow2, String textRow3) {
		this.id = id;
		this.image = image;
		this.textRow1 = textRow1;
		this.textRow2 = textRow2;
		this.textRow3 = textRow3;
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
	
	public String getTextRow1() {
		return textRow1;
	}
	
	public void setTextRow1(String textRow1) {
		this.textRow1 = textRow1;
	}

	public String getTextRow2() {
		return textRow2;
	}

	public void setTextRow2(String textRow2) {
		this.textRow2 = textRow2;
	}

	public String getTextRow3() {
		return textRow3;
	}

	public void setTextRow3(String textRow3) {
		this.textRow3 = textRow3;
	}	
}
