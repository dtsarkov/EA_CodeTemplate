grammar EACodeTemplate;

file	: (line NL)* EOF
;
line 	: comment 
	| assignment 
	| branching
	| text 
;

//
// ============================================================================
comment		: '$COMMENT' EQ StringLiteral
;

//
// ============================================================================
assignment	: variable (op=EQ|op=AEQ) expression
;
expression	: expr ('+' expr)*
//TO-DO: Add += operator
;
expr		: stringLiteral 
		| variable 
		| attribute 
		| tag
		| macros 
//TO-DO: macros has to be replaced
;

variable	: VAR
;
attribute	: ATTR
;
tag		: TAG
;

ATTR		: '$.' ID;
VAR		: '$'  ID;
TAG		: '$#' ID;

// Branching
// ============================================================================
branching	: 
		  if_stmt
		| elseif_stmt
		| else_stmt
		| endif_stmt
		| endtempalte_stmt
		
;
if_stmt		: IF compare_expr '%'
;
elseif_stmt	: ELSEIF compare_expr '%'
;
else_stmt	: ELSE
;
endif_stmt	: ENDIF
;
endtempalte_stmt: '%endTemplate%'
;
// ----------------------------------------------------------------------------
compare_expr	: predicat (pred_op predicat)*
;
predicat	: test_expr test_op test_expr
;
test_expr	: stringLiteral 
		| variable 
		| attribute
		| tag
//		| templateName  //TO-DO: Parser could not recognize templateName
;
templateName	: ID
;
pred_op     	: 'and' | 'or'
;
test_op		: '=='	| '!='
;
//
// ============================================================================
text		: ( 
		    freeText  
		  | variable 
		  | stringLiteral  
		  | macros
		)+
;
//
// ============================================================================
macros		: textMacros
		| templateSubstitution
		
;
textMacros 	: '%dl%' | '%pc%' | '%qt%' | '%sl%' | '%eq%'
;
templateSubstitution	: Template
;
Template	: '%' ID '%'
;

// ============================================================================
// =
// ============================================================================
freeText	: FreeText
;
FreeText	: [a-zA-Z0-9_(){}\.+\-]+
;

//string 		: StringLiteral
//;
stringLiteral	: StringLiteral
;
StringLiteral 	: '"' StringCharacters? '"' 
; 
  
fragment StringCharacters : StringCharacter+ 
; 
  
fragment StringCharacter : ~["\\] 
	| EscapeSequence 
; 
  
fragment EscapeSequence :   
	 '\\' [btnfr"'\\] 
//	| OctalEscape 
//	| UnicodeEscape 
; 


// Keywords
// ============================================================================

IF	: '%if'   	;
ELSE	: '%else'       ;
ELSEIF	: '%elseif'     ;
ENDIF	: '%endif' 	;


PC	: '%';
EQ	: '=';
AEQ	: '+=';
//ADD	: '+';
NUMBER  : [0-9]+;

ID 	: [a-zA-Z_] [a-zA-Z0-9_]*; 

NL 	: '\r'?'\n';
WS 	: [ \t] -> skip;
	
