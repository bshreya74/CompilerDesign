package analyzer;

import java.io.*;
import java.util.*;

public class SemAnalyzer 
{
	public LinkedList<SymbolTable> tables;
	FileOutputStream TableOut;
	FileOutputStream ErrorOut;
	boolean firstpass = false;
	
	public SemAnalyzer(String tableout, String error)
	{
		tables = new LinkedList<SymbolTable>();
		try 
		{
			TableOut = new FileOutputStream(tableout);
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			ErrorOut = new FileOutputStream(error);
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	} 
	
	public void create(String table)
	{
		tables.add(new SymbolTable(table));
	}
	
	public boolean createTable(String table)
	{
		if(firstpass){
			return true;
		}
		create(table);
		return true;
	}
	
	public SymbolTable getTable(String table)
	{
		for(int a = 0; a < tables.size(); a++)
		{
			if(tables.get(a).getTableName().equals(table))
			{
				return tables.get(a);
			}
		}
		return null;
	}
	
	public void insert(String table, String id, String kind, String type, String link, String parent)
	{
		for(int i = 0; i < tables.size(); i++)
		{
			if(tables.get(i).getTableName().equals(table))
			{
				tables.get(i).addRecord(id, kind, type, link, parent);
				return;
			}
		}
	}
	
	public boolean addEntry(String table, String id, String kind, String type, String link, String parent) 
	{
		if(firstpass)
		{
			return true;
		}
		for(int i = 0; i < tables.size(); i++)
		{
			if(tables.get(i).getTableName().equals(table))
			{
				SymbolTable tab = tables.get(i);
				LinkedList<String[]> Entry = tab.entry;
				for(int a = 0; a < Entry.size(); a++)
				{
					String[] list = Entry.get(a);
					if(list[0].equals(id))
					{
						id = id + "*";
					}
				}	
			}
		}
		insert(table, id, kind, type, link, parent);
		return true;
	}

	public void print(String table)
	{
		for(int a = 0; a < tables.size(); a++)
		{
			if(tables.get(a).getTableName().equals(table))
			{
				try 
				{
					TableOut.write(("==========Symbol Table:"+tables.get(a).getTableName()+"=========="+'\n').getBytes());
				} catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				for(int b = 0; b < tables.get(a).entry.size(); b++)
				{
					try 
					{
						TableOut.write((Arrays.toString(tables.get(a).entry.get(b))+'\n').getBytes());
					} catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				try 
				{
					TableOut.write(("------------------------------------------------------------------------"+'\n').getBytes());
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void delete(String table)
	{
		for(int k = 0; k < tables.size(); k++)
		{
			if(tables.get(k).getTableName().equals(table))
			{
				tables.remove(tables.get(k));
			}
		}
		
	}
	
	public void setSecondPass()
	{
		firstpass = true;
	}
	
	public boolean classCheck(String id)
	{
		if(!firstpass)
		{
			return true;
		}
		int counter = 0;
		for(int a = 0; a < tables.size(); a ++)
		{
			if(tables.get(a).getTableName().equals("Global"))
			{
				for(int b = 0; b< tables.get(a).entry.size(); b++)
				{
					if(tables.get(a).entry.get(b)[0].equals(id))
					{
						counter = counter + 1;
					}
					if(tables.get(a).entry.get(b)[0].equals(id+"*"))
					{
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0)
		{
			try 
			{
				ErrorOut.write(("class:"+id+" is not defined!!"+'\n').getBytes());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("class:"+id+" is not defined!!");
		}
		if(counter > 1)
		{
			try 
			{
				ErrorOut.write(("class:"+id+" has mutiple class declaration!!"+'\n').getBytes());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("class:"+id+" has mutiple class declaration!!");
		}
		return true;
	}
	public boolean funCheck(String Tn, String id)
	{
		if(!firstpass)
		{
			return true;
		}
		int counter = 0;
		for(int a = 0; a < tables.size(); a ++)
		{
			if(tables.get(a).getTableName().equals(Tn))
			{
				for(int b = 0; b< tables.get(a).entry.size(); b++)
				{
					if(tables.get(a).entry.get(b)[0].equals(id))
					{
						counter = counter + 1;
					}
					if(tables.get(a).entry.get(b)[0].equals(id+"*"))
					{
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0)
		{
			try
			{
				ErrorOut.write(("function:"+id+" is not defined!!"+'\n').getBytes());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("function:"+id+" is not defined!!");
		}
		if(counter > 1)
		{
			try 
			{
				ErrorOut.write(("function:"+id+" has mutiple function declaration!!"+'\n').getBytes());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("function:"+id+" has mutiple function declaration!!");
		}
		return true;
	}
	
	public boolean paraCheck(String Tn, String id)
	{
		if(!firstpass)
		{
			return true;
		}
		int counter = 0;
		for(int s = 0; s < tables.size(); s++)
		{
			if(tables.get(s).getTableName().equals(Tn))
			{
				for(int c = 0; c < tables.get(s).entry.size(); c++)
				{
					if(tables.get(s).entry.get(c)[0].equals(id))
					{
						counter = counter + 1;
					}
					if(tables.get(s).entry.get(c)[0].equals(id+"*"))
					{
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0)
		{
			try 
			{
				ErrorOut.write(("parameter:"+id+" is not defined!!"+'\n').getBytes());
			} catch (IOException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("parameter:"+id+" is not defined!!");
		}
		if(counter > 1)
		{
			try 
			{
				ErrorOut.write(("parameter:"+id+" has mutiple parameter declaration!!"+'\n').getBytes());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("parameter:"+id+" has mutiple parameter declaration!!");
		}
		return true;
	}
	
	public boolean varCheck(String Tn, String id)
	{
		if(!firstpass)
		{
			return true;
		}
		int counter = 0;
		for(int s = 0; s < tables.size(); s++)
		{
			if(tables.get(s).getTableName().equals(Tn))
			{
				for(int c = 0; c < tables.get(s).entry.size(); c++)
				{
					if(tables.get(s).entry.get(c)[0].equals(id))
					{
						counter = counter + 1;
					}
					if(tables.get(s).entry.get(c)[0].equals(id+"*"))
					{
						counter = counter + 1;
					}
				}
				if(counter == 1)
				{
					return true;
				}
				if(counter > 1)
				{
					try 
					{
						ErrorOut.write(("variable:"+id+" has mutiple variable declaration!!"+'\n').getBytes());
					} catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("variable:"+id+" has mutiple variable declaration!!");
					return true;
				}
				if(Tn.equals("Global"))
				{
					if(counter == 0)
					{
						try 
						{
							ErrorOut.write(("variable:"+id+" is not defined!!"+'\n').getBytes());
						} catch (IOException e) {
							
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("variable:"+id+" is not defined!!");
						return true;
					}
				}
				if(tables.get(s).entry.get(0)[4].equals(null))
				{
					if(counter == 0)
					{
						try
						{
							ErrorOut.write(("variable:"+id+" is not defined!!"+'\n').getBytes());
						} catch (IOException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("variable:"+id+" is not defined!!");
						return true;
					}
				}	
				varCheck(tables.get(s).entry.get(0)[4], id);
			}
		}
		return true;
		
	}
	
	//type check, only related to type, not about dimension check
	public boolean typeCheck(String Tn, String type)
	{
		System.out.println("In Sem Analyzer. type = " + type);
		if(!firstpass)
		{
			return true;
		}
//		String[] splitstring;
//		String newtype;
//		String newdemision;
		for(int a = 0; a < tables.size(); a ++)
		{
			if(tables.get(a).getTableName().equals(Tn))
			{
				if(type.equals("integer") || type.equals("float"))
				{
					return true;
				}
				for(int b = 0; b < tables.get(a).entry.size(); b++)
				{
//					splitstring = tables.get(a).entry.get(b)[2].split(":");
//					newtype = splitstring[0];
//					newdemision = splitstring[1];
					if(tables.get(a).entry.get(b)[0].equals(type))
					{
						return true;
					}
				}
				if(tables.get(a).getTableName().equals("Global"))
				{
					try 
					{
						ErrorOut.write(("type:"+type+" is not defined!!"+'\n').getBytes());
					} catch (IOException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("type:"+type+" is not defined!!");
					return true;
				}
				if(tables.get(a).entry.get(0)[4].equals(null))
				{
					try 
					{
						ErrorOut.write(("type:"+type+" is not defined!!"+'\n').getBytes());
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("type:"+type+" is not defined!!");
					return true;
				}
				typeCheck(tables.get(a).entry.get(0)[4], type);
				
			}
		}
		return true;
	}
}
