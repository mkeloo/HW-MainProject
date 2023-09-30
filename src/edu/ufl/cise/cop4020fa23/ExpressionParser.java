/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;

import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

/*

Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z 
    ExpandedPixel  
ChannelSelector ::= : red | : green | : blue
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken token;



	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		token = lexer.next();
	}


	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		return e;
	}


	/* *****************************  MOKSH ***************************** */

	// match the expected kind and move to the next token
	private void match(Kind expectedKind) throws LexicalException, SyntaxException {
		if (token.kind() == expectedKind) {
			try {
				token = lexer.next();
			} catch (LexicalException e) {
				throw new LexicalException(token.sourceLocation(), "Lexical error while trying to match " + expectedKind);
			}
		} else {
			throw new SyntaxException(token.sourceLocation(), "Expected " + expectedKind + " but found " + token.kind());
		}
	}

	// Expr ::=  ConditionalExpr | LogicalOrExpr
	private Expr expr() throws PLCCompilerException {
		if (token.kind() == Kind.QUESTION) {
			return conditionalExpr();
		} else {
			return logicalOrExpr();
		}
	}


	// ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr
	private ConditionalExpr conditionalExpr() throws PLCCompilerException {
		IToken firstToken = token;
		match(Kind.QUESTION);
		Expr condition = expr();
		match(Kind.RARROW);
		Expr trueExpr = expr();
		match(Kind.COMMA);
		Expr falseExpr = expr();
		return new ConditionalExpr(firstToken, condition, trueExpr, falseExpr);
	}

	// LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*



	// LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*




	/* *****************************  Daniel  ***************************** */

	// ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*


	// PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr


	// AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*


	// MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*


	// UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix


	/* *****************************  Moksh  ***************************** */

	// UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )


    // PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z


	/* *****************************  Daniel  ***************************** */

	// PixelSelector  ::= [ Expr , Expr ]


	// ChannelSelector ::= : red | : green | : blue


	// ExpandedPixel ::= [ Expr , Expr , Expr ]


}
