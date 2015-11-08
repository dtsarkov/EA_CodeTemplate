grammar EACodeTemplate;

file		: (line '\r'? '\n')* {
			System.out.println("");
		}
;
line 		: (
		  assignment
		| comment
		| macros 
		| txt=. {
			System.out.print($txt.text);
		}
		)*?
;
assignment	: variable op expression 
;
op		: '=' 
		| '+='
;
		
expression	: expr ('+' expr)*
;
expr		: variable
		| list
		| STRING
; 
variable	:'$'ID
;
STRING		:'"' .*? '"'
;
CHAR		: [a-zA-Z]
;
ID		: CHAR [a-zA-Z0-9]*
;

comment		: '$COMMENT=' STRING
; 
macros		: procInstruction
//		| function
		| list
//		| branching
//		| substitution
		| templateCall
;
procInstruction	: '%PI' '=' '"' PI* '"%'
;
list		: '%list' '=' '"' ID '"' '@separator=' 
;
templateCall	: '%' ID '%' {
			System.out.print("!template!."+$ID.text);
		}
;
PI		: (' ' | '\\n' | '\\t')+
;