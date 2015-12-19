package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Repository;
import org.sparx.TaggedValue;

import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateBaseListener;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateLexer;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.AssignmentContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.AttributeContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.CallMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Compare_exprContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ElementInScopeContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Else_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Elseif_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Endif_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Endtempalte_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ExprContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FileContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FreeTextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FunctionsContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.If_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Line_textContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ListMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ParameterContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PiMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PredicateContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.SplitMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.StringLiteralContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TagContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextMacrosContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.VariableContext;

public class TemplateProcessor extends EACodeTemplateBaseListener {
	/* Static section  
	 **************************************************************************/
	private static String 		templateFolder;
	private static String 		templateExtention;
	private static Repository 	EA_Model;
	
	private static Map<String,String> TextMacros;
	private static Map<String,String> globalVariables;
	
	static {
		templateFolder = ".";
		templateExtention = ".template";
		
		globalVariables = new HashMap<String,String>();
		
		TextMacros = new HashMap<String,String>();
		TextMacros.put("%eq%", "=");
		TextMacros.put("%pc%", "%");
		TextMacros.put("%dl%", "$");
		TextMacros.put("%qt%", "\"");
		TextMacros.put("%us%", "_");
		TextMacros.put("%nl%",System.lineSeparator());
	}
	
	static public void setTemplateFolder( String path ) {
		templateFolder = path;
	}
	static public String getTemplateFolder( ) {
		return templateFolder;
	}
	
	static public void setTemplateExtention( String extention ) {
		templateExtention = (extention.length() == 0) ? "" : "."+extention;
	}
	static public String getTemplateExtention() {
		return templateExtention;
	}
	
	static public void setEAModel( Repository model ) {
		EA_Model = model;
	}
	static public Repository getEAModel() {
		return EA_Model;
	}
	
	static public void message(String s) {
		System.out.println(s);
	}
	static public void message(String format, Object... args) {
		message(String.format(format, args));
	}
	static public void error(String format, Object...args) {
		System.err.println("ERROR: "+String.format(format, args));
	}
	
	static private boolean debugMode = false;
	static public void debug(String format, Object... args) {
		if ( debugMode ) {
			System.out.println(">>DBG :"+String.format(format, args));
		}
	}
	static public void setDebug(boolean mode) {
		debugMode = mode;
	}
	/*
	 * 
	 **************************************************************************/
	private String 					templateName;
	private boolean					isOpen;
	private EACodeTemplateParser 	parser;
	private CommonTokenStream 		tokens;
	private EACodeTemplateLexer 	lexer;
	private Object 			 		element;
	private Object					packageElement;
	private String					lineSeparator = System.lineSeparator();
	private int						textLevel 	= 0;
	private Writer 					writer;

	private ArrayList<String>  parameters	  = new ArrayList<String>(9);
	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor( String templateName ) {
		this.templateName = templateName;
		writer = new PrintWriter(System.out);
	}
	

	public void addParameter(String value) {
		parameters.add(value);
	}
	
	public void setParameter(int index, String value) {
		if (index < parameters.size() ) 
			parameters.remove(index);
		parameters.add(index,value);
	}
	
	public String getParameter(String parameter ) {
		//Parameter is $[0-9]. $0 contains number of parameters passed to template.
		int index = Integer.valueOf(parameter.substring(1));
		return getParameter(index);
	}
	public String getParameter(int index ) {
		String value = "";
		if (index == 0 ) {
			value = String.valueOf(parameters.size());
		} else if ( index >= 1 && index <= parameters.size() ) {
			value = parameters.get(index-1);
		}
		debug(">> Get Template Parameter [%d] = [%s]", index, value);		
		return value;
	}
	
	public void setOutput(Writer writer) {
		this.writer = writer;
	}
	
	public Writer getOutput() {
		return writer;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}
	public void setLineSeparator(String lineSeparator) {
		debug(">> Set Line separator [%s]", lineSeparator);
		this.lineSeparator = lineSeparator;
	}

