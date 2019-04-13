package analyzer;

import java.io.InputStream;
import java.io.OutputStream;

public class Compiler 
{	
	private LexicalAnalyzer m_lexicalAnalyzer;
	private SyntacticAnalyzer m_syntaxAnalyzer;
	private Token m_tokenPointer;
	
	public Compiler(InputStream input, OutputStream tokenOutput, OutputStream lexErrOutput, OutputStream derivationOutput, OutputStream synErrOutput)
	{
		m_lexicalAnalyzer = new LexicalAnalyzer(input, tokenOutput, lexErrOutput, "input.txt");
		m_syntaxAnalyzer = new SyntacticAnalyzer(derivationOutput, synErrOutput);
		
		m_syntaxAnalyzer.setLexicalAnalyzer(m_lexicalAnalyzer);
	}
	
	public void Compile()
	{
		System.out.println("Running Compiler \n \n");
		
		m_syntaxAnalyzer.parse();
		//m_tokenPointer = m_lexicalAnalyzer.nextToken();
		//while(m_tokenPointer.getType() != Token.T_EOF)
		//{
		//	m_tokenPointer = m_lexicalAnalyzer.nextToken();
		//}
		
		m_syntaxAnalyzer.lockOutput();
		m_lexicalAnalyzer.rewindProgram();
		m_lexicalAnalyzer.lockOutput();
		
		System.out.println("Compilation Completed Successfully\n");
	}

}
