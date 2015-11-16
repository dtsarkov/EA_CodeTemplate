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
assignment	: variable EQ expression
;
variable	: Var
;
Var		: '$'ID
;
expression	: expr ('+' expr)*
;
expr		: (StringLiteral | variable | macros)
;

// Branching
// ============================================================================
branching	: 
		  if_stmt
		| elseif_stmt
		| else_stmt
		| endif_stmt
		| endtempalte_stmt
		
;
if_stmt		: '%if%'
;
elseif_stmt	: '%elseif%'
;
else_stmt	: '%else%'
;
endif_stmt	: '%endif%'
;
endtempalte_stmt: '%endTemplate%'
;

//
// ============================================================================
text		: ( 
		    FreeText 
		  | variable 
		  | StringLiteral 
		  | macros
		)+
;
FreeText	: [a-zA-Z0-9_(){}\.+\-]+
//~["$=\n\r ]+? 
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
StringLiteral : '"' StringCharacters? '"' 
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
/*
IF	: 'if'   	;
ELSE	: 'else'        ;
ELSEIF	: 'elseif'      ;
ENDIF	: 'endif'	;
*/

PC	: '%';
EQ	: '=';
NUMBER  : [0-9]+;

ID 	: [a-zA-Z_] [a-zA-Z0-9_]*; 

NL 	: '\r'?'\n';
WS 	: [ \t] -> skip;
	
