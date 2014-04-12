package com.whitaker.textalyzer;

import java.util.Date;

public class TextMessage 
{
	private enum Directions {OUTBOUND, INBOUND};
	private Directions direction;
	private String body;
	private Date timeCreated;

	public TextMessage(Directions direction, String body, int timeCreated) 
	{
		this.direction = direction;
		this.body = body;
		this.timeCreated = new Date(timeCreated); //Might need to multiple by 1000L
	}
}

