package com.esrij.rest.jaxrs.entity;

public class MyData {
	private long id;
	private String message;

	public MyData() {

	}

	public MyData(long id, String message) {
		super();
		this.id = id;
		this.message = message;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}