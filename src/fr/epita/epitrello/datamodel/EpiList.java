package fr.epita.epitrello.datamodel;

public class EpiList {
	public String listName;

	public EpiList(String listName) {
		setListName(listName);
	}
	
	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}
}
