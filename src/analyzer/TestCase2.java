package analyzer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestCase2 {
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Running main \n \n");
		
		FileInputStream in = null;
		FileOutputStream tokenOut = null;
		FileOutputStream lexErrOut = null;
		FileOutputStream deriveOut = null;
		FileOutputStream synErrOut = null;
		String tablesOut = "tables.txt";
		String semErrOut = "semcheck.txt";
		String codeData = "codeData.txt";
		String codeInstruction = "codeInstruction.txt";
		String programName = "program2.txt";
		
		try
		{
			in = new FileInputStream(programName);
			tokenOut = new FileOutputStream("tokenOut.txt");
			lexErrOut = new FileOutputStream("lexError.txt");
			deriveOut = new FileOutputStream("derivation.txt");
			synErrOut = new FileOutputStream("syntaxError.txt");
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_compiler = new Compiler(in, tokenOut, lexErrOut, deriveOut, synErrOut, tablesOut, semErrOut, codeData, codeInstruction, programName);
		m_compiler.Compile();
		
		in.close();
		tokenOut.close();
		lexErrOut.close();
		deriveOut.close();
		synErrOut.close();
		
	}

}
