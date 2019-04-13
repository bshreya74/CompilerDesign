package analyzer;

import java.io.*;

public class DerivationNode {

	private String value;
	private DerivationNode nextNode;
	
	DerivationNode(String val)
	{
		this.value = val;
		this.nextNode = null;
	}
	
	public void set_Next(DerivationNode next)
	{
		this.nextNode = next;
	}
	
	public DerivationNode get_Next()
	{
		return nextNode;
	}
	
	public String get_Value()
	{
		return value;
	}
	
	public void set_Value(String val)
	{
		this.value = val;
	}
}
