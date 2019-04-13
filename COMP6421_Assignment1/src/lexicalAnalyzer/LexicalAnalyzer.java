package lexicalAnalyzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LexicalAnalyzer {
	
	private InputStream input;
	private OutputStream tokenOutput;
	private OutputStream errorOutput;
	
	private boolean  backupFlag = false;
	private boolean scanComplete = false;
	private int currentByte;
	private int line = 1;
	private int col = 0;
	private int refLine;
	private int refCol;
	private int errors;
	private final static char EOF = 0;
	private String stringBuffer;
	
	private boolean			m_lockedOutput = false;
	//private String			m_programName;
	private int				m_tokenLineBuffer;
	
	public LexicalAnalyzer(InputStream inStream, OutputStream outStream, OutputStream errStream)
	{
		this.input = inStream;
		this.tokenOutput = outStream;
		this.errorOutput = errStream;
	}
	
	public void lockOutput() 
	{
		m_lockedOutput = true;
	}
	
	public Token nextToken() {
		
		Token nextToken = getToken();
		outputToken(nextToken);
		return nextToken;
		
	}
	
	public Token getToken()
	{
		char c = nextchar();
		stringBuffer = new String();
		
		//remove extra space
		while(Character.isWhitespace(c))
		{
			c = nextchar();
		}
		
		refLine = line;
		refCol = col;
		
		//check if we reached end of file
		if(c == EOF)
		{
			scanComplete = true;
			return createToken(Token.T_EOF, null, false);
		}
		
		if(Character.isLetter(c))
		{
			//May be an id or reserved word
			stringBuffer += c;
			c = nextchar();
			while( Character.isLetter(c) || Character.isDigit(c) || c == '_' )
			{
				stringBuffer += c;
				c = nextchar();
			}
			backchar();
			
			//check if it is a reserved word
			if(stringBuffer.equals("if"))
				return createToken(Token.T_KEYWORD_IF, stringBuffer, false);
			if(stringBuffer.equals("then"))
				return createToken(Token.T_KEYWORD_THEN, stringBuffer, false);
			if(stringBuffer.equals("else"))
				return createToken(Token.T_KEYWORD_ELSE, stringBuffer, false);
			if(stringBuffer.equals("for"))
				return createToken(Token.T_KEYWORD_FOR, stringBuffer, false);
			if(stringBuffer.equals("class"))
				return createToken(Token.T_KEYWORD_CLASS, stringBuffer, false);
			if(stringBuffer.equals("integer"))
				return createToken(Token.T_KEYWORD_INTEGER, stringBuffer, false);
			if(stringBuffer.equals("float"))
				return createToken(Token.T_KEYWORD_FLOAT, stringBuffer, false);
			if(stringBuffer.equals("read"))
				return createToken(Token.T_KEYWORD_READ, stringBuffer, false);
			if(stringBuffer.equals("write"))
				return createToken(Token.T_KEYWORD_WRITE, stringBuffer, false);
			if(stringBuffer.equals("return"))
				return createToken(Token.T_KEYWORD_RETURN, stringBuffer, false);
			if(stringBuffer.equals("main"))
				return createToken(Token.T_KEYWORD_MAIN, stringBuffer, false);
			
			//if it is not a keyword it is an identifier
			return createToken(Token.T_ELEMENT_IDENTIFIER, stringBuffer, false);
		}
		
		if(Character.isDigit(c))
		{
			//May be an integer or fraction
			if( c == '0' )
			{
				//invalid integer, first digit 0
				stringBuffer +=c;
				outputError(refLine, refCol, "Error: first digit should be nonzero.");
				return createToken(Token.T_ELEMENT_INTEGER, stringBuffer, true);
			}
			else
			{
				stringBuffer += c;
				c = nextchar();
				while(Character.isDigit(c))
				{
					stringBuffer += c;
					c = nextchar();
				}
			}
			
			if( c == '.')
			{
				//number is float
				stringBuffer += c;
				c = nextchar();
				if( Character.isDigit(c) )
				{
					stringBuffer += c;
					c = nextchar();
					while(Character.isDigit(c))
					{
						stringBuffer += c;
						c = nextchar();
					}
					
					//non digit character
					backchar();
					return createToken(Token.T_ELEMENT_FLOAT, stringBuffer, false);
				}
				
				else
				{
					stringBuffer += c;
					//error in float
					outputError(refLine, refCol, "badly formed float; expected digit after '.' got '" + c + "' instead");
					return createToken(Token.T_ELEMENT_FLOAT, stringBuffer, true);
				}
			}
			backchar();
			// found integer token
			return createToken(Token.T_ELEMENT_INTEGER, stringBuffer, false);
		}
		
		if (c == '/') {
			c = nextchar();
			if (c == '/') {
				c = nextchar();
				while (c != '\n' && c != EOF) {
					c = nextchar();
				}
				backchar();
				createToken(Token.T_PUNCTUATION_COMMENT, "<comment>", false);
				// Ignore comment token and fetch the following token.
				return nextToken();
			}
			else if (c == '*') 
			{
				c = nextchar();
				while (c != EOF) 
				{
					if (c == '*') 
					{
						c = nextchar();
						if (c == '/') 
						{
							// Create comment token
							 createToken(Token.T_PUNCTUATION_CLOSE_MULTI_COMMENT, "<close comment>", false);	
							 return nextToken();
						}
						if (c == EOF) 
						{
							// Error: multi-line comment missing closure.
							outputError(refLine, refCol, "unclosed multi line comment.");
							createToken(Token.T_PUNCTUATION_CLOSE_MULTI_COMMENT, null, true);	
							return nextToken();
						}
					}
					c = nextchar();
				}
				// Error: multi-line comment missing closure.
				outputError(refLine, refCol, "unclosed multi-line comment.");
				createToken(Token.T_PUNCTUATION_CLOSE_MULTI_COMMENT, null, true);
				return nextToken();
			}
			else 
			{
				// It's a division operator
				backchar();
				return createToken(Token.T_OPERATOR_DIVIDE, "<divide>", false);												
			}
		}
		
		if(c == '=')
		{
			c = nextchar();
			if( c == '=')
			{
				return createToken(Token.T_OPERATOR_EQUALTO, "<equal to>", false);
			}
			else
			{
				backchar();
				return createToken(Token.T_OPERATOR_EQUALS, "<equals>", false);
			}
		}
		
		if( c == '<')
		{
			c = nextchar();
			if( c == '>')
			{
				return createToken( Token.T_OPERATOR_GREATER_OR_LESSER, "<less than or greater than>", false );
			}
			else if ( c == '=')
			{
				return createToken( Token.T_OPERATOR_LESSTHAN_OR_EQUALS, "<less than or equals>", false );
			}
			else
			{
				backchar();
				return createToken( Token.T_OPERATOR_LESSERTHAN, "<less than>", false);
			}
		}
		
		if( c == '>')
		{
			c = nextchar();
			if( c == '=')
			{
				return createToken( Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, "<greater than or equals>", false);
			}
			
			backchar();
			return createToken(Token.T_OPERATOR_GREATERTHAN, "<greater than>", false);
		}
		
		if( c == ':')
		{
			c = nextchar();
			if( c == ':')
			{
				return createToken(Token.T_PUNCTUATION_DOUBLE_COLON, "<double colon>", false);
			}
			
			backchar();
			return createToken(Token.T_PUNCTUATION_COLON, "<colon>", false);
		}
		
		if( c == '&')
		{
			c = nextchar();
			if( c == '&')
			{
				return createToken(Token.T_OPERATOR_LOGICAL_AND, "<Logial AND>", false);
			}
			backchar();
			//error in Logical AND
			return createToken(Token.T_OPERATOR_LOGICAL_AND, null, true);
		}
		
		if( c =='|')
		{
			c = nextchar();
			if( c == '|' )
			{
				return createToken(Token.T_OPERATOR_LOGICAL_OR, "<Logical OR>", false);
			}
			backchar();
			//error
			return createToken(Token.T_OPERATOR_LOGICAL_OR, null, true);
		}
		
		if( c == ';')
		{
			return createToken(Token.T_PUNCTUATION_SEMICOLON, "<semicolon>", false);
		}
		
		if( c == '.')
		{
			return createToken( Token.T_PUNCTUATION_DOT, "<dot>", false );
		}
		
		if( c == ',')
		{
			return createToken( Token.T_PUNCTUATION_COMMA, "<comma>", false );
		}
		 
		if( c == '+' )
		 {
			 return createToken( Token.T_OPERATOR_ADD, "<addition>", false );
		 }
		 
		if( c == '-')
		{
			return createToken( Token.T_OPERATOR_SUBTRACT, "<subtract>", false );
		}
		
		if( c == '*')
		{
			return createToken( Token.T_OPERATOR_MULTIPLY, "<multiply>", false );
		}
		
		if( c == '!' )
		{
			return createToken( Token.T_OPERATOR_LOGICAL_NOT, "<logical NOT>", false );
		}
		
		if( c == '(' )
		{
			return createToken( Token.T_PUNCTUATION_OPEN_BRACKET, "<Open Bracket>", false );
		}
		
		if( c == ')')
		{
			return createToken( Token.T_PUNCTUATION_CLOSE_BRACKET, "<close bracket>", false );
		}
		
		if( c == '{')
		{
			return createToken( Token.T_PUNCTUATION_OPEN_PARANTHESIS, "<open parenthesis>", false );
		}
		
		if( c == '}')
		{
			return createToken( Token.T_PUNCTUATION_CLOSE_PARANTHESIS, "<close parenthesis>", false);
		}
		
		if( c == '[' )
		{
			return createToken( Token.T_PUNCTUATION_OPEN_SQ_BRACKET, "<open sq bracket>", false);
		}
		
		if( c == ']' )
		{
			return createToken( Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, "<close sq bracket>", false);
		}
		
		//error: Unknown token
		stringBuffer += c;
		//Output error
		outputError(refLine, refCol, "unknown token.");
		return createToken( Token.T_ELEMENT_UNKNOWN, stringBuffer, true);
	}
	
	
	private Token createToken(int type, String value, boolean hasError)
	{
		Token token = new Token(type, value, hasError, refLine, refCol);
		return token;
	}
	
	private char nextchar()
	{
		//check if end of file reached
		if(scanComplete)
		{
			return EOF;
		}
		
		//if we need to go back one character return last read character
		if(backupFlag)
		{
			backupFlag = false;
		}
		
		else
		{
			//read next character
			try
			{
				currentByte = input.read();
			}catch(IOException e)
			{
				e.printStackTrace();
			}
			
			//if character is \n increase line number
			if(currentByte == '\n')
			{
				col =0;
				line +=1;
			}
			
			//if character is \t increase col number by 4
			else if(currentByte == '\t')
			{
				col +=4;
			}
			
			//we got a character so increase col number by 1
			else
			{
				col +=1;
			}
		}
		
		if (currentByte == -1)
		{
			//reached end of source code
			return EOF;
		}
		
		return (char)currentByte;
	}
	
	private void backchar()
	{
		backupFlag = true;
	}
	public boolean isScanningComplete() {
		return scanComplete;
	}
	
	public void outputToken(Token token)
	{
		// We add new line characters to match the output token string line to the source code line
		if (m_lockedOutput) { return; }
		
		int tokenLine = token.getNumLine();
		String newlines = "";
		int i;
		for (i = 0; i < (tokenLine - m_tokenLineBuffer); i++) 
		{
			newlines += "\r\n";
		}
		
		String sToken = newlines + token.toString() + ' ';
		byte b[] = sToken.getBytes();
		
		// Write token to output
		try 
		{
			tokenOutput.write(b, 0, sToken.length());
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		m_tokenLineBuffer = tokenLine;
	}
	
	public void outputError(int line, int col, String message)
	{
		errors++;
		
		if (m_lockedOutput) 
		{ 
			return; 
		}
		
		// Send error message to output.
		message = "Lexical error:" + " (" + Integer.toString(line) + ',' + Integer.toString(col) + ") " + message + "\r\n";
		byte b[] = message.getBytes();
		
		// Write token to output
		try 
		{
			errorOutput.write(b, 0, message.length());
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public int getErrorCount() 
	{
		return errors;
	}
	
}
