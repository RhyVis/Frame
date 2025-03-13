grammar graph;

// Parser Rules
program
    : name
      statement*
      EOF
    ;

statement
    : reference
    | callGraph
    | jumpMark
    | jumpStatement
    | variableDeclaration
    | variableAssignment
    | objectDeclaration
    | conditionalStatement
    | loopStatement
    | loopBreakStatement
    | loopContinueStatement
    | functionDeclaration
    | globalFunctionDeclaration
    | systemCall
    | functionCall
    | objectMethodCall
    | returnStatement
    | terminateStatement
    | NEWLINE
    ;

name
    : '$' WS* ID NEWLINE
    ;

reference
    : '&[' ID ']' ';'? NEWLINE?
    ;

callGraph
    : '&$(' ID ')' ';'? NEWLINE?
    ;

jumpMark
    : '%[' ID ']' NEWLINE
    ;

jumpStatement
    : '>[' ID '](' expression ')' ';'? NEWLINE?
    ;

variableDeclaration
    : '*' ID ':' TYPE ('=' expression)? ';'? NEWLINE?
    ;

variableAssignment
    : fieldAccess '=' expression ';'? NEWLINE?
    ;

fieldAccess
    : ID ('.' ID)?
    ;

objectDeclaration
    : ':' ID '{' NEWLINE?
      (variableDeclaration NEWLINE?)*
      (functionDeclaration NEWLINE?)*
      '}' NEWLINE?
    ;

conditionalStatement
    : '?(' expression ')' codeBlock ('?(' expression ')' codeBlock)*
    ;

loopStatement
    : '~(' expression ')' codeBlock
    ;

loopBreakStatement
    : '~!!' ';'? NEWLINE?
    ;

loopContinueStatement
    : '~>' ';'? NEWLINE?
    ;

codeBlock
    : '{' NEWLINE?
      statement*
      '}' NEWLINE?
    ;

returnStatement
    : '::' expression? ';'? NEWLINE?
    ;

functionDeclaration
    : '@' ID '(' paramList? ')' ('->' TYPE)? codeBlock
    ;

globalFunctionDeclaration
    : '@$' ID '(' paramList? ')' ('->' TYPE)? codeBlock
    ;

paramList
    : ID (',' ID)*
    ;

systemCall
    : '#' ID '(' argumentList? ')' ';'? NEWLINE?
    ;

functionCall
    : ID '(' argumentList? ')' ';'? NEWLINE?
    ;

objectMethodCall
    : ID '.' ID '(' argumentList? ')' ';'? NEWLINE?
    ;

argumentList
    : expression (',' expression)*
    ;

expression
    : literal
    | ID
    | objectAccessExpr
    | objectInstantiationExpr
    | systemCallExpr
    | functionCallExpr
    | '(' expression ')'
    | '!' expression
    | '-' expression
    | expression ('*'|'/') expression
    | expression ('+'|'-') expression
    | expression ('<'|'>'|'<='|'>=') expression
    | expression ('=='|'!=') expression
    | expression '&&' expression
    | expression '||' expression
    ;

literal
    : INT
    | FLOAT
    | STRING
    | 'null'
    | 'true'
    | 'false'
    ;

objectAccessExpr
    : ID '.' ID
    | ID '.' ID '(' argumentList? ')'
    ;

objectInstantiationExpr
    : '*' ID '(' argumentList? ')'
    ;

systemCallExpr
    : '#' ID '(' argumentList? ')'
    ;

functionCallExpr
    : ID '(' argumentList? ')'
    ;

terminateStatement
    : '!!' ';'? NEWLINE?
    ;

// Lexer Rules
TYPE : 'i' | 'l' | 'f' | 'b' | 's' | 'o';
ID : [a-zA-Z_][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
FLOAT : [0-9]+ '.' [0-9]+ ;
STRING : '"' .*? '"' ;
NEWLINE : '\r'? '\n' | '\r' ;
WS : [ \t]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
AND : '&&' ;
OR : '||' ;
NOT : '!' ;
EQ : '==' ;
NEQ : '!=' ;
GT : '>' ;
LT : '<' ;
GE : '>=' ;
LE : '<=' ;
