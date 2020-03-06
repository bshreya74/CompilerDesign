package analyzer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CodeGenerator {
	FileOutputStream code_data;
	FileOutputStream code_instruction;
	public boolean thirdpass;
	
	public void setThirdPass(Counter counter){
		if(counter.counter == 0){
		thirdpass = true;	
		}
		
	}
	public void closeThirdPass(){
		thirdpass = false;
	}
	
	
	public CodeGenerator(String data, String instruction){
		
		
		try {
			code_data = new FileOutputStream(data);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			code_instruction = new FileOutputStream(instruction);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean pro(){
		if(!thirdpass){
			return true;
		}
		try {
			code_instruction.write(("entry"+'\n').getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean halt(){
		if(!thirdpass){
			return true;
		}
		try {
			code_instruction.write(("hlt"+'\n'+'\n').getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean varGenerate(SemRec name, String table, SemAnalyzer t){
		if(!thirdpass){
			return true;
		}
		String[] a = t.search(table, name.record);
		int size = Integer.parseInt(a[6]);
		//type is int or float
		if(size == 4 ){
			if(a[2].equals("integer") || a[2].equals("float")){
				try {
					code_data.write((table+"_"+name.record+"   dw 0"+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			try {
				code_data.write((table+"_"+name.record+"   res "+size+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		if(size > 4){
			try {
				code_data.write((table+"_"+name.record+"   res "+size+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
		
	}
	
	public int offsetCompute(SemRec nest, SemRec type, String tablename, SemAnalyzer t, SemRec funname){
		SymbolTable a = t.getTable(tablename);
		int offset = 0;
		String t1 = tablename;
		String t2 = tablename;
		
		//find the last type of the nest
		for(int b = 0; b < nest.nest.size(); b++){
			if(b == nest.nest.size()-1){
				type.record = t.getKind(t2, nest.nest.get(b));
				break;
			}
			t2 = t.getReturnType(t2, nest.nest.get(b));
		}
		if(type.record.equals("function")){
			int l = nest.nest.size();
			String last = nest.nest.get(l-1);
		
			
			for(int i = 0; i < t.tables.size(); i++){
				for(int j = 0; j < t.tables.get(i).entry.size(); j++){
					if(t.tables.get(i).entry.get(j)[0].equals(last)){
						funname.record = t.tables.get(i).entry.get(j)[4];
						return 0;
					}
				}
			}
			
		}
		

		if(nest.nest.size() == 1){
			String id = nest.nest.get(0);
			for(int i = 0; i<a.entry.size(); i++){
				if(a.entry.get(i)[0].equals(id)){
					if(a.entry.get(i)[2].equals("int") || a.entry.get(i)[2].equals("float")){
						return 0;
					}
					
					return offset;
				}
				offset = offset + Integer.parseInt(a.entry.get(i)[6]);
			}
			
			
		}
		//deep nest
		for(int j = 0; j < nest.nest.size(); j++){
			if(j == nest.nest.size()-1){
				return offset;
				
			}
			t1 = t.getReturnType(t1, nest.nest.get(j));
			a = t.getTable(t1);
			for(int w = 0; w < a.entry.size(); w++){
				if(a.entry.get(w)[0].equals(nest.nest.get(j+1))){
					break;
				}
				offset = offset + Integer.parseInt(a.entry.get(w)[6]);
			}	
		}
		return 0;
		
	}

		
		public boolean assGenerate(SemRec nest, SemRec nest2,SemRec right_id, String tablename, SemAnalyzer t){
			if(!thirdpass){
				return true;
			}
			SymbolTable a = t.getTable(tablename);
			String t1 = tablename;
			int offset = 0;
			int offset1 = 0;
			int offset2 = 0;
			
			//right side is a num
			if(nest2.nest.toString().equals("[]")){
				//right side is a float
				if(right_id.array.toString().equals("[]")){
					//left side is a id
						if(nest.nest.size() == 1){
							String id = nest.nest.get(0);
							for(int i = 0; i < a.entry.size(); i ++){
								if(a.entry.get(i)[0].equals(id)){ 
									if(a.entry.get(i)[2].equals("int") || a.entry.get(i)[2].equals("float")){
										try {
											code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sub r2,r2,r2"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r2,r2,"+right_id.float_num+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sw "+tablename+"_"+id+"(r0)"+",r2"+'\n').getBytes());
										} catch (IOException e) { 
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										return true;
									}
									try {
										code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      addi r0,r0,"+offset+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      sub r2,r2,r2"+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      addi r2,r2,"+right_id.float_num+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      sw "+tablename+"_"+id+"(r0)"+",r2"+'\n').getBytes());
									} catch (IOException e) { 
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return true;
								}
								offset = offset + Integer.parseInt(a.entry.get(i)[6]);
							}
				
						}
						
						//left side is a nest
						for(int j = 0; j < nest.nest.size(); j++){
							if(j == nest.nest.size()-1){
								a = t.getTable(t1);
								for(int q = 0; q < a.entry.size(); q++){
									if(a.entry.get(q)[0].equals(nest.nest.get(j))){
										try {
											code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r0,r0,"+offset+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sub r2,r2,r2"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r2,r2,"+right_id.float_num+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sw "+tablename+"_"+nest.nest.get(0)+"(r0)"+",r2"+'\n').getBytes());
										} catch (IOException e) { 
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										return true;
									}
									offset = offset+ Integer.parseInt(a.entry.get(q)[6]);
								}
							}
							t1 = t.getReturnType(t1, nest.nest.get(j));
						
							a = t.getTable(t1);
							for(int w = 0; w < a.entry.size(); w++){
								if(a.entry.get(w)[0].equals(nest.nest.get(j+1))){
									break;
								}
									offset = offset + Integer.parseInt(a.entry.get(w)[6]);
							
							
							}
							
						}
						
						
					}
				// right side is an integer
					else{
						int length = right_id.array.toString().length();
						String num = right_id.array.toString().substring(1, length-1);
						if(nest.nest.size() == 1){
							String id = nest.nest.get(0);
							for(int i = 0; i < a.entry.size(); i ++){
								if(a.entry.get(i)[0].equals(id)){
									if(a.entry.get(i)[2].equals("int") || a.entry.get(i)[2].equals("float")){
										try {
											code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sub r2,r2,r2"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r2,r2,"+num+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sw "+tablename+"_"+id+"(r0)"+",r2"+'\n').getBytes());
										} catch (IOException e) { 
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										return true;
									}
									try {
										code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      addi r0,r0,"+offset+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      sub r1,r1,r1"+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      addi r1,r1,"+num+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										code_instruction.write(("      sw "+tablename+"_"+id+"(r0)"+",r1"+'\n').getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return true;
								}
								offset = offset + Integer.parseInt(a.entry.get(i)[6]);
							}
				
						}
						for(int j = 0; j < nest.nest.size(); j++){
							if(j == nest.nest.size()-1){
								a = t.getTable(t1);
								for(int q = 0; q < a.entry.size(); q++){
									if(a.entry.get(q)[0].equals(nest.nest.get(j))){
										try {
											code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r0,r0,"+offset+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sub r2,r2,r2"+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      addi r2,r2,"+num+'\n').getBytes());
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										try {
											code_instruction.write(("      sw "+tablename+"_"+nest.nest.get(0)+"(r0)"+",r2"+'\n').getBytes());
										} catch (IOException e) { 
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										return true;
									}
									offset = offset+ Integer.parseInt(a.entry.get(q)[6]);
								}
							}
							t1 = t.getReturnType(t1, nest.nest.get(j));
						
							a = t.getTable(t1);
							for(int w = 0; w < a.entry.size(); w++){
								if(a.entry.get(w)[0].equals(nest.nest.get(j+1))){
									break;
								}
									offset = offset + Integer.parseInt(a.entry.get(w)[6]);
							
							
							}
							
						}
					}
			}
			
			
			
			//right side is a nest
			SemRec type1 = new SemRec();
			SemRec type2 = new SemRec();
			SemRec funname1 = new SemRec();
			SemRec funname2 = new SemRec();
			offset1 = offsetCompute(nest, type1, tablename, t, funname1);
			offset2 = offsetCompute(nest2, type2, tablename, t, funname2);
			String id1 = nest.nest.get(0);
			String id2 = nest2.nest.get(0);
			
			//function
			if(type2.record.equals("function")){
				
				try {
					code_instruction.write(("      jl r15,"+funname2.record+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					code_instruction.write(("      sw "+tablename+"_"+id1+"(r0),r1"+'\n'+'\n').getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			
			
			try {
				code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      addi r0,r0,"+offset2+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      lw r1,"+tablename+"_"+id2+"(r0)"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      addi r0,r0,"+offset1+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      sw "+tablename+"_"+id1+"(r0),"+"r1"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return true;	
		}
		
		public boolean func(SemRec funcname, String parentname){
			if(!thirdpass){
				return true;
			}
			try {
				code_instruction.write((parentname+":"+funcname.record+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		
		public boolean return_value(SemRec nest, SemRec id, String tablename, String classname, SemAnalyzer t){
			if(!thirdpass){
				return true;
			}
			exprcode(nest, id, classname+":"+tablename, t);
			try {
				code_instruction.write(("      sub r1,r1,r1"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				code_instruction.write(("      lw r1,"+classname+":"+tablename+"(r0)"+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		
		public boolean funj(){
			if(!thirdpass){
				return true;
			}
			try {
				code_instruction.write(("      jr r15"+'\n'+'\n').getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		}
		
		public void exprcode(SemRec nest, SemRec id, String tablename, SemAnalyzer t){
			
			//right side is a num
			if(nest.nest.toString().equals("[]")){
				if(id.array.toString().equals("[]")){
					try {
						code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      sub r4,r4,r4"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      addi r4,r4,"+id.float_num+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      sw "+tablename+"(r0),r4"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					int length = id.array.toString().length();
					String num = id.array.toString().substring(1, length-1);
					try {
						code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      sub r4,r4,r4"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      addi r4,r4,"+num+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      sw "+tablename+"(r0),r4"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//right side is a nest
			else{
				SemRec type = new SemRec();
				SemRec funname= new SemRec();
				int offset = offsetCompute(nest, type, tablename, t, funname);
				String id1 = nest.nest.get(0);
				
				//function
				if(type.record.equals("function")){
					
					try {
						code_instruction.write(("      jl r15,"+funname.record+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("   !!   sw "+tablename+"(r0),r1"+'\n'+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else{
					try {
						code_instruction.write(("      sub r0,r0,r0"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      addi r0,r0,"+offset+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      sub r5,r5,r5"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						code_instruction.write(("      lw r5,"+tablename+"_"+id1+"(r0)"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
					try {
						code_instruction.write(("      sw "+tablename+"(r0),"+"r5"+'\n').getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				
				
			}
			
		}

}
