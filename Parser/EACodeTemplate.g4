grammar EACodeTemplate;

//file	: (line NL)* EOF
file	: line_text*
;
line_text: line NL
	| line EOF
	| emptyLine
;	
emptyLine	: NL
;
line 		: assignment 
		| branching
		| text 
;

//
// ============================================================================
//comment		: '$COMMENT' EQ StringLiteral

//
// ============================================================================
assignment	: variable (op=EQ|op=AEQ) expression
;
expression	: expr ('+' expr)*
;
expr		: stringLiteral 
		| variable 
		| attribute 
		| tag
		| functions
		| parameter
		| callMacro
		| listMacro 
;

variable	: VAR
;
attribute	: ATTR  
;
tag		: TAG
;
parameter	: PARM
;
PARM		: '$' DIGIT	;
ATTR		: (THIS | PARN | PCKG | SRCE | TRGT ) ID;
TAG		: (THIS | PARN | PCKG | SRCE | TRGT ) StringLiteral;
VAR		: '$''$'?  ID	;

THIS		: '$.' | '$this.';
PARN		: '$parent.'	;
PCKG		: '$package.'	;
SRCE		: '$source.'	;
TRGT		: '$target.'	; 

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
endtempalte_stmt: '%exit%'
;
// ----------------------------------------------------------------------------
compare_expr	: predicate (pred_op predicate)*
;
predicate	: expr test_op expr
;
pred_op     	: Pred_op;
test_op		: Test_op;

Pred_op 	: 'and' | 'or';
Test_op		: '=='	| '!=';


//
// ============================================================================
text		: ( 
		    freeText  
		  | variable 
		  | attribute
		  | tag
		  | parameter
		  | stringLiteral  
		  | macros
		)+
;
//
// ============================================================================
macros		: textMacros
		| listMacro
		| functions
		| callMacro
		| splitMacro
		| piMacro
;
textMacros 		: '%dl%' | '%pc%' | '%eq%' | '%qt%' | '%us%' | '%nl%' //| '%sl%' 
;
listMacro		: List 	attribute templateName (
					templateParameters
				|	separator
				|	elementInScope
				)* 
			'%'
;
callMacro		: Call stringLiteral (templateParameters | elementInScope)*  '%'
;
splitMacro		: Split expr templateName (
					templateParameters 
				| 	delimiter
				|	separator
				)*
			'%'
;				
templateName 		: TemplateName stringLiteral
;
templateParameters	: Parameters parameters
;
separator		: Separator expr
;
delimiter		: Delimiter expr
;
elementInScope		: ElementInScope (SRCE | TRGT | PCKG | PARN)
;
functions		: Function parameters ')%'
;
parameters		: expr (',' expr)*
;
piMacro			:  PI stringLiteral '%'
;

List            : '%list=';
Call            : '%call=';
Split           : '%split=';
PI              : '%PI=';
Function        : '%UPPER(' | '%LOWER(' | '%REPLACE(' | '%DEBUG(';

TemplateName    : '@template=';
Parameters      : '@parameters=';
Separator       : '@separator=';
ElementInScope  : '@element=';
Delimiter       : '@delimiter=';
// ============================================================================
// =
// ============================================================================
freeText	: FreeText
;
FreeText	: [a-zA-Z0-9_(){}\.+\-\*\:\/\[\]<>\~!?@#^&\|'`,;]+
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
ELSE	: '%else%'      ;
ELSEIF	: '%elseif'     ;
ENDIF	: '%endif%' 	;

PC	: '%';
EQ	: '=';
EQQ	: '==';
AEQ	: '+=';
//ADD	: '+';

fragment DIGIT : [0-9];
NUMBER  : DIGIT+;

ID 	: [a-zA-Z_] [a-zA-Z0-9_]*; 

NL 	: '\r'?'\n';
COMMENT	: '%%' .*? (NL|EOF)	-> skip;	 
WS 	: [ \t] -> channel(1);

	
