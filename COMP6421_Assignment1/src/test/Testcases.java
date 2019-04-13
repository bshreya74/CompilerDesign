package test;

import lexicalAnalyzer.Token;
import lexicalAnalyzer.LexicalAnalyzer;
import lexicalAnalyzer.Compiler;
import lexicalAnalyzer.Driver;

import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class Testcases 
{
	
	public void testCase_1()throws IOException
	{
		System.out.println("Test case 1 \n");
		FileInputStream in_1 = null;
		FileOutputStream out_1 = null;
		FileOutputStream err_out_1 = null;
		try
		{
			in_1 = new FileInputStream("Testcases\\Testcase_1\\input.txt");
			out_1 =  new FileOutputStream("Testcases\\Testcase_1\\output.txt");
			err_out_1 =  new FileOutputStream("Testcases\\Testcase_1\\error.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_testcase_1 = new Compiler(in_1, out_1, err_out_1);
		m_testcase_1.Compile();
		
		in_1.close();
		out_1.close();
		err_out_1.close();
		
		System.out.println("Test Case 1 completed successfully");
	}
	
	public void testCase_2()throws IOException
	{
		System.out.println("Test case 2 \n");
		
		FileInputStream in_2 = null;
		FileOutputStream out_2 = null;
		FileOutputStream err_out_2 = null;
		try
		{
			in_2 = new FileInputStream("Testcases\\Testcase_2\\input.txt");
			out_2 =  new FileOutputStream("Testcases\\Testcase_2\\output.txt");
			err_out_2 =  new FileOutputStream("Testcases\\Testcase_2\\error.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_testcase_2 = new Compiler(in_2, out_2, err_out_2);
		m_testcase_2.Compile();
		
		in_2.close();
		out_2.close();
		err_out_2.close();
		
		System.out.println("Test Case 2 completed successfully");
	}
	
	public void testCase_3()throws IOException
	{
		System.out.println("Test case 3 \n");
		
		FileInputStream in_3 = null;
		FileOutputStream out_3 = null;
		FileOutputStream err_out_3 = null;
		
		try
		{
			in_3 = new FileInputStream("Testcases\\Testcase_3\\input.txt");
			out_3 =  new FileOutputStream("Testcases\\Testcase_3\\output.txt");
			err_out_3 =  new FileOutputStream("Testcases\\Testcase_3\\error.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_testcase_3 = new Compiler(in_3, out_3, err_out_3);
		m_testcase_3.Compile();
		
		in_3.close();
		out_3.close();
		err_out_3.close();
		
		System.out.println("Test Case 3 completed successfully");
	}
	
	public void testCase_4()throws IOException
	{
		System.out.println("Test case 4 \n");
		
		FileInputStream in_4 = null;
		FileOutputStream out_4 = null;
		FileOutputStream err_out_4 = null;
		
		try
		{
			in_4 = new FileInputStream("Testcases\\Testcase_4\\input.txt");
			out_4 =  new FileOutputStream("Testcases\\Testcase_4\\output.txt");
			err_out_4 =  new FileOutputStream("Testcases\\Testcase_4\\error.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_testcase_4 = new Compiler(in_4, out_4, err_out_4);
		m_testcase_4.Compile();
		
		in_4.close();
		out_4.close();
		err_out_4.close();
		
		System.out.println("Test Case 4 completed successfully");
	}
	
	public void testCase_5()throws IOException
	{
		System.out.println("Test case 5 \n");
		
		FileInputStream in_5 = null;
		FileOutputStream out_5 = null;
		FileOutputStream err_out_5 = null;
		
		try
		{
			in_5 = new FileInputStream("Testcases\\Testcase_5\\input.txt");
			out_5 =  new FileOutputStream("Testcases\\Testcase_5\\output.txt");
			err_out_5 =  new FileOutputStream("Testcases\\Testcase_5\\error.txt");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Compiler m_testcase_5 = new Compiler(in_5, out_5, err_out_5);
		m_testcase_5.Compile();
		
		in_5.close();
		out_5.close();
		err_out_5.close();
		
		System.out.println("Test Case 5 completed successfully");
	}
	
	public static void main(String[] args) throws IOException
	{
		Testcases test = new Testcases();
		test.testCase_1();
		test.testCase_2();
		test.testCase_3();
		test.testCase_4();
		test.testCase_5();
		
	}
	
}
