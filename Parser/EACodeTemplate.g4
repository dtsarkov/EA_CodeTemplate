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
line 		: comment 
		| assignment 
		| branching
		| text 
;

//
// ============================================================================
//comment		: '$COMMENT' EQ StringLiteral
comment 	: '%%' .*? NL 
;

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
		| macros 
//TO-DO: macros has to be replaced
;

variable	: VAR //| GVAR
;
attribute	: ATTR
;
tag		: TAG
;

ATTR		: '$.' ID;
VAR		: '$''$'?  ID;
//GVAR		: '$$' ID;
TAG		: '$.' StringLiteral;

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
else_stmt	: '%else%'
;
endif_stmt	: '%endif%'
;
endtempalte_stmt: '%exit%'
;
// ----------------------------------------------------------------------------
compare_expr	: predicate (pred_op predicate)*
;
predicate	: expr test_op expr
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
		  | attribute
		  | tag
		  | stringLiteral  
		  | macros
		)+
;
//
// ============================================================================
macros		: textMacros
		| listMacro
		| templateSubstitution
;
textMacros 		: '%dl%' | '%pc%' | '%eq%' | '%qt%' | '%us%' //| '%sl%' 
;
listMacro		: List '=' attribute 
				templateParameter 
			'%'
;
List			: '%list'
;
templateParameter 	: TemplateParameter '=' stringLiteral
;
TemplateParameter	: '@template'
;
templateSubstitution	: Template
;
Template		: '%' ID '%'
;

// ============================================================================
// =
// ============================================================================
freeText	: FreeText
;
FreeText	: [a-zA-Z0-9_(){}\.+\-\*\:\/]+
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
//ELSE	: '%else%'      ;
ELSEIF	: '%elseif'     ;
//ENDIF	: '%endif%' 	;


PC	: '%';
EQ	: '=';
AEQ	: '+=';
//ADD	: '+';
NUMBER  : [0-9]+;

ID 	: [a-zA-Z_] [a-zA-Z0-9_]*; 

NL 	: '\r'?'\n';
WS 	: [ \t] -> channel(1);
	
