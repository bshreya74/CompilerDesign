package analyzer;

import java.io.*;
import java.util.*;

public class SyntacticAnalyzer 
{
	private LexicalAnalyzer lex_scanner;
	private Derivation d_Node;
	private OutputStream derivationOutput;
	private OutputStream errorOutput;
	private Token lookahead;
	SemAnalyzer sem;
	private boolean syntaxError;
	private int m_errors;
	public String filename;
	
	SyntacticAnalyzer(String in, OutputStream deriv_output, OutputStream err_output, String SemAnalyzer, String semcheck)
	{
		filename = in;
		errorOutput = err_output;
		derivationOutput = deriv_output;
		syntaxError = true;
		m_errors =0;
		sem = new SemAnalyzer(SemAnalyzer, semcheck);
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
		boolean success = false;
		if(d_Node == null)
		{
			d_Node = new Derivation("prog", derivationOutput);
		}
		lookahead = lex_scanner.nextToken();
		if(startSymbol() && match(Token.T_EOF))
		{
			//System.out.println("in Parse got EOF");
			for(int a = 0; a < sem.tables.size(); a++)
			{
				sem.print(sem.tables.get(a).getTableName());
			}
			return true;
		}
		else
		{
			for(int a = 0; a < sem.tables.size(); a++)
			{
				sem.print(sem.tables.get(a).getTableName());
			}
			return false;
		}
		//lookahead = lex_scanner.nextToken();
		
		//System.out.println("lookahead = " + lookahead);
		//return false;
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
	
	private boolean match(int token, SemRec r)
	{
		d_Node.move();
		
		if(lookahead.getType() == Token.T_ELEMENT_INTEGER || lookahead.getType() == Token.T_ELEMENT_FLOAT || lookahead.getType() == Token.T_ELEMENT_IDENTIFIER)
		{
			if(lookahead.getType() == token)
			{
				if(lookahead.getType() == Token.T_ELEMENT_IDENTIFIER)
				{
					r.record = lookahead.getValue();
				}
				if(lookahead.getType() == Token.T_ELEMENT_INTEGER)
				{
					r.array.add(lookahead.getType());
				}
				lookahead = lex_scanner.nextToken();
				return true;
			}
			else
			{
				//TODO: Error
				String errorMessage = "Expected " + Token.typetoString(token) + " got " + Token.typetoString(lookahead.getType()) + " instead";
				outputError(lookahead.getNumLine(), lookahead.getNumCol(), errorMessage);
				lookahead = lex_scanner.nextToken();
				return false;
			}
		}
		else
		{
			if(lookahead.getType() == token)
			{
				r.record = lookahead.getValue();
				lookahead = lex_scanner.nextToken();
				return true;
			}
			else
			{
				//TODO Error Output
				String errorMessage = "Expected " + Token.typetoString(token) + " got " + Token.typetoString(lookahead.getType()) + " instead";
				outputError(lookahead.getNumLine(), lookahead.getNumCol(), errorMessage);
				lookahead = lex_scanner.nextToken();
				return false;
			}	
		}
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
		//System.out.println("In skipErrors");
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
		
		sem.create("Global");
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "classDeclList", "funcDefList", Token.typetoString(Token.T_KEYWORD_MAIN), "funcBody", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if(classDeclList() && funcDefList() && match(Token.T_KEYWORD_MAIN) && sem.addEntry("Global", "main", "function", null, "main", null) && sem.createTable("Global:main") && funcBody("main", "Global") && match(Token.T_PUNCTUATION_SEMICOLON))
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
		
		SemRec className = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = {Token.typetoString(Token.T_KEYWORD_CLASS), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "inheritedList", Token.typetoString(Token.T_PUNCTUATION_OPEN_PARANTHESIS), "memberList", Token.typetoString(Token.T_PUNCTUATION_CLOSE_PARANTHESIS), Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_CLASS) && match(Token.T_ELEMENT_IDENTIFIER, className)&& sem.addEntry("Global", className.record, "class", null, className.record, null) && sem.createTable(className.record)&& sem.classCheck(className.record) && inheritedList() && match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && memberList(className) && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) && match(Token.T_PUNCTUATION_SEMICOLON))
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
		
