package org.reviewboard.rtc;

public class ReviewBoardRepository {

	public final String username;
	public final String password;
	public final String address;
	public final String name;

	public ReviewBoardRepository(String username, String password, String address, String name) {
		this.username = username;
		this.password = password;
		this.address = address;
		this.name = name;
	}

}
