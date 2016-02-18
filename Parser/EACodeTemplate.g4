//
// History
// Version	Description
// ------------	---------------------------------------------------------------
// 0.21		Added Pred_op to freeText rule to fix the bug when parser did not
//		recognized and/or as a free text.
// ----------------------------------------------------------------------------
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
line 		: fileMacro
		| assignment 
		| branching
		| text 
;

//
// ============================================================================
fileMacro	: FileMacro expr (override)?'%'
;
override	: Override expr // expr should return "override" | "append" | "skip"
; 		
FileMacro	: '%file='
;
Override	: '@mode='
;
// ============================================================================
assignment	: variable (op=EQ|op=AEQ) expression
;
expression	: expr (ADD expr)*
;
expr		: stringLiteral 
		| variable 
		| attribute 
		| tag
		| functions
		| parameter
		| callMacro
		| listMacro
		| splitMacro 
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
ATTR		: (
			THIS | PARN | PCKG | SRCE | TRGT | SROL | TROL 
		  ) ID;
TAG		: (
			THIS | PARN | PCKG | SRCE | TRGT | SROL | TROL 
		  ) StringLiteral;
VAR		: '$''$'?  ID	;

THIS		: '$.' | '$this.'	;
PARN		: '$parent.'		;
PCKG		: '$package.'		;
SRCE		: '$source.'		;
TRGT		: '$target.'		;
SROL		: '$sourceRole.'	;
TROL		: '$targetRole.'	;
 

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
endtempalte_stmt: EXIT | BREAK
;
// ----------------------------------------------------------------------------
compare_expr	: predicate (pred_op predicate)*
;
predicate	: expr test_op expr
		//| expr RegEx_op stringLiteral
;
pred_op     	: Pred_op;
test_op		: Test_op;

Pred_op 	: 'and' | 'or';
Test_op		: '=='	| '!=' | '~=';
//RegEx_op	: '~=';
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
			| '%DATE%' | '%TIME%' //| '%USER%' 
;
listMacro		: List 	attribute templateName (
					templateParameters
				|	separator
				|	conditions
				)* 
			'%'
;
callMacro		: Call templateName (templateParameters | elementInScope)*  '%'
;
splitMacro		: Split expr templateName (
					templateParameters 
				| 	delimiter
				|	separator
				|	elementInScope
				)*
			'%'
;				
templateName 		: TemplateName expr //stringLiteral
;
templateParameters	: Parameters parameters
;
separator		: Separator expr
;
conditions		: OBR compare_expr CBR
;
delimiter		: Delimiter expr
;
elementInScope		: ElementInScope (
				THIS | PARN | PCKG | SRCE | TRGT | SROL | TROL 
			)
;
functions		: Function parameters CBR '%'
;
parameters		: expression (COMA expression)*
;
piMacro			:  PI stringLiteral '%'
;

Call            : '%call';
List            : '%list=';
Split           : '%split=';
PI              : '%PI=';
Function        : '%UPPER(' | '%LOWER(' | '%REPLACE(' | '%TRIM('  | '%MID('
		| '%LENGTH(' //| 'FIND('
		| '%WRAP_TEXT(' | '%PLAIN_TEXT('
		| '%MESSAGE(' | '%WARNING(' | '%ERROR(' | '%DEBUG('
		| '%EXIST('
;

TemplateName    : '@template=';
Parameters      : '@parameters=';
Separator       : '@separator=';
ElementInScope  : '@element=';
Delimiter       : '@delimiter=';
// ============================================================================
// =
// ============================================================================
freeText	: FreeText | Pred_op | COMA | OBR | CBR | DOT | ADD
;
FreeText	: [a-zA-Z0-9_{}\.\-\*\:\/\[\]<>\~!?@#^&\|'`;]+
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
EXIT	: '%exit%' 	;
BREAK	: '%exit loop%' ;

PC	: '%';
EQ	: '=';
EQQ	: '==';
AEQ	: '+=';
ADD	: '+';
COMA	: ',';
OBR	: '(';
CBR	: ')';
DOT	: '.';

fragment DIGIT : [0-9];
NUMBER  : DIGIT+;

ID 	: [a-zA-Z_] [a-zA-Z0-9_]*; 

NL 	: '\r'?'\n';
COMMENT	: '%%' .*? (NL|EOF)	-> skip;	 
WS 	: [ \t] -> channel(1);

	
