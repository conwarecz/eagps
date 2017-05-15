package net.aineuron.eagps.model.transfer;

/**
 * Created by Vit Veres on 15-May-17
 * as a part of Android-EAGPS project.
 */

public class LoginInfo {
	private String username;
	private String password;

	public LoginInfo() {
		password = "";
		username = "";
	}

	public LoginInfo(String password, String username) {
		this.password = password;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
