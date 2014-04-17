package com.whitaker.textalyzer;

public class TextMessage 
{
	public enum Directions {OUTBOUND, INBOUND};
	public Directions direction;
	public String body;	
	public long timeCreated; 

	public TextMessage(Directions direction, String body, long timeCreated) 
	{
		this.direction = direction;
		this.body = body;
		this.timeCreated = timeCreated; 
	}
}

