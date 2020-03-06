package analyzer;

import java.io.*;
import java.util.*;

public class SemAnalyzer 
{
	public LinkedList <SymbolTable> tables;
	FileOutputStream TableOut;
	FileOutputStream ErrorOut;
	boolean firstpass = false;
	boolean secondpass = false;

	
	public SemAnalyzer(String tableout, String error){
		tables = new LinkedList<SymbolTable>();
		try {
			TableOut = new FileOutputStream(tableout);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ErrorOut = new FileOutputStream(error);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	void create (String Tn){
		tables.add(new SymbolTable(Tn));
	}
	
	public boolean createTable(String Tn) {
		if(!firstpass){
			return true;
		}
		create(Tn);
		return true;
	}
	
	public void setFirstPass(){
		firstpass = true;
		try {
			TableOut.write(("**********First Pass begins**********"+'\n'+"**********Create Symbol Table**********"+'\n'+'\n').getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeFirstPass(){
		firstpass = false;
		try {
			TableOut.write(('\n'+"**********First Pass ends**********"+'\n').getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setSecondPass(){
		secondpass = true;
	}
	
	public void closeSecondPass(){
		secondpass = false;
	}
	
	
	
	
	public SymbolTable getTable(String Tn){
		for(int a = 0; a < tables.size(); a++){
			if(tables.get(a).getTableName().equals(Tn)){
				return tables.get(a);
			}
		}
		return null;
	}
	
	public void insert(String Tn, String i, String kind, String type, SemRec dimension, String link, String parent, int size){
		for(int j = 0; j < tables.size(); j++){
			if(tables.get(j).getTableName().equals(Tn)){
				tables.get(j).addRecord(i, kind, type, dimension, link, parent, size);
				return;
			}
		}
	}
	
	public boolean addEntry(String Tn, String i, String kind, String type, SemRec dimension, String link, String parent, int size) {
		if(!firstpass){
			return true;
		}
		insert(Tn, i, kind, type, dimension, link, parent, size);
		return true;
	}
	
	public boolean duplicateCheck(String Tn, SemRec id){
		if(!firstpass){
			return true;
		}
		SymbolTable t = getTable(Tn);
		for(int a = 0; a < t.entry.size(); a ++){
			if(t.entry.get(a)[0].equals(id.record)){
				id.record = id.record +"*";
			}
		}
		return true;
	}
	
	
	
	public String[] search(String Tn, String id){
		SymbolTable t = getTable(Tn);
		for(int i = 0; i < t.entry.size(); i++){
			if(t.entry.get(i)[0].equals(id)){
				return t.entry.get(i);
			}
		}
		if(Tn.equals("Global")){
			return null;
		}
	
		return search(t.entry.get(0)[5], id);
		
	}

	public boolean print(String Tn){
		if(!firstpass){
			return true;
		}
		for(int a = 0; a < tables.size(); a++){
			if(tables.get(a).getTableName().equals(Tn)){
				try {
					TableOut.write(("==========Symbol Table:"+tables.get(a).getTableName()+"=========="+'\n').getBytes());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for(int b = 0; b < tables.get(a).entry.size(); b++){
					try {
						TableOut.write((Arrays.toString(tables.get(a).entry.get(b))+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					TableOut.write(("------------------------------------------------------------------------"+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		}
		return true;
	}
	
	public void delete(String Tn){
		for(int k = 0; k < tables.size(); k++){
			if(tables.get(k).getTableName().equals(Tn)){
				tables.remove(tables.get(k));
			}
		}
		
	}
	
	
	public boolean classCheck(String id, int row, int column, Counter c){
		if(!secondpass){
			return true;
		}
		int counter = 0;
		for(int a = 0; a < tables.size(); a ++){
			if(tables.get(a).getTableName().equals("Global")){
				for(int b = 0; b< tables.get(a).entry.size(); b++){
					if(tables.get(a).entry.get(b)[0].equals(id)){
						counter = counter + 1;
					}
					if(tables.get(a).entry.get(b)[0].equals(id+"*")){
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0){
			c.counter = c.counter + 1;
			try {
				ErrorOut.write(("class:"+id+" is not defined!! [Error location: row:"+row+" column:"+column+"] "+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(counter > 1){
			c.counter = c.counter + 1;
			try {
				ErrorOut.write(("class:"+id+" has mutiple class declaration!! [Error location: row:"+row+" column:"+column+"] "+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return true;
	}
	public boolean funCheck(String Tn, String id, int row, int column, Counter c){
		if(!secondpass){
			return true;
		}
		int counter = 0;
		for(int a = 0; a < tables.size(); a ++){
			if(tables.get(a).getTableName().equals(Tn)){
				for(int b = 0; b< tables.get(a).entry.size(); b++){
					if(tables.get(a).entry.get(b)[0].equals(id)){
						counter = counter + 1;
					}
					if(tables.get(a).entry.get(b)[0].equals(id+"*")){
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0){
			c.counter = c.counter + 1;
			try {
				ErrorOut.write(("function:"+id+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(counter > 1){
			c.counter = c.counter + 1;
			try {
				ErrorOut.write(("function:"+id+" has mutiple function declaration!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return true;
	}
	
	public boolean paraCheck(String Tn, String id, int row, int column, Counter d){
		if(!secondpass){
			return true;
		}
		int counter = 0;
		for(int s = 0; s < tables.size(); s++){
			if(tables.get(s).getTableName().equals(Tn)){
				for(int c = 0; c < tables.get(s).entry.size(); c++){
					if(tables.get(s).entry.get(c)[0].equals(id)){
						counter = counter + 1;
					}
					if(tables.get(s).entry.get(c)[0].equals(id+"*")){
						counter = counter + 1;
					}
				}
			}
		}
		if(counter == 0){
			d.counter = d.counter + 1;
			try {
				ErrorOut.write(("parameter:"+id+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		if(counter > 1){
			d.counter = d.counter + 1;
			try {
				ErrorOut.write(("parameter:"+id+" has mutiple parameter declaration!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return true;
	}
	
	public boolean varCheck(String Tn, String id, int row, int column, Counter d){
		if(!secondpass){
			return true;
		}
		int counter = 0;
		for(int s = 0; s < tables.size(); s++){
			if(tables.get(s).getTableName().equals(Tn)){
				for(int c = 0; c < tables.get(s).entry.size(); c++){
					if(tables.get(s).entry.get(c)[0].equals(id)){
						counter = counter + 1;
					}
					if(tables.get(s).entry.get(c)[0].equals(id+"*")){
						counter = counter + 1;
					}
				}
				if(counter == 1){
					return true;
				}
				if(counter > 1){
					d.counter = d.counter + 1;
					try {
						ErrorOut.write(("variable:"+id+" has mutiple variable declaration!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return true;
				}
				if(Tn.equals("Global")){
					if(counter == 0){
						d.counter = d.counter + 1;
						try {
							ErrorOut.write(("variable:"+id+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return true;
					}
				}
				
				if(tables.get(s).entry.size() == 0){
					String tablename = Tn.split(":")[0];
					varCheck(tablename, id, row, column, d);

					return true;
				}
				
				if(tables.get(s).entry.get(0)[5].equals(null)){
					
					if(counter == 0){
						d.counter = d.counter + 1;
						try {
							ErrorOut.write(("variable:"+id+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return true;
					}
				}	
				varCheck(tables.get(s).entry.get(0)[5], id, row, column, d);
			}
		}
		return true;
		
	}
	
	//type check, only related to type, not about dimension check
	public boolean datatypeCheck(String type, int row, int column, Counter c){
		if(!secondpass){
			return true;
		}
		if(type.equals("integer") || type.equals("float")){
			return true;
		}
		for(int a = 0; a < tables.size(); a ++){
			if(tables.get(a).getTableName().equals("Global")){
				for(int b = 0; b < tables.get(a).entry.size(); b++){
					if(tables.get(a).entry.get(b)[0].equals(type)){
						return true;
					}
				}
				c.counter = c.counter + 1;
				try {
					ErrorOut.write(("type:"+type+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return true;
			}
		}
		
		return true;
	}
	
	public boolean nest(SemRec nest_, SemRec id){
		if(!secondpass){
			return true;
		}
		nest_.nest.add(id.record);
		return true;
	}
	public boolean addIndex(SemRec nest_, SemRec index){
		if(!secondpass){
			return true;
		}
		nest_.nest.add(index.array.toString());
		return true;
	}
	
	public String getReturnType(String table_name, String var_name){
		for(int a = 0; a < tables.size(); a++){
			if(tables.get(a).getTableName().equals(table_name)){
				if(tables.get(a).entry.size() == 0){
					String tablename = table_name.split(":")[0];
					return getReturnType(tablename, var_name);
				}
				for(int b = 0 ; b < tables.get(a).entry.size(); b++){
					if(tables.get(a).entry.get(b)[0].equals(var_name)){
						return tables.get(a).entry.get(b)[2];
					}
				}
				if(table_name.equals("Global")){ 
					return null;
				}
				String tablename = tables.get(a).entry.get(0)[5];
				return getReturnType(tablename, var_name);
			
				
			}
			
		}
		return null;
	}
	
	public String getKind(String table_name, String var_name){
		for(int a = 0; a < tables.size(); a++){
			if(tables.get(a).getTableName().equals(table_name)){
				for(int b = 0 ; b < tables.get(a).entry.size(); b++){
					if(tables.get(a).entry.get(b)[0].equals(var_name)){
						return tables.get(a).entry.get(b)[1];
					}
				}
				return getKind(tables.get(a).entry.get(0)[5], var_name);
			}
			
		}
		return null;
	}
	
	// check if every variable is defined
	public boolean nestCheck(SemRec nest, String tablename, String classname, int row, int column, Counter c){
		if(!secondpass){
			return true;
		}
		
		String table_name = classname +":"+tablename;
		if(nest.nest.size() == 1){ 
			varCheck(table_name, nest.nest.get(0), row,column, c);
			return true;
		}
		for(int a = 0; a < nest.nest.size(); a++){
			if(a == nest.nest.size() - 1){
				String last_type = getKind(table_name, nest.nest.get(a));
				if(last_type == null){
					try {
						ErrorOut.write(("variable or function:"+nest.nest.get(a)+" is not defined!![Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}
				if(last_type.equals("function")){
					
					funCheck(table_name, nest.nest.get(a), row, column, c);
				}
					
					varCheck(table_name, nest.nest.get(a), row, column, c);
					table_name = getReturnType(table_name, nest.nest.get(a));
					return true;
				
			}
			
			varCheck(table_name, nest.nest.get(a), row, column, c);
			table_name = getReturnType(table_name, nest.nest.get(a));
			
		}
		return true;
	}
	
	public boolean int_type(SemRec type){
		if(!secondpass){
			return true;
		}
		
		type.record = "integer";

		return true;
	}
	public boolean float_type(SemRec type){
		if(!secondpass){
			return true;
		}
		type.record = "float";
		return true;
	}
	
	//get the last variable or function type
	public boolean nestType(SemRec type, SemRec nest, String functionname, String classname){
		if(!secondpass){
			return true;
		}
		
		String table_name = classname +":"+functionname;
		for(int a = 0; a < nest.nest.size(); a++){
			
			if(a == nest.nest.size()-1){
				type.record = getReturnType(table_name, nest.nest.get(a));
				table_name = getReturnType(table_name, nest.nest.get(a));
				return true;
			}
			
			table_name = getReturnType(table_name, nest.nest.get(a));
		}
		return true;
	}
	
	public boolean funType(SemRec type, String tablename, String classname, SemAnalyzer t){
		if(!secondpass){
			return true;
		}
		
		SymbolTable a = t.getTable(classname);
		for(int i = 0; i < a.entry.size(); i++){
			if(a.entry.get(i)[0].equals(tablename)){
				type.record = a.entry.get(i)[2];
				return true;
			}
		}
		return true;
	}
			
	public boolean typeCheck(SemRec type1, SemRec type2, int row, int column, Counter c){
		if(!secondpass){
			return true;
		} 
		
		if(type1.record == null ){
			
			
			
			c.counter = c.counter + 1;
			try {
				ErrorOut.write((type1.record+" is not equal to "+type2.record+" [Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		if(type2.record == null){
			
			
			c.counter = c.counter + 1;
			try {
				ErrorOut.write((type1.record+" is not equal to "+type2.record+" [Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		if(type1.record.equals(type2.record)){
			
			return true;
		}
		
		c.counter = c.counter + 1;
		try {
			ErrorOut.write((type1.record+" is not equal to "+type2.record+" [Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return true;	
	}
	
	public boolean paraType(SemRec nest, SemRec type, String tablename, String classname){
		if(!secondpass){
			return true;
		}

		String table_name = classname +":"+tablename;
		
		
			
		//function
		if(nest.nest.size() == 1){
			String id = nest.nest.get(0);
			
			SymbolTable t = getTable("Global:"+id);
			if(t == null){
				return true;
			}
			for(int i = 0; i < t.entry.size(); i++){
				if(t.entry.get(i)[1].equals("parameter")){
					type.paratype.add(t.entry.get(i)[2]);
				}
			}
			return true;
		}
		
		
		
		//method 
		for(int a = 0; a < nest.nest.size(); a ++){
			table_name = getReturnType(table_name, nest.nest.get(a));
			
			
			if(a == nest.nest.size()-2){
				//find the table 
				SymbolTable t = getTable(table_name);
				for(int i = 0 ; i < t.entry.size(); i ++){
					if(t.entry.get(i)[0].equals(nest.nest.get(a+1))){
						table_name = t.entry.get(i)[4];
					
						
					}
				}
				SymbolTable tt = getTable(table_name);
				for(int b = 0; b <tt.entry.size(); b ++ ){
					if(tt.entry.get(b)[1].equals("parameter")){
						type.paratype.add(tt.entry.get(b)[2]);
					
					}	
				}
				return true;
			}
		}
		return true;
	}
	
	//add the parameter type
	public boolean paraNest(SemRec nest, SemRec type){
		if(!secondpass){
			return true;
		}
		nest.paratype.add(type.record);
		return true;
	}
	
	public boolean paraReturnCheck(SemRec paranest1, SemRec paranest2, int row, int column, Counter c){
		if(!secondpass){
			return true;
		}
		if(paranest1.paratype.size() != paranest2.paratype.size()){
			c.counter = c.counter + 1;
			try {
				ErrorOut.write(("function parameter size is different [Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			return true;
		}
		for(int a = 0; a <paranest1.paratype.size(); a ++){
			if(!(paranest1.paratype.get(a).equals(paranest2.paratype.get(a)))){
				c.counter = c.counter + 1;
				try {
					ErrorOut.write(("parameter:["+paranest1.paratype.get(a)+"] type is different from parameter type:["+paranest2.paratype.get(a)+"]"+" [Error location: row:"+row+" column:"+column+"]"+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
		return true;
	}
	
	public boolean sizeCompute(SemRec type, SemRec dimension, SemRec size){
		if(!firstpass){
			return true;
		}
	
		if(type.record.equals("integer")){
			if(dimension.array.toString().equals("[]")){
				size.size = 4;
				return true;
			}
			for(int a = 0; a < dimension.array.size(); a++ ){
				size.size = size.size + 4 * dimension.array.get(a);
			}
			return true;
		}
		if(type.record.equals("float")){
			if(dimension.array.toString().equals("[]")){
				size.size = 4;
				return true;
			}
			for(int a = 0; a < dimension.array.size(); a++ ){
				size.size = size.size + 4 * dimension.array.get(a);
			}
			return true;
		}
	
			size.size = 0;
			return true;
	
	}
	
	
	public int class_var_size(SemAnalyzer t, String tablename){
		SymbolTable a = t.getTable(tablename);
		int size = 0;
		int d_size = 0;
		if(a == null){return 0;};
		for(int b = 0 ; b < a.entry.size(); b++){
			if(a.entry.get(b)[1].equals("variable") && (!a.entry.get(b)[2].equals("integer")) && (!a.entry.get(b)[2].equals("float"))){
				if(a.entry.get(b)[3].equals("[]")){
					a.entry.get(b)[6] = Integer.toString(class_var_size(t, a.entry.get(b)[2]));	
				}
					else{
					String d = a.entry.get(b)[3];
					d = d.substring(1, d.length()-1);
					String[] e = d.split(", ");
					for(int i = 0; i< e.length; i++){
						d_size = d_size + Integer.parseInt(e[i]);
					}
					a.entry.get(b)[6] = Integer.toString(class_var_size(t, a.entry.get(b)[2]) * d_size);
				}
			
			}
			if(a.entry.get(b)[1].equals("function")){
			 class_var_size(t, a.entry.get(b)[4]);
				
			}
			if(a.entry.get(b)[1].equals("parameter") && !(a.entry.get(b)[2].equals("integer")) && !(a.entry.get(b)[2].equals("float"))){
				if(a.entry.get(b)[3].equals("[]")){
					a.entry.get(b)[6] = Integer.toString(class_var_size(t, a.entry.get(b)[2]));	
				}
				else{
					String d = a.entry.get(b)[3];
					d = d.substring(1, d.length()-1);
					String[] e = d.split(", ");
					for(int i = 0; i< e.length; i++){
						d_size = d_size + Integer.parseInt(e[i]);
					}
					a.entry.get(b)[6] = Integer.toString(class_var_size(t, a.entry.get(b)[2]) * d_size);
				}
			}
			
		}
		for(int c = 0 ; c < a.entry.size(); c++){
			size = size + Integer.parseInt(a.entry.get(c)[6]); 
		}
		return size;
	}
	
	
	//update size
	public void update_size(SemAnalyzer t){
		
		SymbolTable a =	t.getTable("Global");
		for(int m = 0; m < a.entry.size(); m++){
			if(a.entry.get(m)[1].equals("class")){
				int p = class_var_size(t, a.entry.get(m)[0]);
				a.entry.get(m)[6] = Integer.toString(p);
			}
			if(a.entry.get(m)[0].equals("main") && a.entry.get(m)[1].equals("function")){
				int p = class_var_size(t, a.entry.get(m)[4]);
				a.entry.get(m)[6] = Integer.toString(p);
			}else {
				class_var_size(t, a.entry.get(m)[4]);
				
				
			}
		}
	}
}
