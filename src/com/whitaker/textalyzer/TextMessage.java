package com.whitaker.textalyzer;

public class TextMessage 
{
	private enum Directions {OUTBOUND, INBOUND};
	private Directions direction;
	private String body;
	private long timeCreated;

	public TextMessage(Directions direction, String body, int timeCreated) 
	{
		this.direction = direction;
		this.body = body;
		this.timeCreated = timeCreated; //Might need to multiple by 1000L
	}
}

