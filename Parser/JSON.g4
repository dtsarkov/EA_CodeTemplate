// The grammar file was taken from book "The Definitive ANTLR 4 Reference" 
// by Terence Parr and slightly modified to work with EA object model
// Modifications made for standard JSON syntax are:
// 	M-1. File may contain only one object.
//	M-2. Object cannot be empty 
//	M-3. Added BOOLEAN
//	M-4. Split NUMBER by INTEGER and DOUBLE
//
// History
// Version	Description
// ------------	---------------------------------------------------------------
// 0.01		Initial version
//				
// ----------------------------------------------------------------------------

// Derived from http://json.org
grammar JSON;

file:   object 
//M-1//    |   array
;

object
   : '{' pair (',' pair)* '}'
//M-2//   | '{' '}'
;

pair
   : STRING ':' value
   ;

array
   : '[' value (',' value)* ']'
   | '[' ']'
   ;

value
   : STRING
//M-4//   | NUMBER
   | INTEGER
   | DOUBLE
   | BOOLEAN
   | NULL
   | object
   | array
//M-3//   | 'true'
//M-3//   | 'false'
//M-3//   | 'null'
   ;


BOOLEAN
   : 'true' | 'false'
   ;
NULL 
   : 'null'
   ;   
STRING
   : '"' (ESC | ~ ["\\])* '"'
   ;
fragment ESC
   : '\\' (["\\/bfnrt] | UNICODE)
   ;
fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;
DOUBLE
    :   '-'? INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3, -4.5
    |   '-'? INT EXP             // 1e10 -3e4
    ;
INTEGER        
    :   '-'? INT                 // -3, 45
    ;   
//M-4//NUMBER
//M-4//    :   '-'? INT '.' [0-9]+ EXP? // 1.35, 1.35E-9, 0.3, -4.5
//M-4//    |   '-'? INT EXP             // 1e10 -3e4
//M-4//    |   '-'? INT                 // -3, 45
//M-4//    ;   
fragment INT
   : '0' | [1-9] [0-9]*
   ;
// no leading zeros
fragment EXP
   : [Ee] [+\-]? INT
   ;
// \- since - means "range" inside [...]
WS
   : [ \t\n\r] + -> skip
   ;