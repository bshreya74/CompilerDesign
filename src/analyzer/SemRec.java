package analyzer;

import java.util.*;

public class SemRec 
{
	public String record;
	public int size;
	public ArrayList<Integer> array;
	public ArrayList<String> nest;
	public ArrayList<String> paratype;
	public float float_num;

	
	public SemRec()
	{
		array = new ArrayList<Integer>();
		nest = new ArrayList<>();
		paratype = new ArrayList<>();
	}
}