	/*
	 * 
	 **************************************************************************/
	private boolean openTemplateFile() {
		String 	fileName = templateFolder+"/"+templateName + templateExtention;
		boolean success = false;
		try {
			InputStream 			is 		= new FileInputStream(fileName);
			ANTLRInputStream  		input 	= new ANTLRInputStream(is);
			
			lexer 	= new EACodeTemplateLexer(input);
			tokens	= new CommonTokenStream(lexer);
			parser 	= new EACodeTemplateParser(tokens);

			success = true;
			
		} catch ( FileNotFoundException e ) {
			error("Could not find template file [%s]",fileName);
		} catch ( IOException e ) {
			error("Could not read from template file [%s]",fileName);
		}
		return success;
	}
	
	public void execute() {
		if ( !isOpen ) {
			isOpen = openTemplateFile();
			if ( !isOpen ) return;
		}
		message("%-30s|%-20s|%-30s"
				,this.getAttribute("$.Name",false)
				,this.getAttribute("$.Type",false)
				,this.templateName
		);
		parser.addParseListener(this);
		parser.file();
		parser.removeParseListeners();
		parser.reset();
	}
	
	public void setElement(Object element) {
		this.element = element;
		if ( element instanceof Element ) {
			if ( ((Element)element).GetType().equalsIgnoreCase("Package") ) {
				this.packageElement = this.element;
				this.element = EA_Model.GetPackageByGuid(((Element)element).GetElementGUID());
			}
		}
		Parent 		= null;
		hasParent 	= true;
		Package 	= null;
		hasPackage	= true;
		Source 		= null;
		hasSource	= true;
		Target 		= null;
		hasTarget	= true;
	}
	
	public Object getElement() {
		return element;
	}
	
	private Object 	Parent 		= null;
	private boolean	hasParent 	= true;
	private Object getParent() {
		if ( Parent == null && hasParent ) {
            debug("Getting parent for elelment %s",element.getClass());
			try {
				Method m = element.getClass().getMethod("GetParentID", null);
                int parentID = Integer.parseInt(m.invoke(element,null).toString());
                debug("\t\tParentID = %d",parentID);
                if ( parentID != 0 )
                    Parent = EA_Model.GetElementByID(parentID);
			} catch (NoSuchMethodException e ) {
				hasParent = false;
			} catch (Exception e ) {
				e.printStackTrace(System.err);
			}
		}
		
		return Parent;
	}

	private Object 	Package 		= null;
	private boolean	hasPackage 		= true;
	private Object getPackage() {
		if ( Package == null && hasPackage ) {
			try {
				Method m = element.getClass().getMethod("GetPackageID", null);
				Package = EA_Model.GetPackageByID(Integer.parseInt(m.invoke(element,null).toString()));
			} catch (NoSuchMethodException e ) {
				hasPackage = false;
			} catch (Exception e ) {
				e.printStackTrace(System.err);
			}
		}
		return Package;
	}

	private Object Source	  = null;
	private boolean hasSource = true;
	private Object getSource() {
		if ( Source == null && hasSource ) {
			try {
				Method m = element.getClass().getMethod("GetClientID", null);
				Source = EA_Model.GetElementByID(Integer.parseInt(m.invoke(element,null).toString()));
			} catch (NoSuchMethodException e ) {
				hasSource = false;
			} catch (Exception e ) {
				e.printStackTrace(System.err);
			}
		}
		return Source;
	}

	private Object  Target	  = null;
	private boolean hasTarget = true;
	private Object getTarget() {
		if ( Target == null && hasTarget ) {
			try {
				Method m = element.getClass().getMethod("GetSupplierID", null);
				Target = EA_Model.GetElementByID(Integer.parseInt(m.invoke(element,null).toString()));
			} catch (NoSuchMethodException e ) {
				hasTarget = false;
			} catch (Exception e ) {
				e.printStackTrace(System.err);
			}
		}
		return Target;
	}
	/*
	 * 
	 **************************************************************************/
	 public void setVariable( String name, String value ) {
		localVariables.put(name,value);
	 }
	 
	@Override
	public void enterAssignment(AssignmentContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel = -1000;
	}
	
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		if ( !executionState.canProcessBranch()) return;
		
