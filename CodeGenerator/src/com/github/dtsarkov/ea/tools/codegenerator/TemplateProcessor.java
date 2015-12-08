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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
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

	private List<String>	   parameters	  = new ArrayList<String>(9);
	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor( String templateName ) {
		this.templateName = templateName;
		writer = new PrintWriter(System.out);
	}
	

	public void addParameter(String value) {
		parameters.add(value);
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
		message("Processing [%-30s] type [%-20s] using [%s]"
				,this.getAttribute("$.Name")
				,this.getAttribute("$.Type")
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
			try {
				Method m = element.getClass().getMethod("GetParentID", null);
				Parent = EA_Model.GetElementByID(Integer.parseInt(m.invoke(element,null).toString()));
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
	@Override
	public void enterAssignment(AssignmentContext ctx) {
		if ( !canExecute() ) return;
		
		textLevel = -1000;
	}
	
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		if ( !canExecute() ) return;
		
		String variable = ctx.variable().getText();
		Map<String,String> scope;
		
		if (variable.startsWith("$$")) {
			scope = globalVariables;
		} else {
			scope = localVariables;
		}
		
		
		String value = "";
		if ( ctx.op.getType() == EACodeTemplateParser.AEQ ) {
			value = scope.getOrDefault(variable, "");
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
			debug("Calc expression for class [%s]",getClass().toString());
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
		String value = "";
		String function = ctx.getChild(0).getText();
		int parmCount = ctx.parameters().expr().size();
		
		debug("Function call [%s] with [%d] parameter(s)",function,parmCount);
		if ( parmCount < 1 ) {
			error("Function call should have at lease one parameter");
			return value;
		}
		String firstParameter = calcExpression(ctx.parameters().expr(0)); 

		if (function.equalsIgnoreCase("%LOWER") ) {
			value = firstParameter.toLowerCase();
		} else if (function.equalsIgnoreCase("%UPPER") ) {
			value = firstParameter.toUpperCase();
		} else if (function.equalsIgnoreCase("%REPLACE") ) {
			if (parmCount < 3 ) {
				error("Incorrect function call %REPLACE( string, regexp, replacement )%\n");
				return value;
			}
			value = firstParameter.replaceAll(
						calcExpression(ctx.parameters().expr(1))
					   ,calcExpression(ctx.parameters().expr(2))
					);
		} else {
			error("Unknown function %s\n", function);
		}
		return value;
	}
	
	private String translateStringLiteral( String s ) {
		String s1 = s.substring(1,s.length()-1) //remove leading and trailing double quotes
					 //.replaceAll("\\b", "\b")
					 .replaceAll("\\t", "\t")
					 .replaceAll("\\n", "\n")
					 .replaceAll("\\f", "\f")
					 .replaceAll("\\r", "\r")
					 .replaceAll("\\\"", "\"")
					 .replaceAll("\\'", "'")
					 .replaceAll("\\\\", "\\")
				;
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
	
	private Object getAttribute(String attributeName) {
		Object element	 = null; 
		Object attribute = null;
		
		String name[] = attributeName.split("\\.");
		debug("getAttribute([%s] [%s]",name[0],name[1]);
		if ( name[0].equalsIgnoreCase("$") || name[0].equalsIgnoreCase("$this") ) {
			element = getElement();
		} else if ( name[0].equalsIgnoreCase("$parent") ) {
			element = getParent();
		} else if ( name[0].equalsIgnoreCase("$package") ) {
			element = getPackage();
		} else if ( name[0].equalsIgnoreCase("$source") ) {
			element = getSource();
		} else if ( name[0].equalsIgnoreCase("$target") ) {
			element = getTarget();
		}
		
		String methodName = "Get"+name[1];
		if ( element != null ) try {
			Method m = element.getClass().getMethod(methodName);
			attribute = m.invoke(element, null);
		} catch (NoSuchMethodException e) {
			if ( packageElement != null ) {
				try {
					Method pm = packageElement.getClass().getMethod(methodName);
					attribute = pm.invoke(packageElement, null);
				} catch (NoSuchMethodException pe) {
					error("Could not find \"%s\" attribute",attributeName);
				} catch (Exception pe ) {
					pe.printStackTrace(System.err);;
				}
			} else {
				error("Could not find \"%s\" attribute",attributeName);
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

		tagName = tagName.substring(3,tagName.length()-1);
		debug("Getting tag [%s] value", tagName);
		
		Object obj 		= element;
		if ( element instanceof org.sparx.Package ) {
			debug("\tSwitch to Package.Element");
			obj = ((org.sparx.Package)element).GetElement();
		}

		try {
			Method m = obj.getClass().getMethod("GetTaggedValues");
			Object tags = m.invoke(obj, null);
			debug(">>\t class name = %s", tags.getClass().getName());
			TaggedValue tag = (TaggedValue)(((Collection)tags).GetByName(tagName));
			if ( tag != null ) {
				value = tag.GetValue();
			} else {
				error("Tag \"%s\" not found",tagName);
			}
		} catch (NoSuchMethodException e) {
			error("Element does not support tags.");
		} catch (Exception e ) {
			e.printStackTrace(System.err);;
		}
		return value;
	}

	/*
	 * IF section
	 **************************************************************************/
	private int 	branchLevel		  = 0;

	private boolean processBranch 	  = true;
	private boolean isBranchProcessed  = false;
	//TODO: Replace by stack to implement nested ifs
	@Override
	public void exitIf_stmt(If_stmtContext ctx) {
		branchLevel++;
		isBranchProcessed  = false;
		processBranch = evalCompareExpr(ctx.compare_expr());
	}

	@Override
	public void exitElseif_stmt(Elseif_stmtContext ctx) {
		isBranchProcessed = processBranch;
		if ( !processBranch ) {
			processBranch = evalCompareExpr(ctx.compare_expr());
		}
	}

	@Override
	public void exitElse_stmt(Else_stmtContext ctx) {
		isBranchProcessed = processBranch;
		processBranch = !isBranchProcessed;
	}

	@Override
	public void exitEndif_stmt(Endif_stmtContext ctx) {
		processBranch 	  = true;
		isBranchProcessed = false;
		branchLevel--;
	}
	
	private boolean canExecute() {
		return (processBranch && !isBranchProcessed );
	}
	
	private boolean evalCompareExpr( Compare_exprContext ctx ) {
		String op = null;
		boolean expressionValue = false;
		List<PredicateContext> predicates = ctx.predicate();
		
		int o = -1;
		for ( int p = 0; p < predicates.size(); p++ ) {
			if ( o == -1 ) {
				expressionValue = evalPredicate(predicates.get(p));
			} else {
				op = ctx.pred_op(o).getText().toLowerCase();
				if ( op.compareTo("and") == 0 ) {
					expressionValue &= evalPredicate(predicates.get(p));
				} else if (op.compareTo("or") == 0 ) {
					expressionValue |= evalPredicate(predicates.get(p));
				} else {
					error("Unsupported operator \"%s\"\n",op);
				}
			}
		}
		debug("\tevalCompareExpr = "+expressionValue);
		return expressionValue;
	}

	
	@Override
	public void exitEndtempalte_stmt(Endtempalte_stmtContext ctx) {
		if (processBranch) {
			debug(">>Removing listener...");
			flashOutput();
			parser.removeParseListener(this);
		}
	}

	private boolean evalPredicate( PredicateContext ctx ) {
		String exp1 = calcExpression(ctx.expr(0));
		String exp2 = calcExpression(ctx.expr(1));
		String op   = ctx.test_op().getText();
		
		debug("Eval([%s] %s [%s]) is ",exp1,op,exp2);
		
		if ( exp1 == null || exp2 == null ) {
			debug("\t\tfalse");
			return false;
		}
		
		boolean equal = (exp1.compareTo(exp2) == 0);
		
		if ( op.compareTo("!=") == 0 ) {
			equal = !equal;
		}

		debug("\t\t%s",equal);
		return equal;
	}
	/*
	 * List section
	 **************************************************************************/
	@Override
	public void enterListMacro(ListMacroContext ctx) {
		if ( !canExecute() ) return;
		textLevel++;
	}
	
	@Override
	public void exitListMacro(ListMacroContext ctx) {
		if ( !canExecute() ) return;
		textLevel--;
		String attr = ctx.attribute().getText();
		String name = translateStringLiteral(ctx.templateName().stringLiteral().getText());

		debug("List macro: attribute=[%s] template=[%s]",attr,name);

		Object attribute = getAttribute(attr);
		if (attribute == null)  return;

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

		
		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExprContext> parms = ctx.templateParameters(0).parameters().expr();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				debug("\t\tset parameter $%d = [%s]",i,value);
				tp.addParameter(value);
			}
		}
		String separator = getLineSeparator();
		if (ctx.separator(0) != null ) {
			separator = calcExpression(ctx.separator(0).expr());
		}
		
		StringWriter sw;
		StringBuffer sb;
		for ( short i = 0; i < ecCount; i++ ) {
			obj = ec.GetAt(i);
			sw = new StringWriter();
			tp.setOutput(sw);
			tp.setElement(obj);
			tp.execute();
			sb = sw.getBuffer();
			if ( sb.length() > 0 ) {
				writeText(sb.toString());
				if ( i < ecCount - 1 )
					writeText(separator);
			}
			obj = null;
		}
		ec = null;
		System.gc();
	}
	
	/*
	 * Functions section
	 **************************************************************************/
	@Override
	public void enterFunctions(FunctionsContext ctx) {
		if ( !canExecute() ) return;
		
		textLevel++;
	}
	
	@Override
	public void exitFunctions(FunctionsContext ctx) {
		if ( !canExecute() ) return;
		
		textLevel--;
		if ( isTextMode() ) sendTextOut(calcFunction(ctx), ctx);
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
			debug("Finish line with [%s]", lineSeparator);
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
		if ( !canExecute() ) return;
		textLevel++;
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(getVariableValue(ctx.VAR().getText()),ctx);
	}

	@Override
	public void exitFreeText(FreeTextContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(ctx.FreeText().getText(),ctx);
	}

	@Override
	public void exitStringLiteral(StringLiteralContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(ctx.StringLiteral().getText(),ctx);
	}
	
	@Override
	public void exitAttribute(AttributeContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		if (isTextMode()) sendTextOut(getAttributeValue(ctx.getText()),ctx);
	}

	
	@Override
	public void exitParameter(ParameterContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(getParameter(ctx.getText()),ctx);
	}
	
	@Override
	public void exitTextMacros(TextMacrosContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(TextMacros.getOrDefault(ctx.getText(),""),ctx);
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
	
	@Override
	public void exitCallMacro(CallMacroContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;

		String name = translateStringLiteral(ctx.stringLiteral().getText());
		
		debug("Opening template [%s]...", name);
		TemplateProcessor tp = new TemplateProcessor(name);
		tp.setElement(element);
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
