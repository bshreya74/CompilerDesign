package lexicalAnalyzer;

import java.io.*;

public class Driver 
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Running main \n \n");
		
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
		System.out.println("\n \n");
		
		FileInputStream in = null;
		FileOutputStream out = null;
		FileOutputStream err_out = null;
		
		try
		{
			in = new FileInputStream(args[0]);
			out = new FileOutputStream(args[1]);
			err_out = new FileOutputStream(args[2]);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_compiler = new Compiler(in, out, err_out);
		m_compiler.Compile();
		
		in.close();
		out.close();
		err_out.close();
		
	}

}
