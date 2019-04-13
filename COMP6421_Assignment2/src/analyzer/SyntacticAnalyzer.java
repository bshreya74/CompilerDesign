package analyzer;

import java.io.OutputStream;

public class SyntacticAnalyzer 
{
	private LexicalAnalyzer lex_scanner;
	private Derivation d_Node;
	private OutputStream derivationOutput;
	private OutputStream errorOutput;
	private Token lookahead;
	private boolean syntaxError;
	private int m_errors;
	
	SyntacticAnalyzer(OutputStream deriv_output, OutputStream err_output)
	{
		errorOutput = err_output;
		derivationOutput = deriv_output;
		syntaxError = true;
		m_errors =0;
	}
	
	private void outputError(int line, int col, String message)
	{
		if(syntaxError == false)
		{
			return;
		}
		m_errors++;
		message = "Syntax Error : ( " +  line + ", " + col + " ) " + message + "\r\n";
		byte[] b = message.getBytes();
		
		try
		{
			errorOutput.write(b, 0, message.length());
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean parse()
	{
		if(d_Node == null)
		{
			d_Node = new Derivation("prog", derivationOutput);
		}
		lookahead = lex_scanner.nextToken();
		if(startSymbol() && match(Token.T_EOF))
		{
			System.out.println("in Parse got EOF");
			return true;
		}
		
		lookahead = lex_scanner.nextToken();
		System.out.println("lookahead = " + lookahead);
		return false;
	}
	
	private boolean match(int token)
	{
		d_Node.move();
		
		if(lookahead.getType() == token)
		{
			lookahead = lex_scanner.nextToken();
			return true;
		}
		
		String errorMessage = "Expected " + Token.typetoString(token) + " got " + Token.typetoString(lookahead.getType()) + " instead";
		outputError(lookahead.getNumLine(), lookahead.getNumCol(), errorMessage);
		
		lookahead = lex_scanner.nextToken();
		//System.out.println("Match token false");
		return false;
	}
	
	private boolean checkLHSin(int[] set)
	{
		if(set == null)
		{
			return false;
		}
		
		for(int i = 0; i < set.length; i++)
		{
			if(lookahead.getType() == set[i])
			{
				//found token
				return true;
			}
		}
		
		return false;
	}
	
	private boolean checkEPSILONin( int[] set )
	{
		if(set == null)
		{
			return false;
		}
		
		for(int i = 0; i < set.length; i++)
		{
			if( Token.T_EPSILON == set[i])
			{
				//found token
				return true;
			}
		}
		
		return false;
	}
	
	//TODO: skipErrors
	private boolean skipErrors(int[] FIRST, int[] FOLLOW)
	{
		System.out.println("In skipErrors");
		if(checkLHSin(FIRST) || checkEPSILONin(FIRST) && checkLHSin(FOLLOW))
		{
			return true;
		}
		
		// Error found
		String expectedToken = "[ ";
		boolean begin = true;
		for(int i = 0; i< FIRST.length; i++)
		{
			if(FIRST[i] == Token.T_EPSILON)
			{
				continue;
			}
			if(begin)
			{
				expectedToken += Token.typetoString(FIRST[i]);
				begin = false;
			}
			
			else
			{
				expectedToken += ", " + Token.typetoString(FIRST[i]);
			}
		}
			
		expectedToken += " ]";
		
		//TODO: outputError
		String errorMessage = "Expected " + expectedToken + " got " + Token.typetoString(lookahead.getType()) + " instead";
		outputError(lookahead.getNumLine(), lookahead.getNumCol(), errorMessage);
		
		while( !(checkLHSin(FIRST) || checkLHSin(FOLLOW)) )
		{
			lookahead = lex_scanner.nextToken();
			if( (checkEPSILONin(FIRST) && checkLHSin(FOLLOW)) || lookahead.getType() == Token.T_EOF)
			{
				//parsing aborted;
				System.out.println("parsing aborted");
				return false;
			}
		}
		return true;
	}
	
	private boolean startSymbol()
	{
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_CLASS, Token.T_KEYWORD_MAIN, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_CLASS, Token.T_KEYWORD_MAIN, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "classDeclList", "funcDefList", Token.typetoString(Token.T_KEYWORD_MAIN), "funcBody", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if(classDeclList() && funcDefList() && match(Token.T_KEYWORD_MAIN) && funcBody() && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("Start returning true");
				return true;
			}
			System.out.println("Start in if returning false");
			return false;
		}
		System.out.println("Start outside if returning false");
		return false;
	}
	
	private boolean classDeclList()
	{
		System.out.println("classDeclList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_CLASS };
		int[] FIRST1 = { Token.T_KEYWORD_CLASS};
		int[] FOLLOW = { Token.T_KEYWORD_MAIN, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST,FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "classDecl", "classDeclList" };
			d_Node.replace(values);
			if( classDecl() && classDeclList())
			{
				System.out.println("classDeclList -> classDecl classDeclList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("classDeclList -> EPSILON");
			return true;
		}
		System.out.println("returning false outside for classDeclList");
		return false;
	}
	
	private boolean funcDefList()
	{
		System.out.println("funcDefList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_KEYWORD_MAIN };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "funcDef", "funcDefList"  };
			d_Node.replace(values);
			if( funcDef() && funcDefList())
			{
				System.out.println("funcDefList -> funcDef funcDefList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("funcDefList -> ep");
			return true;
		}
		System.out.println("returning false outside for funcDefList");
		return false;
	}
	
	private boolean classDecl()
	{
		System.out.println("classDecl");
		int[] FIRST = { Token.T_KEYWORD_CLASS };
		int[] FIRST1 = { Token.T_KEYWORD_CLASS };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = {Token.typetoString(Token.T_KEYWORD_CLASS), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "inheritedList", Token.typetoString(Token.T_PUNCTUATION_OPEN_PARANTHESIS), "memberList", Token.typetoString(Token.T_PUNCTUATION_CLOSE_PARANTHESIS), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_CLASS) && match(Token.T_ELEMENT_IDENTIFIER) && inheritedList() && match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && memberList() && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("classDecl -> 'class' 'id' inheritedList '{' memberList '}' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean inheritedList()
	{
		System.out.println("inheritedList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_COLON};
		int[] FIRST1 = { Token.T_PUNCTUATION_COLON};
		int[] FOLLOW = { Token.T_PUNCTUATION_OPEN_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = {Token.typetoString(Token.T_PUNCTUATION_COLON), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "idInClassDecList"  };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COLON) && match(Token.T_ELEMENT_IDENTIFIER) && idInClassDeclList())
			{
				System.out.println("inheritedList -> ':' 'id' idInClassDeclList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("inheritedList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean idInClassDeclList()
	{
		System.out.println("idInClassDeclList");
		int[] FIRST = { Token.T_EPSILON,  Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		int[] FOLLOW = { Token.T_PUNCTUATION_OPEN_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "idInClassDeclList"  };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && match(Token.T_ELEMENT_IDENTIFIER) && idInClassDeclList())
			{
				System.out.println("idInClassDeclList -> ',' 'id' idInClassDeclList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("idInClassDeclList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean memberList()
	{
		System.out.println("memberList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "memberListNew" };
			d_Node.replace(values);
			if( type() && match(Token.T_ELEMENT_IDENTIFIER) && memberListNew())
			{
				System.out.println("memberList -> type 'id' memberListNew ");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("memberList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean memberListNew()
	{
		System.out.println("memeberListNew");
		int[] FIRST = { Token.T_PUNCTUATION_SEMICOLON, Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_OPEN_BRACKET }; 
		int[] FIRST1 = { Token.T_PUNCTUATION_SEMICOLON, Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "varDecl", "memberList"  };
			d_Node.replace(values);
			if( varDecl() && memberList())
			{
				System.out.println("memberListNew -> varDecl memberList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { "funcDecl", "funcInClass"  };
			d_Node.replace(values);
			if(funcDecl() && funcInClass())
			{
				System.out.println("memberListNew -> funcDecl funcInClass");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcInClass()
	{
		System.out.println("funcInClass");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW ={ Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "funcInClassNew" };
			d_Node.replace(values);
			if(type() && match(Token.T_ELEMENT_IDENTIFIER) && funcInClassNew())
			{
				System.out.println("funcInClass -> type 'id' funcInClassNew");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("funcInClass -> ep");
			return true;
		}
		return false;
	}
	
	private boolean funcInClassNew()
	{
		System.out.println("funcInClassNew");
		int[] FIRST = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "funcDecl", "funcInClass"  };
			d_Node.replace(values);
			if(funcDecl() && funcInClass())
			{
				System.out.println("funcInClassNew -> funcDecl funcInClass");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcDecl()
	{
		System.out.println("funcDecl");
		int[] FIRST = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "fParamsList", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && fParamsList() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("funcDecl -> '(' fParamsList ')' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcHead()
	{
		System.out.println("funcHead");
		int[] FIRST = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", "funcHeadId", Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "fParamList", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET) };
			d_Node.replace(values);
			if( type() && funcHeadId() && match(Token.T_PUNCTUATION_OPEN_BRACKET) && fParamsList() && match(Token.T_PUNCTUATION_CLOSE_BRACKET))
			{
				System.out.println("funcHead -> type funcHeadId '(' fParamsList ')'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcHeadId()
	{
		System.out.println("funcHeadId");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "scopeSpec" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && scopeSpec())
			{
				System.out.println("funcHeadId -> 'id' scopeSpec");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean scopeSpec()
	{
		System.out.println("scopeSpec");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_DOUBLE_COLON};
		int[] FIRST1 = { Token.T_PUNCTUATION_DOUBLE_COLON };
		int[] FOLLOW = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_DOUBLE_COLON), Token.typetoString(Token.T_ELEMENT_IDENTIFIER) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_DOUBLE_COLON) && match(Token.T_ELEMENT_IDENTIFIER))
			{
				System.out.println("scopeSpec -> 'sr' 'id' ");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("scopeSpec -> ep");
			return true;
		}
		
		return false;
	}
	
	private boolean funcDef()
	{
		System.out.println("funcDef");
		int[] FIRST = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
				
		if(checkLHSin(FIRST1))
		{
			String[] values = { "funcHead", "funcBody", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( funcHead() && funcBody() && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("funcDef -> funcHead funcBody ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcBody()
	{
		System.out.println("funcBody");
		int[] FIRST = { Token.T_PUNCTUATION_OPEN_PARANTHESIS };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_PARANTHESIS };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
	
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_PARANTHESIS), "varStatinFunc", Token.typetoString(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && varStatinFunc() && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS))
			{
				System.out.println("funcBody -> '{' varStatinFunc '}'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatinFunc()
	{
		System.out.println("varStatinFunc");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_FLOAT, Token.T_KEYWORD_INTEGER, Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FIRST1 = { Token.T_KEYWORD_FLOAT, Token.T_KEYWORD_INTEGER };
		int[] FIRST2 = { Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "varStat", "varStatinFunc" };
			d_Node.replace(values);
			if( varStat() && varStatinFunc())
			{
				System.out.println("varStatinFunc ->varStat varStatinFunc");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { "varStatNew", "varStatinFuncNew" };
			d_Node.replace(values);
			if( varStatNew() && varStatinFuncNew())
			{
				System.out.println("varStatinFunc -> varStatNew varStatinFuncNew");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("varStatinFunc -> ep");
			return true;
		}
		return false;
	}
	
	private boolean varStatinFuncNew()
	{
		System.out.println("varStatinFuncNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "varStatNew", "varStatinFuncNew" };
			d_Node.replace(values);
			if(varStatNew() && varStatinFuncNew())
			{
				System.out.println("varStatinFuncNew -> varStatNew varStatinFuncNew");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("varStatinFuncNew -> ep");
			return true;
		}
		return false;
	}
	
	private boolean varStat()
	{
		System.out.println("varStat");
		int[] FIRST = { Token.T_KEYWORD_FLOAT, Token.T_KEYWORD_INTEGER };
		int[] FIRST1 = { Token.T_KEYWORD_FLOAT, Token.T_KEYWORD_INTEGER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "typeNew", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "varDecl" };
			d_Node.replace(values);
			if( typeNew() && match(Token.T_ELEMENT_IDENTIFIER) && varDecl())
			{
				System.out.println("varStat	-> typeNew 'id' varDecl");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatNew()
	{
		System.out.println("varStatNew");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "varStatTail" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && varStatTail())
			{
				System.out.println("varStatNew	-> 'id' varStatTail");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { "statementOther" };
			d_Node.replace(values);
			if(statementOther())
			{
				System.out.println("varStatNew -> statementOther");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatTail()
	{
		System.out.println("varStatTail");
		int[] FIRST = { Token.T_EPSILON, Token.T_ELEMENT_IDENTIFIER, Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_DOT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_EQUALS };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_OPERATOR_EQUALS, Token.T_PUNCTUATION_DOT };
		int[] FIRST3 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "varDecl" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && varDecl() )
			{
				System.out.println("varStatTail	-> 'id' varDecl");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { "indiceList", "idnestList", "assignStatTail", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( indiceList() && idnestList() && assignStatTail() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("varStatTail	-> indiceList idnestList assignStatTail ';'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST3))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "idnestList", "assignStatTail", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && idnestList() && assignStatTail() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("varStatTail	-> '(' aParams ')' idnestList assignStatTail ';'");
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	private boolean assignStatTail()
	{
		System.out.println("assignStatTail");
		int[] FIRST = { Token.T_OPERATOR_EQUALS };
		int[] FIRST1 = { Token.T_OPERATOR_EQUALS };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "assignOp", "expr" };
			d_Node.replace(values);
			if( assignOp() && expr() )
			{
				System.out.println("assignStatTail -> assignOp expr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean typeNew()
	{
		System.out.println("typeNew");
		int[] FIRST = { Token.T_KEYWORD_FLOAT, Token.T_KEYWORD_INTEGER };
		int[] FIRST1 = { Token.T_KEYWORD_FLOAT };
		int[] FIRST2 = { Token.T_KEYWORD_INTEGER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_FLOAT) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_FLOAT) )
			{
				System.out.println("typeNew	-> 'float'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_INTEGER) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_INTEGER) )
			{
				System.out.println("typeNew	-> 'integer'");
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	private boolean statementOther()
	{
		System.out.println("statementOther");
		int[] FIRST = { Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN }; 
		int[] FIRST1 = { Token.T_KEYWORD_IF };
		int[] FIRST2 = { Token.T_KEYWORD_FOR };
		int[] FIRST3 = { Token.T_KEYWORD_READ };
		int[] FIRST4 = { Token.T_KEYWORD_WRITE };
		int[] FIRST5 = { Token.T_KEYWORD_RETURN };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_IF), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_KEYWORD_THEN), "statBlock", Token.typetoString(Token.T_KEYWORD_ELSE), "statBlock", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_IF) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_KEYWORD_THEN) && statBlock() && match(Token.T_KEYWORD_ELSE) && statBlock() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_FOR), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "assignOp", "expr", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON), "relExpr", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON), "assignStat", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "statBlock", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_FOR) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && type() && match(Token.T_ELEMENT_IDENTIFIER) && assignOp() && expr() && match(Token.T_PUNCTUATION_SEMICOLON) && relExpr() && match(Token.T_PUNCTUATION_SEMICOLON) && assignStat() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && statBlock() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST3) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_READ), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "variable", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_READ) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && variable() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'read' '(' variable ')' ';' ");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST4))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_WRITE), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_WRITE) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'write' '(' expr ')' ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST5) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_RETURN), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_RETURN) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'return' '(' expr ')' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean statementList()
	{
		System.out.println("statementList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "statement", "statementList" };
			d_Node.replace(values);
			if( statement() && statementList() )
			{
				System.out.println("statementList -> statement statementList");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("statementList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean varDecl()
	{
		System.out.println("varDecl");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_SEMICOLON };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_SEMICOLON };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "arraySizeList", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( arraySizeList() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("varDecl -> arraySizeList ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arraySizeList()
	{
		System.out.println("arraySizeList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FOLLOW = { Token.T_PUNCTUATION_COMMA, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "arraySize", "arraySizeList" };
			d_Node.replace(values);
			if( arraySize() && arraySizeList() )
			{
				System.out.println("arraySizeList -> arraySize arraySizeList");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("arraySizeList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean statement()
	{
		System.out.println("statement");
		int[] FIRST = { Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_KEYWORD_IF };
		int[] FIRST3 = { Token.T_KEYWORD_FOR };
		int[] FIRST4 = { Token.T_KEYWORD_READ };
		int[] FIRST5 = { Token.T_KEYWORD_WRITE };
		int[] FIRST6 = { Token.T_KEYWORD_RETURN };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "assignStat", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( assignStat() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> assignStat ';' ");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_IF), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_KEYWORD_THEN), "statBlock", Token.typetoString(Token.T_KEYWORD_ELSE), "statBlock", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_IF) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_KEYWORD_THEN) && statBlock() && match(Token.T_KEYWORD_ELSE) && statBlock() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> 'if' '(' expr ')' 'then' statBlock 'else' statBlock ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST3) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_FOR), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "assignOp", "expr", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON), "relExpr", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON), "assignStat", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "statBlock", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_FOR) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && type() && match(Token.T_ELEMENT_IDENTIFIER) && assignOp() && expr() && match(Token.T_PUNCTUATION_SEMICOLON) && relExpr() && match(Token.T_PUNCTUATION_SEMICOLON) && assignStat() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && statBlock() && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> 'for' '(' type 'id' assignOp expr ';' relExpr ';' assignStat ')' statBlock ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST4) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_READ), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "variable", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_READ) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && variable() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> 'read' '(' variable ')' ';'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST5))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_WRITE), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_WRITE) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> 'write' '(' expr ')' ';'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST6) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_RETURN), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_RETURN) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statement -> 'return' '(' expr ')' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean assignStat()
	{
		System.out.println("assignStat");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "assignOp", "expr" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && assignOp() && expr() )
			{
				System.out.println("assignStat -> 'id' assignOp expr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean statBlock()
	{
		System.out.println("statBlock");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_PARANTHESIS, Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_PARANTHESIS };
		int[] FIRST2 = { Token.T_ELEMENT_IDENTIFIER, Token.T_KEYWORD_IF, Token.T_KEYWORD_FOR, Token.T_KEYWORD_READ, Token.T_KEYWORD_WRITE, Token.T_KEYWORD_RETURN };
		int[] FOLLOW = { Token.T_KEYWORD_ELSE, Token.T_PUNCTUATION_SEMICOLON };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_PARANTHESIS), "statementList", Token.typetoString(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && statementList() && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) )
			{
				System.out.println("statBlock -> '{' statementList '}'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { "statement" };
			d_Node.replace(values);
			if(statement())
			{
				System.out.println("statBlock -> statement");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("statBlock -> ep");
			return true;
		}
		return false;
	}

	private boolean expr()
	{
		System.out.println("expr");
		int[] FIRST = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "arithExpr", "exprNew" };
			d_Node.replace(values);
			if( arithExpr() && exprNew())
			{
				System.out.println("expr -> arithExpr exprNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean exprNew()
	{
		System.out.println("exprNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		int[] FIRST1 = { Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		int[] FOLLOW = { Token.T_PUNCTUATION_COMMA, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_PUNCTUATION_SEMICOLON };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1))
		{
			String[] values = { "relOp", "arithExpr" };
			d_Node.replace(values);
			if( relOp() && arithExpr() )
			{
				System.out.println("exprNew -> relOp arithExpr");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("exprNew -> ep");
			return true;
		}
		return false;
	}
	
	private boolean relExpr()
	{
		System.out.println("relExpr");
		int[] FIRST = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "arithExpr", "relOp", "arithExpr" };
			d_Node.replace(values);
			if( arithExpr() && relOp() && arithExpr() )
			{
				System.out.println("relExpr -> arithExpr relOp arithExpr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arithExpr()
	{
		System.out.println("arithExpr");
		int[] FIRST = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "term", "arithExprNew" };
			d_Node.replace(values);
			if( term() && arithExprNew() )
			{
				System.out.println("arithExpr -> term arithExprNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arithExprNew()
	{
		System.out.println("arithExprNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR };
		int[] FIRST1 = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "addOp", "term", "arithExprNew" };
			d_Node.replace(values);
			if( addOp() && term() && arithExprNew() )
			{
				System.out.println("arithExprNew -> addOp term arithExprNew");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("arithExprNew -> ep");
			return true;
		}
		return false;
	}
	
	private boolean sign()
	{
		System.out.println("sign");
		int[] FIRST = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT }; 
		int[] FIRST1 = { Token.T_OPERATOR_ADD };
		int[] FIRST2 = { Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_ADD) };
			d_Node.replace(values);
			if( match(Token.T_OPERATOR_ADD) )
			{
				System.out.println("sign -> '+'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_SUBTRACT) };
			d_Node.replace(values);
			if( match(Token.T_OPERATOR_SUBTRACT))
			{
				System.out.println("sign -> '-'");
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	private boolean term()
	{
		System.out.println("term");
		int[] FIRST = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "factor", "termNew" };
			d_Node.replace(values);
			if(factor() && termNew())
			{
				System.out.println("term -> factor termNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean termNew()
	{
		System.out.println("termNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND };
		int[] FIRST1 = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND };
		int[] FOLLOW = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "multOp", "factor", "termNew" };
			d_Node.replace(values);
			if( multOp() && factor() && termNew() )
			{
				System.out.println("termNew -> multOp factor termNew");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("termNew -> ep");
			return true;
		}
		return false;
	}
	
	private boolean factor()
	{
		System.out.println("factor");
		int[] FIRST = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT,  Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_ELEMENT_INTEGER };
		int[] FIRST3 = { Token.T_ELEMENT_FLOAT };
		int[] FIRST4 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FIRST5 = { Token.T_OPERATOR_LOGICAL_NOT };
		int[] FIRST6 = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "varFunc" };
			d_Node.replace(values);
			if(varFunc())
			{
				System.out.println("factor -> varFunc");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_INTEGER) };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_INTEGER) )
			{
				System.out.println("factor -> 'intNum'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST3) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_FLOAT) };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_FLOAT) )
			{
				System.out.println("factor -> 'floatNum'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST4) )
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "arithExpr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && arithExpr() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) )
			{
				System.out.println("factor -> '(' arithExpr ')'");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST5) )
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_LOGICAL_NOT), "factor" };
			d_Node.replace(values);
			if( match(Token.T_OPERATOR_LOGICAL_NOT) && factor() )
			{
				System.out.println("factor -> 'not' factor");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST6) )
		{
			String[] values = { "sign", "factor" };
			d_Node.replace(values);
			if( sign() && factor() )
			{
				System.out.println("factor -> sign factor");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean variable()
	{
		System.out.println("variable");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "variableNew" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && variableNew() )
			{
				System.out.println("variable -> 'id' variableNew  ");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean variableNew()
	{
		System.out.println("variableNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_DOT, Token.T_PUNCTUATION_OPEN_BRACKET};
		int[] FIRST1 = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		//int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_BRACKET};
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "indiceList", "idnestList" };
			d_Node.replace(values);
			if( indiceList() && idnestList() )
			{
				System.out.println("variableNew	-> indiceList idnestList");
				return true;
			}
			return false;
		}
		
		/*if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "idnestList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && idnestList() )
			{
				System.out.println("variableNew	-> '(' aParams ')' idnestList");
				return true;
			}
			return false;
		}*/
		return false;
	}
	
	private boolean varFunc()
	{
		System.out.println("varFunc");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "indiceList", "idnestListNew", "varFuncTail" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && indiceList() && idnestListNew() && varFuncTail() )
			{
				System.out.println("varFunc -> 'id' indiceList idnestListNew varFuncTail");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varFuncTail()
	{
		System.out.println("varFuncTail");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FOLLOW = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "varFuncTail2" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && varFuncTail2() )
			{
				System.out.println("varFuncTail	-> '(' aParams ')' varFuncTail2 ");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FOLLOW) )
		{
			d_Node.delete();
			System.out.println("varFuncTail	-> ep");
			return true;
		}
		return false;
	}
	
	private boolean varFuncTail2()
	{
		System.out.println("varFuncTail2");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		int[] FOLLOW = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "idnest" };
			d_Node.replace(values);
			if(idnest())
			{
				System.out.println("varFuncTail2 -> idnest");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("varFuncTail2 -> ep");
			return true;
		}
		return false;
	}
	
	private boolean idnestListNew()
	{
		System.out.println("idnestListNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		int[] FOLLOW = { Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if( checkLHSin(FIRST1))
		{
			String[] values = { "idnestNew", "idnestListNew" };
			d_Node.replace(values);
			if(idnestNew() && idnestListNew())
			{
				System.out.println("idnestListNew -> idnestNew idnestListNew");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("idnestListNew -> ep");
			return true;
		}
		return false;
	}
	
	private boolean indiceList()
	{
		System.out.println("indiceList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FOLLOW = { Token.T_PUNCTUATION_DOT, Token.T_OPERATOR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_COMMA, Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "indice", "indiceList" };
			d_Node.replace(values);
			if( indice() && indiceList())
			{
				System.out.println("indiceList -> indice indiceList");
				return true;
			}
			return false;
		}
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("indiceList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean idnestNew()
	{
		System.out.println("idnestNew");
		int[] FIRST = { Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_DOT), "idnestNewTail" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_DOT) && idnestNewTail())
			{
				System.out.println("idnestNew -> '.' idnestNewTail");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean idnestNewTail()
	{
		System.out.println("idnestNewTail");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER, Token.T_PUNCTUATION_OPEN_BRACKET }; 
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "indiceList" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) && indiceList())
			{
				System.out.println("idnestNewTail -> 'id' indiceList");
				return true;
			}
			return false;
		}
		
		if( checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) )
			{
				System.out.println("idnestNewTail -> '(' aParams ')'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean indice()
	{
		System.out.println("indice");
		int[] FIRST = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_SQ_BRACKET), "arithExpr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_SQ_BRACKET) && arithExpr() && match(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) )
			{
				System.out.println("indice -> '[' arithExpr ']'");
				return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean idnestList()
	{
		System.out.println("idnestList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		int[] FOLLOW = { Token.T_OPERATOR_EQUALS, Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "idnest", "idnestList" };
			d_Node.replace(values);
			if( idnest() && idnestList())
			{
				System.out.println("idnestList -> idnest idnestList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("idnestList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean idnest()
	{
		System.out.println("idnest");
		int[] FIRST = { Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_DOT), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "indiceList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_DOT) && match(Token.T_ELEMENT_IDENTIFIER) && indiceList())
			{
				System.out.println("idnest -> '.' 'id' indiceList");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arraySize()
	{
		System.out.println("arraySize");
		int[] FIRST = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_SQ_BRACKET), Token.typetoString(Token.T_ELEMENT_INTEGER), Token.typetoString(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_SQ_BRACKET) && match(Token.T_ELEMENT_INTEGER) && match(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) )
			{
				System.out.println("arraySize -> '[' 'intNum' ']'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean type()
	{
		System.out.println("type");
		int[] FIRST = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER }; 
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER };
		int[] FIRST2 = { Token.T_KEYWORD_FLOAT };
		int[] FIRST3 = { Token.T_ELEMENT_IDENTIFIER };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_INTEGER) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_INTEGER) )
			{
				System.out.println("type -> 'integer'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_FLOAT) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_FLOAT) )
			{
				System.out.println("type -> 'float'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST3))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER) };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER) )
			{
				System.out.println("type -> 'id' ");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean fParamsList()
	{
		System.out.println("fParamsList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT,  Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT,  Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "arraySizeList", "fParamsTailList" };
			d_Node.replace(values);
			if( type() && match(Token.T_ELEMENT_IDENTIFIER) && arraySizeList() && fParamsTailList() )
			{
				System.out.println("fParamsList -> type 'id' arraySizeList fParamsTailList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("fParamsList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean fParamsTailList()
	{
		System.out.println("fParamsTailList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		int[] FOLLOW = {  Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "fParamsTail", "fParamsTailList" };
			d_Node.replace(values);
			if( fParamsTail() && fParamsTailList() )
			{
				System.out.println("fParamsTailList	-> fParamsTail fParamsTailList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("fParamsTailList	-> ep");
			return true;
		}
		return false;
	}
	
	private boolean aParams()
	{
		System.out.println("aParams");
		int[] FIRST = { Token.T_EPSILON, Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT, Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT, Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "expr", "aParamsTailList" };
			d_Node.replace(values);
			if( expr() && aParamsTailList() )
			{
				System.out.println("aParams -> expr aParamsTailList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("aParams -> ep");
			return true;
		}
		return false;
	}
	
	private boolean  aParamsTailList()
	{
		System.out.println("aParamsTailList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "aParamsTail", "aParamsTailList" };
			d_Node.replace(values);
			if( aParamsTail() && aParamsTailList() )
			{
				System.out.println("aParamsTailList -> aParamsTail aParamsTailList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FOLLOW))
		{
			d_Node.delete();
			System.out.println("aParamsTailList -> ep");
			return true;
		}
		return false;
	}
	
	private boolean fParamsTail()
	{
		System.out.println("fParamsTail");
		int[] FIRST = { Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "arraySizeList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && type() && match(Token.T_ELEMENT_IDENTIFIER) && arraySizeList())
			{
				System.out.println("fParamsTail -> ',' type 'id' arraySizeList");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean aParamsTail()
	{
		System.out.println("aParamstail");
		int[] FIRST = { Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), "expr" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && expr() )
			{
				System.out.println("aParamsTail -> ',' expr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean assignOp()
	{
		System.out.println("assignOp");
		int[] FIRST = { Token.T_OPERATOR_EQUALS };
		int[] FIRST1 = { Token.T_OPERATOR_EQUALS };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_EQUALS) };
			d_Node.replace(values);
			if( match( Token.T_OPERATOR_EQUALS ) )
			{
				System.out.println("assignOp -> '='");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean relOp()
	{
		System.out.println("relOp");
		int[] FIRST = { Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		int[] FIRST1 = { Token.T_OPERATOR_EQUALTO };
		int[] FIRST2 = { Token.T_OPERATOR_NOT_EQUALS};
		int[] FIRST3 = { Token.T_OPERATOR_LESSERTHAN };
		int[] FIRST4 = { Token.T_OPERATOR_GREATERTHAN };
		int[] FIRST5 = { Token.T_OPERATOR_LESSTHAN_OR_EQUALS };
		int[] FIRST6 = { Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_EQUALS) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_EQUALS))
			{
				System.out.println("relOp -> 'eq'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_NOT_EQUALS) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_NOT_EQUALS))
			{
				System.out.println("relOp -> 'neq'");
				return true;
			}
			return false;
		}
		if(checkLHSin(FIRST3))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_LESSERTHAN) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_LESSERTHAN))
			{
				System.out.println("relOp -> 'lt'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST4))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_GREATERTHAN) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_GREATERTHAN))
			{
				System.out.println("relOp -> 'gt'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST5))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_LESSTHAN_OR_EQUALS) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_LESSTHAN_OR_EQUALS))
			{
				System.out.println("relOp -> 'leq'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST6))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_GREATERTHAN_OR_EQUALS) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_GREATERTHAN_OR_EQUALS))
			{
				System.out.println("relOp -> 'geq'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean addOp()
	{
		System.out.println("addOp");
		int[] FIRST = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR };
		int[] FIRST1 = { Token.T_OPERATOR_ADD };
		int[] FIRST2 = { Token.T_OPERATOR_SUBTRACT };
		int[] FIRST3 = { Token.T_OPERATOR_LOGICAL_OR };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_ADD) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_ADD))
			{
				System.out.println("addOp -> '+'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_SUBTRACT) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_SUBTRACT))
			{
				System.out.println("addOp -> '-'");
				return true;
			}
			return false;
		}
		if(checkLHSin(FIRST3))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_LOGICAL_OR) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_LOGICAL_OR))
			{
				System.out.println("addOp -> 'or'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean multOp()
	{
		System.out.println("multOp");
		int[] FIRST = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND };
		int[] FIRST1 = { Token.T_OPERATOR_MULTIPLY };
		int[] FIRST2 = { Token.T_OPERATOR_DIVIDE };
		int[] FIRST3 = { Token.T_OPERATOR_LOGICAL_AND };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_MULTIPLY) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_MULTIPLY))
			{
				System.out.println("multOp -> '*'");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_DIVIDE) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_DIVIDE))
			{
				System.out.println("multOp -> '/'");
				return true;
			}
			return false;
		}
		if(checkLHSin(FIRST3))
		{
			String[] values = { Token.typetoString(Token.T_OPERATOR_LOGICAL_AND) };
			d_Node.replace(values);
			if(match(Token.T_OPERATOR_LOGICAL_AND))
			{
				System.out.println("multOp -> 'and'");
				return true;
			}
			return false;
		}
		return false;

	}
	
	public void setLexicalAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
		lex_scanner = lexicalAnalyzer;
	}
	
	public void lockOutput()
	{
		syntaxError = false;
		d_Node.disable();
	}
	
	public int getErrorCount()
	{
		return m_errors;
	}
}