		String variable = ctx.variable().getText();
		Map<String,String> scope;
		
		if (variable.startsWith("$$")) {
			scope = globalVariables;
		} else {
			scope = localVariables;
		}
		
		
		String value = "";
		if ( ctx.op.getType() == EACodeTemplateParser.AEQ ) {
			value = scope.get(variable);
		}
		
		ExprContext c;
		for ( int i = 0; i < ctx.expression().getChildCount(); i++ ) {
			c = ctx.expression().expr(i);
			if ( c != null )
				value += calcExpression(c);
		}
		debug("Assignment (%d): %s=%s",textLevel, variable,value);
		scope.put(variable, value);
		
		textLevel = 0;
	}

	private String calcExpression(ExprContext ctx ) {
		ParseTree 	c;
		String 		s = "", v = null;
		for ( int i = 0; i < ctx.getChildCount(); i++ ) {
			c = ctx.getChild(i);
			debug("Calc expression for class [%s]",c.getClass().toString());
			if ( c instanceof StringLiteralContext ) {
				v = translateStringLiteral(c.getText());
			} else if ( c instanceof VariableContext ) {
				v = getVariableValue(c.getText());
			} else if ( c instanceof AttributeContext ) {
				v = getAttributeValue(c.getText());
			} else if ( c instanceof TagContext ) {
				v = getTagValue(c.getText());
			} else if ( c instanceof FunctionsContext ) {
				v = calcFunction((FunctionsContext)c);
			} else if ( c instanceof ParameterContext ) {
				v = getParameter(c.getText());
			} else if ( c instanceof CallMacroContext ) {
				StringWriter sw = new StringWriter(255);
				executeCallMacro((CallMacroContext)c, sw);
				v = sw.toString();
			} else if ( c instanceof ListMacroContext ) {
				StringWriter sw = new StringWriter(255);
				executeListMacro((ListMacroContext)c, sw);
				v = sw.toString();
			} else {
				debug(">> Unknown Expression context [%]",c.getClass().toString()); 
			}
			if ( v == null ) {
				s = null;
			} else {
				s += v;
			}
		}
		return s;
	}
	
	private String calcFunction(FunctionsContext ctx ) {
		String value = null;
		String function = ctx.getChild(0).getText();
		int parmCount = ctx.parameters().expr().size();
		
		debug("Function call [%s] with [%d] parameter(s)",function,parmCount);
		if ( parmCount < 1 ) {
			error("Function call should have at lease one parameter");
			return value;
		}
		String firstParameter = calcExpression(ctx.parameters().expr(0)); 

		if (function.equalsIgnoreCase("%LOWER(") ) {
			value = firstParameter.toLowerCase();
		} else if (function.equalsIgnoreCase("%UPPER(") ) {
			value = firstParameter.toUpperCase();
		} else if (function.equalsIgnoreCase("%REPLACE(") ) {
			if (parmCount < 3 ) {
				error("Incorrect function call %REPLACE( string, regexp, replacement )%\n");
				return value;
			}
			value = firstParameter.replaceAll(
						calcExpression(ctx.parameters().expr(1))
					   ,calcExpression(ctx.parameters().expr(2))
					);
		} else if (function.equalsIgnoreCase("%DEBUG(") ) {
			debug(firstParameter);
		} else {
			error("Unknown function %s\n", function);
		}
		return value;
	}
	
	static private String escapes[][] = {
			 {Matcher.quoteReplacement("\\t"), 	"\t"}
			,{Matcher.quoteReplacement("\\n"), 	"\n"}
			,{Matcher.quoteReplacement("\\f"), 	"\f"}
			,{Matcher.quoteReplacement("\\r"), 	"\r"}
			,{Matcher.quoteReplacement("\\\""), "\""}
			,{Matcher.quoteReplacement("\\'"), 	"'" }
			,{Matcher.quoteReplacement("\\\\"), Matcher.quoteReplacement("\\")}
	};
	private String translateStringLiteral( String s ) {
		String s1 = s.substring(1,s.length()-1); //remove leading and trailing double quotes
		for ( int i = 0; i < escapes.length; i++ )
			s1 = s1.replaceAll(escapes[i][0], escapes[i][1]);
		return s1; 
	}
	
	private boolean isGlobalVariable(String variable) {
		return variable.startsWith("$$");
	}
	
	private String getVariableValue(String variable ) {
		String value = "";
		
		if ( isGlobalVariable(variable)) {
			value = globalVariables.get(variable);
		} else {
			value = localVariables.get(variable);
		}
		return value;
	}

	private Object[] getElementSet(String scope) {
		Object[] elements = new Object[2];
		if ( scope.equalsIgnoreCase("$") || scope.equalsIgnoreCase("$this") ) {
			elements[0]	= this.element;
			elements[1]	= this.packageElement;
		} else if ( scope.equalsIgnoreCase("$parent") ) {
			elements[0] = getParent();
		} else if ( scope.equalsIgnoreCase("$package") ) {
			elements[0] 	= getPackage();
			elements[1] = ((org.sparx.Package)elements[0]).GetElement();
		} else if ( scope.equalsIgnoreCase("$source") ) {
			elements[0] = getSource();
		} else if ( scope.equalsIgnoreCase("$target") ) {
			elements[0] = getTarget();
		}
		
		
		return elements;
	}
	
    private Object getAttribute(String attributeName) {
        return getAttribute(attributeName,true);
    }
    
	private Object getAttribute(String attributeName, boolean raiseError) {
		
		String name[] = attributeName.split("\\.");
		debug("getAttribute([%s] [%s]",name[0],name[1]);
		
		Object[] elements	= getElementSet(name[0]);
		String methodName 	= "Get"+name[1];

		Object attribute 		= null;
		
		if ( elements[0] != null ) try {
			Method m = elements[0].getClass().getMethod(methodName);
			attribute = m.invoke(elements[0], null);
		} catch (NoSuchMethodException e) {
			if ( elements[1] != null ) {
				try {
					Method pm = elements[1].getClass().getMethod(methodName);
					attribute = pm.invoke(elements[1], null);
				} catch (NoSuchMethodException pe) {
                    if ( raiseError ) {
                        error("Could not find \"%s\" attribute (2)",attributeName);
                    } else {
                        attribute = null;
                    }
				} catch (Exception pe ) {
					pe.printStackTrace(System.err);;
				}
			} else {
                if ( raiseError ) {
                    error("Could not find \"%s\" attribute (1)",attributeName);
                } else {
                    attribute = null;
                }
			}
		} catch (Exception e ) {
			e.printStackTrace(System.err);;
		}

		return attribute;
	}
	
	private String getAttributeValue(String attributeName) {
		String value = null;
		Object attribute = getAttribute(attributeName);
		
		if ( attribute != null )
			value = attribute.toString();
		
		return value;
	}
	
	private String getTagValue( String tagName ) {
		String value 	= null;
		debug("Processing tag value experession [%s]", tagName);

		String name[] = tagName.split("\\.");
		tagName = name[1].substring(1, name[1].length()-1);
		Object[] elements = getElementSet(name[0]);
		
		Method m	= null;
		Object tags = null;
		for ( int i = 0; i < elements.length; i++ ) {
			debug(">>\t class name = %s", elements[i].getClass().getName());
			try {
				m 		= elements[i].getClass().getMethod("GetTaggedValues");
				tags 	= m.invoke(elements[i], null);
				TaggedValue tag = (TaggedValue)(((Collection)tags).GetByName(tagName));
				if ( tag != null ) {
					value = tag.GetValue();
				} else {
                    if ( isTextMode() )
                        error("Tag \"%s\" not found",tagName);
				}
				break;
			} catch (NoSuchMethodException e) {
				debug("\t\t No Such Method Exception");
			} catch (Exception e ) {
				e.printStackTrace(System.err);;
			}
		}
		if ( m == null )
			error("Element does not support tags.");
		
		return value;
	}

	private ExecutionState executionState = new ExecutionState();
	private Stack<ExecutionState> executionStates = new Stack<ExecutionState>(); 
	/*
	 * IF section
	 **************************************************************************/
	@Override
	public void exitIf_stmt(If_stmtContext ctx) {
		executionStates.push(executionState);
		executionState = new ExecutionState(executionState);
		if ( executionState.canProcessBranch() ) {
			executionState.setProcessBranch(evalCompareExpr(ctx.compare_expr()));
			debug("IF > %s", executionState.toString());
		} else {
			executionState.setBranchProcessed();
		}
	}

	@Override
	public void exitElseif_stmt(Elseif_stmtContext ctx) {
		if ( executionState.canProcessBranch() ) {
			executionState.setBranchProcessed();
		} else {
			executionState.setProcessBranch(evalCompareExpr(ctx.compare_expr()));
		}
		debug("ELSEIF > %s", executionState.toString());
	}

	@Override
	public void exitElse_stmt(Else_stmtContext ctx) {
		if ( executionState.canProcessBranch() ) {
			executionState.setBranchProcessed();
		} else if (!executionState.isBranchProcessed()){
			executionState.setProcessBranch(true);
		}
		debug("ELSE > %s", executionState.toString());
	}

	@Override
	public void exitEndif_stmt(Endif_stmtContext ctx) {
		executionState = executionStates.pop();
		debug("ENDIF > %s", executionState.toString());
	}
	
	private boolean evalCompareExpr( Compare_exprContext ctx ) {
		String op = null;
		boolean expressionValue = false;
		List<PredicateContext> predicates = ctx.predicate();
		
        String dbg = "";
		for ( int p = 0; p < predicates.size(); p++) {
			if ( p == 0 ) {
				expressionValue = evalPredicate(predicates.get(p));
                dbg += expressionValue;
			} else {
				op = ctx.pred_op(p-1).getText().toLowerCase();
				if ( op.compareTo("and") == 0 ) {
					expressionValue = (expressionValue && evalPredicate(predicates.get(p)));
				} else if (op.compareTo("or") == 0 ) {
					expressionValue = (expressionValue || evalPredicate(predicates.get(p)));
				} else {
					error("Unsupported operator \"%s\"\n",op);
				}
                dbg += " ["+op+"] "+expressionValue;
			}
		}
		debug("\tevalCompareExpr (%s) = %s",dbg,expressionValue);
		return expressionValue;
	}

	
	@Override
	public void exitEndtempalte_stmt(Endtempalte_stmtContext ctx) {
		if (executionState.canProcessBranch()) {
			debug(">>Removing listener...");
			flashOutput();
			parser.removeParseListener(this);
		}
	}

	private boolean evalPredicate( PredicateContext ctx ) {
		String exp1 = calcExpression(ctx.expr(0));
		String exp2 = calcExpression(ctx.expr(1));
		String op   = ctx.test_op().getText();
		
		boolean equal = false;
		if ( exp1 != null && exp2 != null ) {
			equal = (exp1.compareTo(exp2) == 0);
			
			if ( op.compareTo("!=") == 0 ) {
				equal = !equal;
			}
		}
		debug("Eval([%s] %s [%s]) is %s",exp1,op,exp2,equal);
		return equal;
	}
	/*
	 * Split section
	 **************************************************************************/
	@Override
	public void enterSplitMacro(SplitMacroContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel++;
	}
	
	@Override
	public void exitSplitMacro(SplitMacroContext ctx) {
		textLevel--;
		if ( executionState.canProcessBranch() && isTextMode() ) 
			executeSplitMacro(ctx,writer);
	}
	 
	private void executeSplitMacro(SplitMacroContext ctx, Writer writer ) {
		String stringToSplit = calcExpression(ctx.expr());
		String templateName  = translateStringLiteral(ctx.templateName().stringLiteral().getText());

		String separator = getLineSeparator();
		if (ctx.separator(0) != null ) {
			separator = calcExpression(ctx.separator(0).expr());
		}
		
		String delimiter     = ",";
		if ( ctx.delimiter(0) != null ) {
			delimiter = calcExpression(ctx.delimiter(0).expr());
		}

		String[] parts 	= stringToSplit.split(delimiter);
		
		debug("Split macro: string=[%s] @template=[%s] @delimiter=[%s] @separator=[%s]> count %d"
			,stringToSplit
			,templateName
			,delimiter
			,separator
			,parts.length
		);

		TemplateProcessor tp 	= new TemplateProcessor(templateName);
		tp.setElement(element);

		//Set parameters
		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExprContext> parms = ctx.templateParameters(0).parameters().expr();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				debug("\tset parameter $%d = [%s]",i+1,value);
				tp.addParameter(value);
			}
		}
		
		//Execute template for each element
		StringWriter sw;
		StringBuffer sb;
		for ( int i = 0, w = 0; i < parts.length; i++ ) {
			sw = new StringWriter();
			tp.setOutput(sw);
			tp.setVariable("$PART",parts[i]);
			tp.execute();
			sb = sw.getBuffer();
			try {
				if ( sb.toString().trim().length() > 0 ) {
					if ( w != 0 )	writer.write(separator);
					writer.write(sb.toString());
					w++;
				}
			} catch (IOException e ) {
				error("Cannot write to output stream");
				break;
			}
		}
	}
	 
	/*
	 * List section
	 **************************************************************************/
	@Override
	public void enterListMacro(ListMacroContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel++;
	}
	
	@Override
	public void exitListMacro(ListMacroContext ctx) {
		textLevel--;
		if ( executionState.canProcessBranch() && isTextMode() ) 
			executeListMacro(ctx,writer);
	}

	private void executeListMacro(ListMacroContext ctx, Writer writer ) {
		String attr = ctx.attribute().getText();
		String name = translateStringLiteral(ctx.templateName().stringLiteral().getText());

		debug("List macro: attribute=[%s] template=[%s]",attr,name);

		Object attribute = getAttribute(attr);
		if (attribute == null)  { 
			return;
		}

		debug("\tattribute.Class = [%s]"
				,attribute.getClass().getName()
		);
		if ( !(attribute instanceof Collection) ) {
			error("Attribute \"%s\" is not a Collection\n",attr);
			attribute = null;
			return;
		}
		
		TemplateProcessor tp 	= new TemplateProcessor(name);

		Collection	ec 			= (Collection)attribute;
		short		ecCount	 	= ec.GetCount();
		Object		obj			= null;
		debug("\tCount = [%d]",ecCount);

		//Set parameters
		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExprContext> parms = ctx.templateParameters(0).parameters().expr();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				debug("\t\tset parameter $%d = [%s]",i+1,value);
				tp.addParameter(value);
			}
		}
		String separator = getLineSeparator();
		if (ctx.separator(0) != null ) {
			separator = calcExpression(ctx.separator(0).expr());
		}
		
		//Execute template for each element
		StringWriter sw;
		StringBuffer sb;
		for ( short i = 0, w = 0; i < ecCount; i++ ) {
			obj = ec.GetAt(i);
			sw = new StringWriter();
			tp.setOutput(sw);
			tp.setElement(obj);
			tp.execute();
			sb = sw.getBuffer();
			try {
				if ( sb.toString().trim().length() > 0 ) {
					if ( w != 0 )	writer.write(separator);
					writer.write(sb.toString());
					w++;
				}
			} catch (IOException e ) {
				error("Cannot write to output stream");
				break;
			}
			obj = null;
		}
		obj = null;
		ec 	= null;
		System.gc();		
	}
	/*
	 * Functions section
	 **************************************************************************/
	@Override
	public void enterFunctions(FunctionsContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel++;
	}
	
	@Override
	public void exitFunctions(FunctionsContext ctx) {
		if ( executionState.canProcessBranch() ) {
			textLevel--;
			if ( isTextMode() ) {
				String value = calcFunction(ctx);
				if ( value != null ) {
					sendTextOut(value, ctx);
				}
			}
		}
	}

	/*
	 * Text section
	 **************************************************************************/
	private boolean isTextMode() {
		return (textLevel == 1 );
	}
	private void sendTextOut(String text, ParserRuleContext ctx) {
			if (isTextMode() && text != null) {
				Token token = ctx.getStart();
				int   idx  = token.getTokenIndex();
				List<Token> channel = tokens.getHiddenTokensToLeft(idx, 1);
				if (channel != null ) {
					String ws = "";
					for(int i = 0; i < channel.size(); i++) {
						token = channel.get(i);
						if ( token == null ) break;
						ws += token.getText();
						i++;
					};
					writeText(ws);
				}
				writeText(text);
			}
	}
	
	private void finishLine() {
		if (isTextMode()) {
			//debug("Finish line with [%s]", lineSeparator);
			writeText(lineSeparator);
		}
	}

	private void writeText(String text) {
		try {
			writer.write(text);
		} catch(IOException e) {
			error("Cannot write to output stream!");
			parser.removeParseListener(this);
		}
	}
	
	private void flashOutput() {
		try {
			writer.flush();
		} catch(IOException e) {
			
		}
	}
	
	@Override
	public void enterText(TextContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel++;
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(getVariableValue(ctx.VAR().getText()),ctx);
	}

	@Override
	public void exitFreeText(FreeTextContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) {
			//V 0.21 
			//replaced ctx.FreeText().getText() by ctx.getText() as grammar now 
			//also includes Pred_op (and/or) in addition to FreeText
			sendTextOut(ctx.getText(),ctx);
		}
	}

	@Override
	public void exitStringLiteral(StringLiteralContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(ctx.StringLiteral().getText(),ctx);
	}
	
	@Override
	public void exitAttribute(AttributeContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(getAttributeValue(ctx.getText()),ctx);
	}

	@Override
	public void exitTag(TagContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(getTagValue(ctx.getText()),ctx);
	}
	
	@Override
	public void exitParameter(ParameterContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(getParameter(ctx.getText()),ctx);
	}
	
	@Override
	public void exitTextMacros(TextMacrosContext ctx) {
		if ( executionState.canProcessBranch() && isTextMode() ) 
			sendTextOut(TextMacros.get(ctx.getText()),ctx);
	}

	
	
	@Override
	public void enterPiMacro(PiMacroContext ctx) {
		textLevel++;
	}
	@Override
	public void exitPiMacro(PiMacroContext ctx) {
		setLineSeparator(translateStringLiteral(ctx.stringLiteral().getText()));
		textLevel--;
	}

	/*
	 * Call Macro Section 
	 **************************************************************************/
	@Override
	public void enterCallMacro(CallMacroContext ctx) {
		textLevel++;
	}

	@Override
	public void exitCallMacro(CallMacroContext ctx) {
		textLevel--;
		if ( executionState.canProcessBranch() && isTextMode() ) 
			executeCallMacro(ctx,writer);
	}
	
    private Object getElementInScope(ElementInScopeContext ctx) {
        Object obj = null;
		if ( ctx != null ) {
			String en = "UNKNOWN";
			if ( ctx.SRCE() != null ) {
				obj = this.getSource();
				en  = "source";
			} else if ( ctx.TRGT() != null ) {
				obj = this.getTarget();
				en  = "target";
			} else if ( ctx.PCKG() != null ) {
				obj = this.getPackage();
				en  = "package";
			} else if ( ctx.PARN() != null ) {
				obj = this.getParent();
				en  = "parent";
			}
			if ( obj == null ) {
				error("Element does not have the \"%s\" property!", en);
			}
		}
        return obj;
    }
	private void executeCallMacro( CallMacroContext ctx, Writer writer ) {

		String name = translateStringLiteral(ctx.stringLiteral().getText());
		
		debug(">> Opening template [%s]...", name);
		
		TemplateProcessor tp = new TemplateProcessor(name);
		tp.setOutput(writer);

		Object e = getElementInScope(ctx.elementInScope(0));
        if ( e == null ) e = element;
		
		tp.setElement(e);

		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExprContext> parms = ctx.templateParameters(0).parameters().expr();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				debug("\t\tset parameter $%d = [%s]",i,value);
				tp.addParameter(value);
			}
		}

		tp.execute();
	}
	
	@Override
	public void exitLine_text(Line_textContext ctx) {
		if ( ctx.NL() != null )
			finishLine();
		textLevel = 0;
	}
	
	@Override
	public void exitFile(FileContext ctx) {
		flashOutput();
	}
	
	

}
