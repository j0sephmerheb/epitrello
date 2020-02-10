package fr.epita.epitrello.datamodel;

public class EpiUser {
	public String username;

	public EpiUser(String username) {
		setUsername(username);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}