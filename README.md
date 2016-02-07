EA\_CodeGenerator
=================

Code **Generator** for Sparx Systems Enterprise Architect

Links:

ANTLR Parser http://www.antlr.org/download.html EA Code Template Syntax
http://sparxsystems.com/enterprise\_architect\_user\_guide/12.1/software\_engineering/codetemplatesyntax.html

 

What's new
----------

### Version 0.37

-   Fixed **%file%** macro bug when macro does not create new output file when
    called from a template which called from another template.

-   Added the second parameter "*remove new lines*" to **PLAIN_TEXT** function.
    Syntax:
    ~~~~
    %PLAIN_TEXT(<notes> [,<remove new lines>])%
    ~~~~
    Where:
	* <notes> - element notes
	* <remove new lines>  - expression returning "true" or "false"

-   Added a new command line parameter **-D** | **--variable**. The parameter allows
    define global variables (**$$**) from command line.   E.g. the below command
    line defines two global variables **$$Variable1** and **$$Variable2**
    ~~~~ 
    EACodeGenerator.cmd -m model.eap -t template_name ... -D Variable1=Value1" --variable Variable2=Value2 
    ~~~~

### Version 0.36

-   Added **loop** option to **%exit%** macro.

    **%exit** [**loop**]**%** statement stops template execution. With the
    “loop” option it also stops execution of **%list** and **%split** macros
    from which the template was called.
