package analyzer;

import java.io.*;

public class Driver 
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Running main \n \n");
				
		FileInputStream in = null;
		FileOutputStream tokenOut = null;
		FileOutputStream lexErrOut = null;
		FileOutputStream deriveOut = null;
		FileOutputStream synErrOut = null;
		FileOutputStream tablesOut = null;
		FileOutputStream semErrOut = null;
		
		try
		{
			in = new FileInputStream("code_allocation.txt");
			tokenOut = new FileOutputStream("tokenOut.txt");
			lexErrOut = new FileOutputStream("lexError.txt");
			deriveOut = new FileOutputStream("derivation.txt");
			synErrOut = new FileOutputStream("syntaxError.txt");
			//tablesOut = new FileOutputStream("tables.txt");
			//semErrOut = new FileOutputStream("semOut.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_compiler = new Compiler(in, tokenOut, lexErrOut, deriveOut, synErrOut);
		m_compiler.Compile();
		
		in.close();
		tokenOut.close();
		lexErrOut.close();
		deriveOut.close();
		synErrOut.close();
		
	}
}
