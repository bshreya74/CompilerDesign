package lexicalAnalyzer;

import java.io.InputStream;
import java.io.OutputStream;

public class Compiler 
{
	
	private LexicalAnalyzer m_lexicalAnalyzer;
	private Token m_tokenPointer;
	
	public Compiler(InputStream input, OutputStream output, OutputStream erroutput)
	{
		m_lexicalAnalyzer = new LexicalAnalyzer(input, output, erroutput);
		
	}
	
	public void Compile()
	{
		System.out.println("Running Compiler \n \n");
		m_tokenPointer = m_lexicalAnalyzer.nextToken();
		while(m_tokenPointer.getType() != Token.T_EOF)
		{
			m_tokenPointer = m_lexicalAnalyzer.nextToken();
		}
		
		System.out.println("Compilation Completed Successfully\n");
	}
}
