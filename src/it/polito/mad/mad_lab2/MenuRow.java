package it.polito.mad.mad_lab2;

public class MenuRow {
    public int icon;
    public String text;
            
    public MenuRow(int icon, String textMenu) {
        super();
        this.icon = icon;
        this.text = textMenu;
    }

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
