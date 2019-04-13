package analyzer;

import java.io.*;

public class Derivation {
	
	private DerivationNode root;
	private DerivationNode cursor;
	private OutputStream output;
	private boolean lockedOutput;
	private boolean enabled;
	
	Derivation( String value, OutputStream outStream)
	{
		root = new DerivationNode(value);
		cursor = root;
		lockedOutput = false;
		enabled = true;
		output = outStream;
		
		outputTree();
	}
	
	private void outputTree()
	{
		if(!enabled)
		{
			return;
		}
		
		String derivationOutput = getString() + "\n";
		
		try
		{
			output.write(derivationOutput.getBytes(), 0, derivationOutput.length());
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getString()
	{
		DerivationNode current = root;
		String derivation = new String();
		
		while(current != null)
		{
			derivation += current.get_Value() + " ";
			current = current.get_Next();
		}
		
		return derivation;
	}
	
	public void disable()
	{
		enabled = false;
	}
	
	public void move()
	{
		if(!enabled)
		{
			return;
		}
		
		if(cursor == null)
		{
			return;
		}
		
		cursor = cursor.get_Next();
	}

	public void delete()
	{
		if(!enabled)
		{
			return;
		}
		
		DerivationNode current = root;
		
		if(cursor == root)
		{
			root = current.get_Next();
			cursor = root;
		}
		
		else
		{
			while(current.get_Next() != cursor)
			{
				current = current.get_Next();
			}
			
			move();
			current.set_Next(cursor);
		}
		outputTree();
	}
	
	public void replace(String[] values)
	{
		if(!enabled)
		{
			return;
		}
		
		String first = values[0];
		cursor.set_Value(first);
		
		for(int i = values.length-1; 0<i; i--)
		{
			insert(values[i]);
		}
		
		outputTree();
	}
	
	private void insert(String value)
	{
		if(!enabled)
		{
			return;
		}
		
		DerivationNode newNode = new DerivationNode(value);
		DerivationNode cursorNext = cursor.get_Next();
		newNode.set_Next(cursorNext);
		cursor.set_Next(newNode);
	}
}
