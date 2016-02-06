EA\_CodeGenerator
=================

Code **Generator** for Sparx Systems Enterprise Architect

Links:

ANTLR Parser http://www.antlr.org/download.html EA Code Template Syntax
http://sparxsystems.com/enterprise\_architect\_user\_guide/12.1/software\_engineering/codetemplatesyntax.html

 

What’s new
----------

### Version 0.37

-   Fixed **%file% **macro bug when macro does not create new output file when
    called from a template which called from another template.

-   Added a new command line parameter **-D --variable**. The parameter allows
    define global variables (**\$\$**) from command line.  
    E.g. the below command line defines two global variables **\$\$Variable1**
    and **\$\$Variable2**

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
EACodeGenerator.cmd -m model.eap -e "Element name" ... -D Variable1="Variable1 Value" --variable Variable2="Varable2 Value" 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Version 0.36

-   Added “loop” option to %exit% macro.

    **%exit** [**loop**]**%** statement stops template execution. With the
    “loop” option it also stops execution of **%list** and **%split** macros
    from which the template was called.
