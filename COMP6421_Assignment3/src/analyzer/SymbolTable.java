package analyzer;
import java.util.*;

public class SymbolTable 
{
	private String tableName;
	public LinkedList<String[]> entry;
	
	public SymbolTable(String name)
	{
		this.tableName = name;
		entry = new LinkedList<String[]>();
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public void addRecord(String id, String kind, String type, String link, String parent)
	{
		String[] rec = { id, kind, type, link, parent };
		entry.add(rec);
	}
}
