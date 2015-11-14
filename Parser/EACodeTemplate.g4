grammar EACodeTemplate;

file		: (
			  line '\r'? '\n'
			| '\r'? '\n'
		   )* EOF
;
line 		: comment
		| assignemet
		| branching
		| procInstruction
		| (
		  	  macros
			| freeText /*{
				System.out.println("TEXT=["+$freeText.text+"]");
			}*/
		   )*
		
;
freeText : FreeTextCharacter+
;
//fragment 
FreeTextCharacter: ~[%$]
;

// Assignment Definition Rules 
// ============================================================================
// These rules apply to variable definitions:
// • Variables have global scope within the template in which they are 
//   defined and are not accessible to other templates
// • Each variable must be defined at the start of a line, without any 
//   intervening white space
// • Variables are denoted by prefixing the name with $, as in $foo
// • Variables do not have to be declared, prior to being defined
// • Variables must be defined using either the assignment operator (=), 
//   or the addition-assignment operator (+=)
// • Multiple terms can be combined in a single definition using 
//   the addition operator (+) 

assignemet	: variable '=' expression 
;
variable	:'$'ID
;
OP		: '=' | '+='
;
		
expression	: expr ('+' expr)*
;
expr		: variable
		| StringLiteral
//		| list
//		| templateCall
; 

// Branching Macro Defintion
// ============================================================================
branching	: br_if
		| br_else
		| br_endif
		| br_endTemplate
;
br_if		: '%'('if' | 'elseIf') test testOp test'%'
;
testOp		: '==' | '!='
;
test		: variable
		| StringLiteral
		//TO-DO: Add substitution macro
;
br_else		: '%else%'
;
br_endif	: '%endIf%'
;
br_endTemplate	: '%endTemplate%'
;

// Processing Instructions Macro Definition
// ============================================================================
procInstruction	: '%PI=' Separators '%'
;
Separators : '"' (' ' | EscapeSequence) * '"'
;

// Macros Definition Rules
// ============================================================================
// All macros are enclosed within percent (%) signs in the form of:
//     %<macroname>%

macros		: templateCall
//		| templateSubstitution
//		| fieldSubstitution
//		| function
//		| list
//		| macro  
;

// List Macro Definition
// ----------------------------------------------------------------------------
// List Macro is used to loop or iterate through a set of Objects that 
// are contained within or are under the current object, 
// you can do so using the %list macro. This macro performs an iterative 
// pass on all the objects in the scope of the current template, 
// and calls another template to process each one.
//
// The basic structure is:
//     %list=<TemplateName> @separator=<string> @indent=<string> ( <conditions> ) %

list		: '%list' '=' '"' ID '"' '@separator=' '%'
;




// ----------------------------------------------------------------------------
templateCall	: '%' ID '%' {
			System.out.print("!template!."+$ID.text);
		}
;
// ============================================================================

comment		: '$COMMENT=' StringLiteral
; 


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
/* 
fragment  OctalEscape :   
	  '\\' OctalDigit 
	| '\\' OctalDigit OctalDigit 
	| '\\' ZeroToThree OctalDigit OctalDigit 
; 
fragment UnicodeEscape :   
	'\\' 'u' HexDigit HexDigit HexDigit HexDigit 
; 

fragment OctalDigit  : [0-7]            ;
fragment HexDigit    : [0-9A-Fa-f]      ;
fragment ZeroToThree : [0-3] 		;
*/

// ============================================================================
fragment ID_LETTER	: [a-zA-Z_]
;
fragment DIGIT		: [0-9]
;	
ID		: ID_LETTER (ID_LETTER | DIGIT)*
;

// Operators
// ============================================================================
ASSIGN		: '=';
ADD_ASSIGN	: '+=';


// Whitespace and comments
// ============================================================================
WS  :  [ \t]+ -> skip
    ;

//COMMENT
//    :   '/*' .*? '*/' -> skip
//    ;
//
//LINE_COMMENT
//    :   '//' ~[\r\n]* -> skip
//    ;
