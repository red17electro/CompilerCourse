package minijava.syntax;

import java_cup.runtime.*;
import java_cup.runtime.ComplexSymbolFactory.Location;
import static minijava.syntax.MiniJavaParserSym.*;
import minijava.syntax.MiniJavaParserSym;
import java.io.Reader;
import java.util.function.Consumer;

%%

/* -----------------Options and Declarations Section----------------- */

%public
%class Lexer

%unicode
%cup
%line
%column



// Code between %{ and %}, both of which must be at the beginning of a
// line, will be copied letter to letter into the lexer class source.
// Here you declare member variables and functions that are used inside
// scanner actions.
%{
    private ComplexSymbolFactory symbolFactory;

    public Lexer(ComplexSymbolFactory symbolFactory, Reader input){
	    this(input);
        this.symbolFactory = symbolFactory;
    }

    private Symbol symbol(int code){
        String name = MiniJavaParserSym.terminalNames[code];
        Location left = new Location(yyline+1,yycolumn+1-yylength());
        Location right = new Location(yyline+1,yycolumn+1);
	    return symbolFactory.newSymbol(name, code, left, right);
    }

    private Symbol symbol(int code, String lexem){
        String name = MiniJavaParserSym.terminalNames[code];
        Location left = new Location(yyline+1,yycolumn+1-yylength());
        Location right = new Location(yyline+1,yycolumn+1);
	    return symbolFactory.newSymbol(name, code, left, right, lexem);
    }

%}



// Macro Declarations:
// These declarations are regular expressions that will be used later
// in the Lexical Rules Section.

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Number = 0 | [1-9][0-9]*

IdentifierStart = [a-zA-Z]
IdentifierPart = {IdentifierStart} | [0-9_]
Identifier = {IdentifierStart} {IdentifierPart}*

Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment   = "/*"  ~ "*/"
EndOfLineComment     = "//" [^\r\n]* {LineTerminator}?

%%
/* ------------------------Lexical Rules Section---------------------- */


<YYINITIAL> {

    "abstract"         { return symbol(ABSTRACT); }
    "continue"         { return symbol(CONTINUE); }
    "for"              { return symbol(FOR); }
    "new"              { return symbol(NEW); }
    "switch"           { return symbol(SWITCH); }
    "assert"           { return symbol(ASSERT); }
    "default"          { return symbol(DEFAULT); }
    "if"               { return symbol(IF); }
    "package"          { return symbol(PACKAGE); }
    "synchronized"     { return symbol(SYNCHRONIZED); }
    "boolean"          { return symbol(BOOLEAN); }
    "do"               { return symbol(DO); }
    "goto"             { return symbol(GOTO); }
    "private"          { return symbol(PRIVATE); }
    "this"             { return symbol(THIS); }
    "break"            { return symbol(BREAK); }
    "double"           { return symbol(DOUBLE); }
    "implements"       { return symbol(IMPLEMENTS); }
    "protected"        { return symbol(PROTECTED); }
    "throw"            { return symbol(THROW); }
    "byte"             { return symbol(BYTE); }
    "else"             { return symbol(ELSE); }
    "import"           { return symbol(IMPORT); }
    "public"           { return symbol(PUBLIC); }
    "throws"           { return symbol(THROWS); }
    "case"             { return symbol(CASE); }
    "enum"             { return symbol(ENUM); }
    "instanceof"       { return symbol(INSTANCEOF); }
    "return"           { return symbol(RETURN); }
    "transient"        { return symbol(TRANSIENT); }
    "catch"            { return symbol(CATCH); }
    "extends"          { return symbol(EXTENDS); }
    "int"              { return symbol(INT); }
    "short"            { return symbol(SHORT); }
    "try"              { return symbol(TRY); }
    "char"             { return symbol(CHAR); }
    "final"            { return symbol(FINAL); }
    "interface"        { return symbol(INTERFACE); }
    "static"           { return symbol(STATIC); }
    "void"             { return symbol(VOID); }
    "class"            { return symbol(CLASS); }
    "finally"          { return symbol(FINALLY); }
    "long"             { return symbol(LONG); }
    "strictfp"         { return symbol(STRICTFP); }
    "volatile"         { return symbol(VOLATILE); }
    "const"            { return symbol(CONST); }
    "float"            { return symbol(FLOAT); }
    "native"           { return symbol(NATIVE); }
    "super"            { return symbol(SUPER); }
    "while"            { return symbol(WHILE); }

    "main"             { return symbol(MAIN); }
    "String"           { return symbol(STRING); }
    "length"           { return symbol(LENGTH); }
    "System"           { return symbol(SYSTEM); }
    "out"              { return symbol(OUT); }
    "println"          { return symbol(PRINTLN); }


    "{"                { return symbol(LBRACE); }
    "}"                { return symbol(RBRACE); }
    "["                { return symbol(LBRACKET); }
    "]"                { return symbol(RBRACKET); }
    "("                { return symbol(LPAREN); }
    ")"                { return symbol(RPAREN); }
    "."                { return symbol(DOT); }
    ";"                { return symbol(SEMI); }
    ","                { return symbol(COMMA); }
    "="                { return symbol(EQ); }
    "!"                { return symbol(NEG); }
    "&&"               { return symbol(AND); }
    "+"                { return symbol(PLUS); }
    "-"                { return symbol(MINUS); }
    "*"                { return symbol(TIMES); }
    "/"                { return symbol(DIV); }
    "<"                { return symbol(LESS); }
    "=="               { return symbol(EQUALS); }

    "true"             { return symbol(TRUE); }
    "false"            { return symbol(FALSE); }
    "null"             { return symbol(NULL); }



    {Number}           { return symbol(NUMBER, yytext()); }
    {Identifier}       { return symbol(ID, yytext()); }
    {WhiteSpace}       { /* skip whitespace */ }
    {Comment}          { /* comment */ }
}


/* All unmatched inputs produce an error: */
[^]                    { return symbol(INVALID_TOKEN, yytext()); }
<<EOF>>                { return symbol(EOF); }
