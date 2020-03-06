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
	
	public void addRecord(String id, String kind, String type, SemRec dimension, String link, String parent, int size)
	{
		if(dimension == null){
			String[] rec = { id, kind, type, "[]", link, parent , Integer.toString(size)};
			entry.add(rec);
		}
		else{
			String[] rec = { id, kind, type, dimension.array.toString(), link, parent, Integer.toString(size)};
			entry.add(rec);
		}
	}
}
