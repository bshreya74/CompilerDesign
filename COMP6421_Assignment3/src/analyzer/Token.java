package analyzer;

public class Token 
{
	private int type;
	private String value;
	private boolean hasError;
	
	private int numCol;
	private int numLine;
	
	public final static int T_ELEMENT_IDENTIFIER = 0;
	public final static int T_ELEMENT_INTEGER = 1;
	public final static int T_ELEMENT_FLOAT = 2;
	public final static int T_ELEMENT_UNKNOWN = 3;
	public final static int T_KEYWORD_IF = 4;
	public final static int T_KEYWORD_THEN = 5;
	public final static int T_KEYWORD_ELSE = 6;
	public final static int T_KEYWORD_FOR = 7;
	public final static int T_KEYWORD_CLASS = 8;
	public final static int T_KEYWORD_INTEGER = 9;
	public final static int T_KEYWORD_FLOAT = 10;
	public final static int T_KEYWORD_READ = 11;
	public final static int T_KEYWORD_WRITE = 12;
	public final static int T_KEYWORD_RETURN = 13;
	public final static int T_KEYWORD_MAIN = 14;
	public final static int T_OPERATOR_EQUALTO = 15;
	public final static int T_OPERATOR_GREATER_OR_LESSER = 16;
	public final static int T_OPERATOR_LESSERTHAN = 17;
	public final static int T_OPERATOR_GREATERTHAN = 18;
	public final static int T_OPERATOR_LESSTHAN_OR_EQUALS = 19;
	public final static int T_OPERATOR_GREATERTHAN_OR_EQUALS = 20;
	public final static int T_OPERATOR_ADD = 21;
	public final static int T_OPERATOR_SUBTRACT = 22;
	public final static int T_OPERATOR_MULTIPLY = 23;
	public final static int T_OPERATOR_DIVIDE = 24;
	public final static int T_OPERATOR_EQUALS = 25;
	public final static int T_OPERATOR_LOGICAL_AND = 26;
	public final static int T_OPERATOR_LOGICAL_OR = 27;
	public final static int T_OPERATOR_LOGICAL_NOT = 28;
	public final static int T_PUNCTUATION_SEMICOLON = 29;
	public final static int T_PUNCTUATION_COMMA = 30;
	public final static int T_PUNCTUATION_DOT = 31;
	public final static int T_PUNCTUATION_COLON = 32;
	public final static int T_PUNCTUATION_DOUBLE_COLON = 33;
	public final static int T_PUNCTUATION_OPEN_BRACKET = 34;
	public final static int T_PUNCTUATION_CLOSE_BRACKET = 35;
	public final static int T_PUNCTUATION_OPEN_PARANTHESIS = 36;
	public final static int T_PUNCTUATION_CLOSE_PARANTHESIS = 37;
	public final static int T_PUNCTUATION_OPEN_SQ_BRACKET = 38;
	public final static int T_PUNCTUATION_CLOSE_SQ_BRACKET = 39;
	public final static int T_PUNCTUATION_OPEN_MULTI_COMMENT = 40;
	public final static int T_PUNCTUATION_CLOSE_MULTI_COMMENT = 41;
	public final static int T_PUNCTUATION_COMMENT = 42;
	public final static int T_EOF = 43;
	public final static int T_EPSILON = 44;
	public final static int T_PROGRAM = 45;
	public final static int T_OPERATOR_NOT_EQUALS = 46;
	
	public Token(int type, String value, boolean hasError, int numLine, int numCol)
	{
		this.type = type;
		this.value = value;
		this.hasError = hasError;
		this.numLine = numLine;
		this.numCol = numCol;
	}
	
	public static String typetoString (int type)
	{
		switch(type)
		{
			case T_ELEMENT_IDENTIFIER : return "'id'" ;
			case T_ELEMENT_INTEGER : return "'intNum'";
			case T_ELEMENT_FLOAT : return "'floatNum'";
			case T_ELEMENT_UNKNOWN : return "unknown";
			case T_KEYWORD_IF : return "'if'";
			case T_KEYWORD_THEN : return "'then'";
			case T_KEYWORD_ELSE : return "'else'";
			case T_KEYWORD_FOR : return "'for'";
			case T_KEYWORD_CLASS : return "'class'";
			case T_KEYWORD_INTEGER : return "'integer'";
			case T_KEYWORD_FLOAT : return "'float'";
			case T_KEYWORD_READ : return "'read'";
			case T_KEYWORD_WRITE : return "'write'";
			case T_KEYWORD_RETURN : return "'return'";
			case T_KEYWORD_MAIN : return "'main'";
			case T_OPERATOR_EQUALTO : return "'=='";
			case T_OPERATOR_GREATER_OR_LESSER : return "'<>'";
			case T_OPERATOR_LESSERTHAN : return "'<'";
			case T_OPERATOR_GREATERTHAN : return "'>'";
			case T_OPERATOR_LESSTHAN_OR_EQUALS : return "'<='";
			case T_OPERATOR_GREATERTHAN_OR_EQUALS : return "'>='";
			case T_OPERATOR_ADD : return "'+'";
			case T_OPERATOR_SUBTRACT : return "'-'";
			case T_OPERATOR_MULTIPLY : return "'*'";
			case T_OPERATOR_DIVIDE : return "'/'";
			case T_OPERATOR_EQUALS : return "'='";
			case T_OPERATOR_NOT_EQUALS : return "'!='";
			case T_OPERATOR_LOGICAL_AND : return "'&&'";
			case T_OPERATOR_LOGICAL_OR : return "'||'";
			case T_OPERATOR_LOGICAL_NOT : return "'!'";
			case T_PUNCTUATION_SEMICOLON : return "';'";
			case T_PUNCTUATION_COMMA : return "','";
			case T_PUNCTUATION_DOT : return "'.'";
			case T_PUNCTUATION_COLON : return "':'";
			case T_PUNCTUATION_DOUBLE_COLON : return "'::'";
			case T_PUNCTUATION_OPEN_BRACKET : return "'('";
			case T_PUNCTUATION_CLOSE_BRACKET : return "')'";
			case T_PUNCTUATION_OPEN_PARANTHESIS : return "'{'";
			case T_PUNCTUATION_CLOSE_PARANTHESIS : return "'}'";
			case T_PUNCTUATION_OPEN_SQ_BRACKET : return "'['";
			case T_PUNCTUATION_CLOSE_SQ_BRACKET : return "']'";
			case T_PUNCTUATION_OPEN_MULTI_COMMENT : return "'/*'";
			case T_PUNCTUATION_CLOSE_MULTI_COMMENT : return "'*/'";
			case T_PUNCTUATION_COMMENT : return "'//'";
			case T_EOF : return "End of file";
			case T_PROGRAM : return "program";
			
			default :
			{
				System.out.println("Invalid Token "+ type );
				return "Invalid";
			}
		}
	}
	
	public int getType()
	{
		return type;
	}
	
	 public String getValue()
	 {
		 return value;
	 }
	
	 public int getNumLine()
	 {
		 return numLine;
	 }
	 
	 public int getNumCol()
	 {
		 return numCol;
	 }
	 
	 public String toString()
	 {
		 String errString = "";
			if (hasError == true) 
			{
				errString = " ERROR";
			}
			
			return "<" + '(' + Integer.toString(numLine) + ',' + Integer.toString(numCol) + ')' + ' ' + typetoString(type) + " '" + value + "'" + errString + ">";
	 }
}