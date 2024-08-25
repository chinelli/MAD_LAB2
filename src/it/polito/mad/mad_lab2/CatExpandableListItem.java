package it.polito.mad.mad_lab2;

public class CatExpandableListItem {
	private int id;
	private int type;
	private String name;
	
	public CatExpandableListItem(int id, int type, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