		SemRec name = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = {Token.typetoString(Token.T_PUNCTUATION_COLON), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "idInClassDecList"  };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COLON) && match(Token.T_ELEMENT_IDENTIFIER, name) && idInClassDeclList())
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
		
		SemRec name = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "idInClassDeclList"  };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && match(Token.T_ELEMENT_IDENTIFIER, name) && idInClassDeclList())
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
	
	private boolean memberList(SemRec className)
	{
		System.out.println("memberList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec type = new SemRec();
		SemRec varName = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "memberListNew" };
			d_Node.replace(values);
			if( type(type) && match(Token.T_ELEMENT_IDENTIFIER, varName) && memberListNew(className, type, varName))
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
	
	private boolean memberListNew(SemRec className, SemRec type, SemRec varName)
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
		
		SemRec var_dimension = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "varDecl", "memberList"  };
			d_Node.replace(values);
			if( varDecl(var_dimension) && sem.addEntry(className.record, varName.record, "variable", type.record + ":"+var_dimension.array, null, "Global") && sem.varCheck(className.record, varName.record) && sem.typeCheck(className.record, type.record) && memberList(className))
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
			if(sem.createTable(className.record+":"+varName.record) && sem.addEntry(className.record, varName.record, "function", type.record, className.record+":"+varName.record, "Global")&& sem.funCheck(className.record, varName.record) && funcDecl(varName.record, className.record) && funcInClass(className))
			{
				System.out.println("memberListNew -> funcDecl funcInClass");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcInClass(SemRec className)
	{
		System.out.println("funcInClass");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT, Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW ={ Token.T_PUNCTUATION_CLOSE_PARANTHESIS };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec type = new SemRec();
		SemRec varName = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "funcInClassNew" };
			d_Node.replace(values);
			if(type(type) && match(Token.T_ELEMENT_IDENTIFIER, varName) && funcInClassNew(className, type, varName))
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
	
	private boolean funcInClassNew(SemRec className, SemRec type, SemRec varName)
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
			if(funcDecl(varName.record, className.record) && funcInClass(className))
			{
				System.out.println("funcInClassNew -> funcDecl funcInClass");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcDecl(String functionName, String className)
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
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && fParamsList(functionName, className) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("funcDecl -> '(' fParamsList ')' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcHead(SemRec className, SemRec funcName, SemRec funcType)
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
			if( type(funcType) && funcHeadId(className, funcName) && sem.createTable("Global:"+funcName.record) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && fParamsList("Global", funcName.record ) && match(Token.T_PUNCTUATION_CLOSE_BRACKET))
			{
				System.out.println("funcHead -> type funcHeadId '(' fParamsList ')'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcHeadId(SemRec className, SemRec funcName)
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
			if( match(Token.T_ELEMENT_IDENTIFIER, funcName) && scopeSpec(funcName))
			{
				System.out.println("funcHeadId -> 'id' scopeSpec");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean scopeSpec(SemRec funcName)
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
			if( match(Token.T_PUNCTUATION_DOUBLE_COLON) && match(Token.T_ELEMENT_IDENTIFIER, funcName))
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
		SemRec funcName = new SemRec();
		SemRec funcType = new SemRec();
		SemRec className = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "funcHead", "funcBody", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( funcHead(className, funcName, funcType) && sem.addEntry("Global", funcName.record, "function", funcType.record, funcName.record, null) && sem.funCheck("Global", funcName.record) && funcBody(funcName.record, "Global") && match(Token.T_PUNCTUATION_SEMICOLON))
			{
				System.out.println("funcDef -> funcHead funcBody ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean funcBody(String tableName, String className)
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
			if( match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && varStatinFunc(tableName, className) && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS))
			{
				System.out.println("funcBody -> '{' varStatinFunc '}'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatinFunc(String tableName, String className)
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
		
		SemRec type = new SemRec();
		SemRec id = new SemRec();
		SemRec dimension = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "varStat", "varStatinFunc" };
			d_Node.replace(values);
			if( varStat(type, id, dimension) && sem.addEntry(className+":"+tableName, id.record, "variable", type.record + ":"+ dimension.array, null, className) && sem.varCheck(className + ":" + tableName, id.record) && sem.typeCheck(className+":"+tableName, type.record) && varStatinFunc(tableName, className))
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
			if( varStatNew(id, tableName, className) && varStatinFuncNew(id, tableName, className))
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
	
	private boolean varStatinFuncNew(SemRec id, String tableName, String className)
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
			if(varStatNew(id, tableName, className) && varStatinFuncNew(id, tableName, className))
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
	
	private boolean varStat(SemRec type, SemRec id, SemRec dimension)
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
			if( typeNew(type) && match(Token.T_ELEMENT_IDENTIFIER, id) && varDecl(dimension))
			{
				System.out.println("varStat	-> typeNew 'id' varDecl");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatNew(SemRec id, String tableName, String className)
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
			if( match(Token.T_ELEMENT_IDENTIFIER, id) && sem.varCheck(className+":"+tableName, id.record) && varStatTail(id, tableName, className))
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
			if(statementOther(tableName, className))
			{
				System.out.println("varStatNew -> statementOther");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varStatTail(SemRec id, String tableName, String className)
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
		
		SemRec idNew = new SemRec();
		SemRec dimension = new SemRec();
		SemRec right_type = new SemRec();
		SemRec name = new SemRec();
		SemRec type = new SemRec();
		ArrayList<SemRec> list = new ArrayList<SemRec>();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "varDecl" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER, idNew) && varDecl(dimension) && sem.addEntry(className+":"+tableName, idNew.record, "variable", type.record + ":"+ dimension.array, null, className) && sem.varCheck(className + ":" + tableName, idNew.record) && sem.typeCheck(className+":"+tableName, type.record))
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
			if( indiceList(name, type) && idnestList(type) && sem.varCheck(className+":"+tableName, name.record) && assignStatTail(right_type) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams(list) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && idnestList(type) && assignStatTail(right_type) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("varStatTail	-> '(' aParams ')' idnestList assignStatTail ';'");
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	private boolean assignStatTail(SemRec type)
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
			if( assignOp() && expr(type) )
			{
				System.out.println("assignStatTail -> assignOp expr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean typeNew(SemRec type)
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
			if( match(Token.T_KEYWORD_FLOAT, type) )
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
			if( match(Token.T_KEYWORD_INTEGER, type) )
			{
				System.out.println("typeNew	-> 'integer'");
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	private boolean statementOther(String tableName, String className)
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
		
		SemRec name = new SemRec();
		SemRec type = new SemRec();
		SemRec right_type = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_KEYWORD_IF), Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "expr", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), Token.typetoString(Token.T_KEYWORD_THEN), "statBlock", Token.typetoString(Token.T_KEYWORD_ELSE), "statBlock", Token.typetoString(Token.T_PUNCTUATION_SEMICOLON) };
			d_Node.replace(values);
			if( match(Token.T_KEYWORD_IF) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_KEYWORD_THEN) && statBlock(tableName, className) && match(Token.T_KEYWORD_ELSE) && statBlock(tableName, className) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_FOR) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && type(type) && match(Token.T_ELEMENT_IDENTIFIER, name) && sem.addEntry(className+":"+tableName, name.record, "variable", type.record, null, "Global") && sem.varCheck(className+":"+tableName, name.record) && assignOp() && expr(right_type) && match(Token.T_PUNCTUATION_SEMICOLON) && relExpr() && match(Token.T_PUNCTUATION_SEMICOLON) && assignStat() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && statBlock(tableName,className) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_READ) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && variable(name, type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_WRITE) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_RETURN) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("statementOther -> 'return' '(' expr ')' ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean statementList(String tableName, String className)
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
			if( statement(tableName, className) && statementList(tableName, className) )
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
	
	private boolean varDecl(SemRec var_dimension)
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
			if( arraySizeList(var_dimension) && match(Token.T_PUNCTUATION_SEMICOLON) )
			{
				System.out.println("varDecl -> arraySizeList ';'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arraySizeList(SemRec param_dimension)
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
			if( arraySize(param_dimension) && arraySizeList(param_dimension) )
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
	
	private boolean statement(String tableName, String className)
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
		
		SemRec name = new SemRec();
		SemRec type = new SemRec();
		SemRec right_type = new SemRec();
		
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
			if( match(Token.T_KEYWORD_IF) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_KEYWORD_THEN) && statBlock(tableName, className) && match(Token.T_KEYWORD_ELSE) && statBlock(tableName, className) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_FOR) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && type(type) && match(Token.T_ELEMENT_IDENTIFIER, name) && sem.addEntry(className+":"+tableName, name.record, "variable", type.record, null, "Global") && sem.varCheck(className+":"+tableName, name.record) && assignOp() && expr(right_type) && match(Token.T_PUNCTUATION_SEMICOLON) && relExpr() && match(Token.T_PUNCTUATION_SEMICOLON) && assignStat() && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && statBlock(tableName, className) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_READ) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && variable(name, type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_WRITE) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
			if( match(Token.T_KEYWORD_RETURN) && match(Token.T_PUNCTUATION_OPEN_BRACKET) && expr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && match(Token.T_PUNCTUATION_SEMICOLON) )
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
		
		SemRec name = new SemRec();
		SemRec type = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "assignOp", "expr" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER, name) && assignOp() && expr(type) )
			{
				System.out.println("assignStat -> 'id' assignOp expr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean statBlock(String tableName, String className)
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
			if( match(Token.T_PUNCTUATION_OPEN_PARANTHESIS) && statementList(tableName, className) && match(Token.T_PUNCTUATION_CLOSE_PARANTHESIS) )
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
			if(statement(tableName, className))
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

	private boolean expr(SemRec type)
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
			if( arithExpr(type) && exprNew(type))
			{
				System.out.println("expr -> arithExpr exprNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean exprNew(SemRec type)
	{
		System.out.println("exprNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		int[] FIRST1 = { Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS };
		int[] FOLLOW = { Token.T_PUNCTUATION_COMMA, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_PUNCTUATION_SEMICOLON };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec right_type = new SemRec();
		
		if( checkLHSin(FIRST1))
		{
			String[] values = { "relOp", "arithExpr" };
			d_Node.replace(values);
			if( relOp() && arithExpr(right_type) )
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
		
		SemRec left_type = new SemRec();
		SemRec right_type = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "arithExpr", "relOp", "arithExpr" };
			d_Node.replace(values);
			if( arithExpr(left_type) && relOp() && arithExpr(right_type) )
			{
				System.out.println("relExpr -> arithExpr relOp arithExpr");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arithExpr(SemRec type)
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
			if( term(type) && arithExprNew(type) )
			{
				System.out.println("arithExpr -> term arithExprNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arithExprNew(SemRec type)
	{
		System.out.println("arithExprNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR };
		int[] FIRST1 = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec right_type = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "addOp", "term", "arithExprNew" };
			d_Node.replace(values);
			if( addOp() && term(right_type) && arithExprNew(right_type) )
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
	
	private boolean term(SemRec type)
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
			if(factor(type) && termNew(type))
			{
				System.out.println("term -> factor termNew");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean termNew(SemRec type)
	{
		System.out.println("termNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND };
		int[] FIRST1 = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND };
		int[] FOLLOW = { Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec right_type = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "multOp", "factor", "termNew" };
			d_Node.replace(values);
			if( multOp() && factor(right_type) && termNew(right_type) )
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
	
	private boolean factor(SemRec type)
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
		
		SemRec name = new SemRec();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { "varFunc" };
			d_Node.replace(values);
			if(varFunc(name, type))
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
			if( match(Token.T_ELEMENT_INTEGER, type) )
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
			if( match(Token.T_ELEMENT_FLOAT, type) )
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
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && arithExpr(type) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) )
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
			if( match(Token.T_OPERATOR_LOGICAL_NOT) && factor(type) )
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
			if( sign() && factor(type) )
			{
				System.out.println("factor -> sign factor");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean variable(SemRec name, SemRec type)
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
			if( match(Token.T_ELEMENT_IDENTIFIER, name) && variableNew(name, type) )
			{
				System.out.println("variable -> 'id' variableNew  ");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean variableNew(SemRec name, SemRec type)
	{
		System.out.println("variableNew");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET, Token.T_PUNCTUATION_DOT, Token.T_PUNCTUATION_OPEN_BRACKET};
		int[] FIRST1 = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_BRACKET};
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		ArrayList<SemRec> list = new ArrayList<>();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "indiceList", "idnestList" };
			d_Node.replace(values);
			if( indiceList(name, type) && idnestList(type) )
			{
				System.out.println("variableNew	-> indiceList idnestList");
				return true;
			}
			return false;
		}
		
		if(checkLHSin(FIRST2))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "idnestList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams(list) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && idnestList(type) )
			{
				System.out.println("variableNew	-> '(' aParams ')' idnestList");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varFunc(SemRec name, SemRec type)
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
			if( match(Token.T_ELEMENT_IDENTIFIER, name) && indiceList(name, type) && idnestListNew(type) && varFuncTail(name, type) )
			{
				System.out.println("varFunc -> 'id' indiceList idnestListNew varFuncTail");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean varFuncTail(SemRec name, SemRec type)
	{
		System.out.println("varFuncTail");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		int[] FOLLOW = { Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		ArrayList<SemRec> list = new ArrayList<SemRec>();
		
		if( checkLHSin(FIRST1) )
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_OPEN_BRACKET), "aParams", Token.typetoString(Token.T_PUNCTUATION_CLOSE_BRACKET), "varFuncTail2" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams(list) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) && varFuncTail2(type) )
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
	
	private boolean varFuncTail2(SemRec type)
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
			if(idnest(type))
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
	
	private boolean idnestListNew(SemRec type)
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
			if(idnestNew(type) && idnestListNew(type))
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
	
	private boolean indiceList(SemRec name, SemRec type)
	{
		System.out.println("indiceList");
		int[] FIRST = { Token.T_EPSILON, Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FIRST1 = { Token.T_PUNCTUATION_OPEN_SQ_BRACKET };
		int[] FOLLOW = { Token.T_PUNCTUATION_DOT, Token.T_OPERATOR_EQUALS, Token.T_PUNCTUATION_SEMICOLON, Token.T_OPERATOR_MULTIPLY, Token.T_OPERATOR_DIVIDE, Token.T_OPERATOR_LOGICAL_AND, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT, Token.T_OPERATOR_LOGICAL_OR, Token.T_PUNCTUATION_CLOSE_SQ_BRACKET, Token.T_PUNCTUATION_CLOSE_BRACKET, Token.T_OPERATOR_EQUALTO, Token.T_OPERATOR_NOT_EQUALS, Token.T_OPERATOR_LESSERTHAN, Token.T_OPERATOR_GREATERTHAN, Token.T_OPERATOR_LESSTHAN_OR_EQUALS, Token.T_OPERATOR_GREATERTHAN_OR_EQUALS, Token.T_PUNCTUATION_COMMA, Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec index_type = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "indice", "indiceList" };
			d_Node.replace(values);
			if( indice(index_type) && indiceList(name, type))
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
	
	private boolean idnestNew(SemRec type)
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
			if( match(Token.T_PUNCTUATION_DOT) && idnestNewTail(type))
			{
				System.out.println("idnestNew -> '.' idnestNewTail");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean idnestNewTail(SemRec type)
	{
		System.out.println("idnestNewTail");
		int[] FIRST = { Token.T_ELEMENT_IDENTIFIER, Token.T_PUNCTUATION_OPEN_BRACKET }; 
		int[] FIRST1 = { Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST2 = { Token.T_PUNCTUATION_OPEN_BRACKET };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		SemRec name = new SemRec();
		ArrayList<SemRec> list = new ArrayList<SemRec>();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "indiceList" };
			d_Node.replace(values);
			if( match(Token.T_ELEMENT_IDENTIFIER, name) && indiceList(name, type))
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
			if( match(Token.T_PUNCTUATION_OPEN_BRACKET) && aParams(list) && match(Token.T_PUNCTUATION_CLOSE_BRACKET) )
			{
				System.out.println("idnestNewTail -> '(' aParams ')'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean indice(SemRec index_type)
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
			if( match(Token.T_PUNCTUATION_OPEN_SQ_BRACKET) && arithExpr(index_type) && match(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) )
			{
				System.out.println("indice -> '[' arithExpr ']'");
				return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean idnestList(SemRec type)
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
			if( idnest(type) && idnestList(type))
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
	
	private boolean idnest(SemRec type)
	{
		System.out.println("idnest");
		int[] FIRST = { Token.T_PUNCTUATION_DOT };
		int[] FIRST1 = { Token.T_PUNCTUATION_DOT };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		SemRec name = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_DOT), Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "indiceList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_DOT) && match(Token.T_ELEMENT_IDENTIFIER, name) && indiceList(name, type))
			{
				System.out.println("idnest -> '.' 'id' indiceList");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean arraySize(SemRec param_dimension)
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
			if( match(Token.T_PUNCTUATION_OPEN_SQ_BRACKET) && match(Token.T_ELEMENT_INTEGER, param_dimension) && match(Token.T_PUNCTUATION_CLOSE_SQ_BRACKET) )
			{
				System.out.println("arraySize -> '[' 'intNum' ']'");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean type(SemRec type)
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
			if( match(Token.T_KEYWORD_INTEGER, type) )
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
			if( match(Token.T_KEYWORD_FLOAT, type) )
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
			if( match(Token.T_ELEMENT_IDENTIFIER, type) )
			{
				System.out.println("type -> 'id' ");
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean fParamsList(String functionName, String className)
	{
		System.out.println("fParamsList");
		int[] FIRST = { Token.T_EPSILON, Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT,  Token.T_ELEMENT_IDENTIFIER };
		int[] FIRST1 = { Token.T_KEYWORD_INTEGER, Token.T_KEYWORD_FLOAT,  Token.T_ELEMENT_IDENTIFIER };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec paramName = new SemRec();
		SemRec paramType = new SemRec();
		SemRec param_dimension = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "arraySizeList", "fParamsTailList" };
			d_Node.replace(values);
			if( type(paramType) && match(Token.T_ELEMENT_IDENTIFIER, paramName) && arraySizeList(param_dimension) && sem.addEntry(className+":"+functionName, paramName.record, "parameter", paramType.record+":"+param_dimension.array, null, className) && sem.paraCheck(className+":"+functionName, paramName.record) && sem.typeCheck(className+":"+functionName, paramType.record) && fParamsTailList(functionName, className) )
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
	
	private boolean fParamsTailList(String functionName, String className)
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
			if( fParamsTail(functionName, className) && fParamsTailList(functionName, className) )
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
	
	private boolean aParams(ArrayList<SemRec> a)
	{
		System.out.println("aParams");
		int[] FIRST = { Token.T_EPSILON, Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT, Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FIRST1 = { Token.T_ELEMENT_INTEGER, Token.T_ELEMENT_FLOAT, Token.T_PUNCTUATION_OPEN_BRACKET, Token.T_OPERATOR_LOGICAL_NOT, Token.T_ELEMENT_IDENTIFIER, Token.T_OPERATOR_ADD, Token.T_OPERATOR_SUBTRACT };
		int[] FOLLOW = { Token.T_PUNCTUATION_CLOSE_BRACKET };
		
		if(!skipErrors(FIRST, FOLLOW))
		{
			return false;
		}
		
		SemRec type = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { "expr", "aParamsTailList" };
			d_Node.replace(values);
			if( expr(type) && a.add(type) && aParamsTailList(a) )
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
	
	private boolean  aParamsTailList(ArrayList<SemRec> a)
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
			if( aParamsTail() && aParamsTailList(a) )
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
	
	private boolean fParamsTail(String functionName, String className)
	{
		System.out.println("fParamsTail");
		int[] FIRST = { Token.T_PUNCTUATION_COMMA };
		int[] FIRST1 = { Token.T_PUNCTUATION_COMMA };
		
		if(!skipErrors(FIRST, null))
		{
			return false;
		}
		
		SemRec paramName = new SemRec();
		SemRec paramType = new SemRec();
		SemRec param_dimension = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), "type", Token.typetoString(Token.T_ELEMENT_IDENTIFIER), "arraySizeList" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && type(paramType) && match(Token.T_ELEMENT_IDENTIFIER, paramName) && arraySizeList(param_dimension) && sem.addEntry(className+":"+functionName, paramName.record, "parameter", paramType.record+":"+param_dimension.array, null, className) && sem.paraCheck(className+":"+functionName, paramName.record) && sem.typeCheck(className+":"+functionName, paramType.record))
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
		
		SemRec type = new SemRec();
		
		if(checkLHSin(FIRST1))
		{
			String[] values = { Token.typetoString(Token.T_PUNCTUATION_COMMA), "expr" };
			d_Node.replace(values);
			if( match(Token.T_PUNCTUATION_COMMA) && expr(type) )
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
