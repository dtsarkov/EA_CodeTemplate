package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.sparx.Collection;
import org.sparx.Repository;

import com.github.dtsarkov.ea.tools.EA;
import com.github.dtsarkov.ea.tools.Logger;
import com.github.dtsarkov.ea.tools.ParserErrorListener;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateBaseListener;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateLexer;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.AssignmentContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.AttributeContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.CallMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Compare_exprContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ConditionsContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ElementInScopeContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Else_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Elseif_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Endif_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Endtempalte_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ExprContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ExpressionContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FileContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FileMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FilterMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FreeTextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FunctionsContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.If_stmtContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.Line_textContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ListMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.MacrosContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.OverrideContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ParameterContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PiMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PredicateContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.QueryMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.SeparatorContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.SplitMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.StringLiteralContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TagContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TemplateNameContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TemplateParametersContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextMacrosContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.VariableContext;

public class TemplateProcessor extends EACodeTemplateBaseListener {
	/* Static section  
	 **************************************************************************/
	private static final String BREAK = "___BREAK___";
	private static final String NL = "\n";
	private static final String CR = "\r";
	
	private static String 		templateExtention;
	private static String		outputFolder;

	private static Repository 	EA_Model;
	
	private static Map<String,String> TextMacros;
	private static Map<String,String> globalVariables;
	private static Compare_exprContext globalFilter = null;
	
	static {
		templateExtention 	= ".template";
		outputFolder		= ".";
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
		Date date 			= Calendar.getInstance().getTime();
		
		globalVariables = new HashMap<String,String>();
		
		TextMacros = new HashMap<String,String>();
		TextMacros.put("%eq%", "=");
		TextMacros.put("%pc%", "%");
		TextMacros.put("%dl%", "$");
		TextMacros.put("%qt%", "\"");
		TextMacros.put("%us%", "_");
		TextMacros.put("%nl%",System.lineSeparator());
		TextMacros.put("%DATE%", df.format(date));
		TextMacros.put("%TIME%", dt.format(date));
		TextMacros.put("%USER%", "");
	}
	
	static public void setTemplateExtention( String extention ) {
		templateExtention = (extention.length() == 0) ? "" : "."+extention;
	}
	static public String getTemplateExtention() {
		return templateExtention;
	}
	

	public static String getOutputFolder() {
		return outputFolder;
	}
	public static void setOutputFolder(String outputFolder) {
		TemplateProcessor.outputFolder = outputFolder;
	}

	static public void setEAModel( Repository model ) {
		EA_Model = model;
	}
	static public Repository getEAModel() {
		return EA_Model;
	}
	
	public static void addVariable(String variable, String value) {
		globalVariables.put(variable, value);
	}
	/*
	 * 
	 **************************************************************************/
	private String 					templateFolder;
	private String 					templateName;
	private boolean					isOpen;
	private EACodeTemplateParser 	parser;
	private CommonTokenStream 		tokens;
	private EACodeTemplateLexer 	lexer;
	private EAElement		 		element;
	private String					lineSeparator = System.lineSeparator();
	private int						textLevel 	= 0;
	private Writer 					writer;
	private boolean					canRedirectOutput = true;
	private String					outputFileName;

	private ArrayList<String>  parameters	  = new ArrayList<String>(9);
	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor( String templateName ) {
		this(templateName, null);
	}

	public TemplateProcessor( String templateName, String templateFolder ) {
		this.templateName 	= templateName;
		setTemplateFolder(templateFolder);
		writer = new PrintWriter(System.out);
	}

	public void setTemplateFolder( String path ) {
		File file = new File(
				((path == null) ? "." : path)
				+File.separator
				+templateName+templateExtention
		);
		this.templateFolder = file.getParent();
		this.templateName	= file.getName();

		int idx = this.templateName.lastIndexOf(".");
		
		if ( idx != -1 ) {
			this.templateName = this.templateName.substring(0, idx);
		}
	}
	
	public String getTemplateFolder( ) {
		return templateFolder;
	}
	
	private String getTemplateFullName(String templateName) {
		return templateFolder+File.separator+templateName+templateExtention;
	}
	
	public boolean existTemplate(String templateName ) {
		File file = new File(getTemplateFullName(templateName));
		return file.exists();
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
		Logger.debug(">> Get Template Parameter [%d] = [%s]", index, value);		
		return value;
	}
	
	public void setOutput(Writer writer) {
		this.writer = writer;
	}
	public Writer getOutput() {
		return writer;
	}

	public void setOutputFilename( String fileName ) {
		this.outputFileName = fileName;
	}
	
	public String getOutputFilename() {
		return this.outputFileName;
	}
	
	public boolean isRedirectOutputEnabled() {
		return canRedirectOutput;
	}
	public void enableRedirectOutput(boolean enableRedirectOutput) {
		this.canRedirectOutput = enableRedirectOutput;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}
	public void setLineSeparator(String lineSeparator) {
		Logger.debug(">> Set Line separator [%s]", lineSeparator);
		this.lineSeparator = lineSeparator;
	}

	/*
	 * 
	 **************************************************************************/
	private boolean openTemplateFile() {
		String 	fileName = getTemplateFullName(templateName);
		boolean success = false;
		try {
			InputStream 			is 		= new FileInputStream(fileName);
			ANTLRInputStream  		input 	= new ANTLRInputStream(is);
			
			lexer 	= new EACodeTemplateLexer(input);
			tokens	= new CommonTokenStream(lexer);
			parser 	= new EACodeTemplateParser(tokens);

			success = true;
			
		} catch ( FileNotFoundException e ) {
			Logger.error("Could not find template file [%s]",fileName);
		} catch ( IOException e ) {
			Logger.error("Could not read from template file [%s]",fileName);
		}
		return success;
	}
	
	public void execute() {
		if ( !isOpen ) {
			isOpen = openTemplateFile();
			if ( !isOpen ) return;
		}
		if ( Logger.getVerbose() ) {
			Logger.message("%-30s|%-20s|%-30s"
					,element.getAttribute("$.Name",null)
					,element.getAttribute("$.Type",null)
					,this.templateName
			);
		}
		parser.addParseListener(this);
		parser.removeErrorListeners();
		parser.addErrorListener(new ParserErrorListener());
		parser.file();
		parser.removeParseListeners();
		parser.reset();
		
	}
	
	public void setElement(Object element) {
		this.element = new EAElement(element);
	}
	
	public void setElement(EAElement element) {
		this.element = element;
	}
	
	/*
	 * 
	 **************************************************************************/
	 public void setVariable( String name, String value ) {
		localVariables.put(name,value);
	 }

	private boolean inAssignmentMode = false; 
	@Override
	public void enterAssignment(AssignmentContext ctx) {
		inAssignmentMode = true;
	}
	
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		
		if ( executionState.canProcessBranch()) {
			String variable = ctx.variable().getText();
			Map<String,String> scope;
			
			if (variable.startsWith("$$")) {
				scope = globalVariables;
			} else {
				scope = localVariables;
			}
			
			
			String value = calcExpression(ctx.expression());
			if ( value != null && ctx.op.getType() == EACodeTemplateParser.AEQ ) {
				//get variable value for '+=' assignment
				String curentValue = scope.get(variable);
				if ( curentValue != null ) //variable was defined before 
					value = curentValue + value;
			}
			Logger.debug("Assignment (%d): %s=%s",textLevel, variable,value);
			scope.put(variable, value);
		}
		inAssignmentMode = false;
	}

	private String calcExpression(ExpressionContext ctx ) {
		String value = "";
		String v;
		ExprContext c;
		for ( int i = 0; i < ctx.getChildCount(); i++ ) {
			c = ctx.expr(i);
			if ( c != null ) {
				v = calcExpression(c); 
				if ( v != null ) { 
					value += v;
				}
			}
		}
		return value;
	}

	private String calcExpression(ExprContext ctx ) {
		ParseTree 	c;
		String 		s = "", v = null;
		for ( int i = 0; i < ctx.getChildCount(); i++ ) {
			c = ctx.getChild(i);
			Logger.debug("Calc expression for class [%s]",c.getClass().toString());
			if ( c instanceof StringLiteralContext ) {
				v = Utils.translateStringLiteral(c.getText());
			} else if ( c instanceof VariableContext ) {
				v = getVariableValue(c.getText());
			} else if ( c instanceof AttributeContext ) {
				v = element.getAttributeValue((AttributeContext)c); 
			} else if ( c instanceof TagContext ) {
				v = element.getTagValue(c.getText());
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
			} else if ( c instanceof SplitMacroContext ) {
				StringWriter sw = new StringWriter(255);
				executeSplitMacro((SplitMacroContext)c, sw);
				v = sw.toString();
			} else if ( c instanceof QueryMacroContext ) {
				StringWriter sw = new StringWriter(255);
				executeQueryMacro((QueryMacroContext)c, sw);
				v = sw.toString();
			} else if ( c instanceof PiMacroContext ) {
				StringWriter sw = new StringWriter(255);
				executePiMacro((PiMacroContext)c, sw);
				v = sw.toString();
			} else if ( c instanceof TextMacrosContext ) {
				v = processTextMacro((TextMacrosContext)c);
			} else {
				Logger.debug(">> Unknown Expression context [%]",c.getClass().toString()); 
			}
			if ( v != null ) {
				s += v;
			}
		}
		return s;
	}
	
	private String calcFunction(FunctionsContext ctx ) {
		String value = null;
		String function = ctx.getChild(0).getText();
		int parmCount = ctx.parameters().expression().size();
		
		Logger.debug("Function call [%s] with [%d] parameter(s)",function,parmCount);
		if ( parmCount < 1 ) {
			Logger.error(ctx,"Function call should have at lease one parameter");
			return value;
		}
		String firstParameter = calcExpression(ctx.parameters().expression(0)); 

		if (function.equalsIgnoreCase("%LOWER(") ) {
			value = firstParameter.toLowerCase();
		} else if (function.equalsIgnoreCase("%UPPER(") ) {
			value = firstParameter.toUpperCase();
		} else if (function.equalsIgnoreCase("%TRIM(") ) {
			value = firstParameter.trim();
		} else if (function.equalsIgnoreCase("%LENGTH(") ) {
			value = String.valueOf(firstParameter.length());
		} else if (function.equalsIgnoreCase("%REPLACE(") ) {
			if (parmCount < 3 ) {
				Logger.error(ctx,"Incorrect function call %REPLACE(string, regexp, replacement )%");
				return value;
			}
			value = firstParameter.replaceAll(
						calcExpression(ctx.parameters().expression(1))
					   ,calcExpression(ctx.parameters().expression(2))
					);
		} else if (function.equalsIgnoreCase("%MID(") ) {
			if (parmCount < 2) {
				Logger.error(ctx,"Incorrect function call %MID(string, start [,count])%");
				return value;
			} 
			int start	= Integer.valueOf(calcExpression(ctx.parameters().expression(1)))-1;
			int	end 	= start; 
			if (parmCount >  2 ) {
				end	  = start+Integer.valueOf(calcExpression(ctx.parameters().expression(2)));
				if ( end > firstParameter.length() )
					end = firstParameter.length();
			} else {
				end = firstParameter.length();
			}
			value = firstParameter.substring(start, end);
			
		} else if (function.equalsIgnoreCase("%PLAIN_TEXT(") ) {
			value = EA_Model.GetFormatFromField("TXT", firstParameter);
			if (parmCount > 1 ) {
				String removeNewLines = calcExpression(ctx.parameters().expression(1));
				Logger.debug( "\tSecond parameter = [%s]", removeNewLines);
				if ( removeNewLines.equalsIgnoreCase("true") ) {
					value = value.replaceAll(CR, "").replaceAll(" *\n[ |\t]*", " ");
				}
			}
			
		} else if (function.equalsIgnoreCase("%WRAP_TEXT(") ) {
			if (parmCount < 2) {
				Logger.error(ctx,"Incorrect function call %WRAP_TEXT(string, length [,prefix [,sufix]])%");
				return value;
			}
			value = Utils.wrapText(firstParameter
					,Integer.valueOf(calcExpression(ctx.parameters().expression(1)))
					,(parmCount > 2) ? calcExpression(ctx.parameters().expression(2)) : ""
					,(parmCount > 3) ? calcExpression(ctx.parameters().expression(3)) : ""
			);
		} else if (function.equalsIgnoreCase("%DEBUG(") ) {
			Logger.debug(firstParameter);
		} else if (function.equalsIgnoreCase("%ERROR(") ) {
			Logger.error(ctx,firstParameter,false);
		} else if (function.equalsIgnoreCase("%WARNING(") ) {
			Logger.warning(ctx,firstParameter);
		} else if (function.equalsIgnoreCase("%MESSAGE(") ) {
			Logger.message(firstParameter);
		} else if (function.equalsIgnoreCase("%EXIST(") ) {
			value = Boolean.toString(existTemplate(firstParameter));
		} else if (function.equalsIgnoreCase("%DEFINED(") ) {
			value = Boolean.toString(firstParameter != null);
		} else {
			Logger.error(ctx, "Unknown function "+function+")%");
		}
		Logger.debug("\t\t value = %s", value);
		return value;
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
		if (value == null && this.isTextMode() )
			Logger.warning("Variable \"%s\" is not defined", variable);
		
		return value;
	}

	/*
	 * IF section
	 **************************************************************************/
	private ExecutionState executionState = new ExecutionState();
	private Stack<ExecutionState> executionStates = new Stack<ExecutionState>(); 

	
	@Override
	public void exitIf_stmt(If_stmtContext ctx) {
		executionStates.push(executionState);
		executionState = new ExecutionState(executionState);
		if ( executionState.canProcessBranch() ) {
			executionState.setProcessBranch(evalCompareExpr(ctx.compare_expr()));
			Logger.debug("IF > %s", executionState.toString());
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
		Logger.debug("ELSEIF > %s", executionState.toString());
	}

	@Override
	public void exitElse_stmt(Else_stmtContext ctx) {
		if ( executionState.canProcessBranch() ) {
			executionState.setBranchProcessed();
		} else if (!executionState.isBranchProcessed()){
			executionState.setProcessBranch(true);
		}
		Logger.debug("ELSE > %s", executionState.toString());
	}

	@Override
	public void exitEndif_stmt(Endif_stmtContext ctx) {
		executionState = executionStates.pop();
		Logger.debug("ENDIF > %s", executionState.toString());
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
					Logger.error(ctx,"Unsupported operator \""+op+"\"");
				}
                dbg += " ["+op+"] "+expressionValue;
			}
		}
		Logger.debug("\tevalCompareExpr (%s) = %s",dbg,expressionValue);
		return expressionValue;
	}

	
	@Override
	public void exitEndtempalte_stmt(Endtempalte_stmtContext ctx) {
		if (executionState.canProcessBranch()) {
			Logger.debug(">>Removing listener...");
			flashOutput();
			parser.removeParseListener(this);
			if ( ctx.BREAK() != null ) {
				this.setVariable(BREAK, "true");
			}
			executionStates.clear();
		}
	}

	private boolean evalPredicate( PredicateContext ctx ) {
		boolean equal = false;
		
		String exp1 = calcExpression(ctx.expression(0));
		exp1 = (exp1 == null ) ? "" : exp1;
		
		ExpressionContext expc2 = ctx.expression(1);
		if ( expc2 == null ) {
			equal = exp1.equalsIgnoreCase("true");
			Logger.debug("Eval([%s]) is %s",exp1,equal);
		} else {
			String exp2 = calcExpression(ctx.expression(1));
			exp2 = (exp2 == null ) ? "" : exp2;
			String op   = ctx.test_op().getText();
			
			if ( op.compareTo("~=") == 0 ) {
				equal = exp1.matches(exp2);
			} else {
				equal = (exp1.compareTo(exp2) == 0);
				if ( op.compareTo("!=") == 0 ) {
					equal = !equal;
				}
			}
			Logger.debug("Eval([%s] %s [%s]) is %s",exp1,op,exp2,equal);
		}
		if ( ctx.not(0) != null ) equal = !equal;
		return equal;
	}

	/*
	 * Split section
	 **************************************************************************/
	private void executeSplitMacro(SplitMacroContext ctx, Writer writer ) {
		String stringToSplit = calcExpression(ctx.expr());
		//String templateName  = translateStringLiteral(ctx.templateName().stringLiteral().getText());
		String templateName = calcExpression(ctx.templateName().expr());

		String separator = getLineSeparator();
		if (ctx.separator(0) != null ) {
			separator = calcExpression(ctx.separator(0).expression());
		}
		
		String delimiter     = ",";
		if ( ctx.delimiter(0) != null ) {
			delimiter = calcExpression(ctx.delimiter(0).expression());
		}

		ConditionsContext 	ctxConditions 	= ctx.conditions(0);
		Compare_exprContext conditions 		= null;
		if (ctxConditions != null ) {
			conditions = ctxConditions.compare_expr();
			Logger.debug("\tConditions=[%s]",conditions.getText());
		}
		
		String[] parts 	= stringToSplit.split(delimiter);
		
		Logger.debug("Split macro: string=[%s] @template=[%s] @delimiter=[%s] @separator=[%s]> count %d"
			,stringToSplit
			,templateName
			,delimiter
			,separator
			,parts.length
		);

		TemplateProcessor tp 	= new TemplateProcessor(templateName,getTemplateFolder());
		
		EAElement elementInScope = getElementInScope(ctx.elementInScope(0));
		//TODO: Add element filter
		if ( elementInScope == null ) {
			elementInScope = element;
		}

		tp.setElement(elementInScope);

		//Set parameters
		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExpressionContext> parms = ctx.templateParameters(0).parameters().expression();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				Logger.debug("\tset parameter $%d = [%s]",i+1,value);
				tp.addParameter(value);
			}
		}
		
		//Execute template for each element
		StringWriter sw;
		StringBuffer sb;
		String txt	 = "";
		String breakLoop;
		int w = 0;
		for ( int i = 0; i < parts.length; i++ ) {
			sw = new StringWriter();
			tp.setOutput(sw);
			tp.setVariable("$PART",parts[i]);
			tp.setVariable("$COUNT", Integer.toString(parts.length));
			tp.setVariable("$CURRENT", Integer.toString(i+1));
			
			if ( conditions == null || tp.evalCompareExpr(conditions) ) { 
				tp.execute();
				sb = sw.getBuffer();

				if ( sb.toString().trim().length() > 0 ) {
					if ( w != 0 )	txt += separator;
					txt += sb.toString();
					w++;
				}
				breakLoop = tp.getVariableValue(BREAK);
				if (breakLoop != null && breakLoop.equalsIgnoreCase("true")) {
					break;
				}
			}
		}
		if (writer != null ) try {
			writer.write(txt);
		} catch(IOException e){
			Logger.error(ctx,"Split Macro: Cannot write to output stream!");
		}
	}
	 
	/*
	 * List section
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	private void executeListMacro( ListMacroContext ctx, Writer writer ) {
		String attr = ctx.attribute().getText();

		Logger.debug("List macro: attribute=[%s]",attr);

		Object attribute = element.getAttribute(attr,ctx.attribute());
		if (attribute == null)  { 
			return;
		}

		Logger.debug("\tattribute.Class = [%s]",attribute.getClass().getName());
		if ( !(attribute instanceof Collection) ) {
			Logger.error(ctx,"Attribute \""+attr+"\" is not a Collection");
			attribute = null;
			return;
		}
		
		executeCollectionMacro(
				(Collection)attribute
				,ctx.templateName()
				,ctx.templateParameters(0)
				,ctx.conditions(0)
				,ctx.separator(0)
				,writer
		);

		
	}

	/*
	 * Query macro
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	private void executeQueryMacro(QueryMacroContext ctx, Writer writer ) {
		String queryName  	= calcExpression(ctx.expr());
		String searchValue 	= calcExpression(ctx.searchTerm().expression());
		
		Logger.debug("Query macro: queryName=[%s] searchValue= [%s]"
				,queryName, searchValue
		);

		try {
			Collection elementCollection = EA.model().GetElementsByQuery(queryName, searchValue);
	
			executeCollectionMacro(
					 elementCollection
					,ctx.templateName()
					,ctx.templateParameters(0)
					,ctx.conditions(0)
					,ctx.separator(0)
					,writer
			);
		} catch(Exception e) {
			Logger.error(ctx, e.getMessage());
		}
	}

	/*
	 * Collection macro
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	private void executeCollectionMacro(
			 Collection 				elementCollection
			,TemplateNameContext		ctxTemplateName
			,TemplateParametersContext	ctxTemplateParameters
			,ConditionsContext			ctxConditions
			,SeparatorContext			ctxSeparator
			,Writer 					writer 
	) {

		short		ecCount	 	= elementCollection.GetCount();
		Logger.debug("\tFound=[%d] elements", ecCount);
		
		if (ecCount == 0) return;

		Compare_exprContext conditions = null;
		if (ctxConditions != null ) {
			conditions = ctxConditions.compare_expr();
			Logger.debug("\tConditions=[%s]",conditions.getText());
		}
		
		//Prepare list of elements
		ArrayList<Object> elements = new ArrayList<Object>(ecCount);
		EAElement currentElement = this.element;

		Object obj = null;
		for ( short i = 0; i < ecCount; i++ ) {
			obj = elementCollection.GetAt(i);
			Logger.debug("\t get element class=[%s]", (obj==null) ? "null" : obj.getClass().getName());
			//TODO: Add element filter
			this.setElement(obj);
			if ( conditions == null || evalCompareExpr(conditions) ) {
				elements.add(obj);
			}
		}
		this.setElement(currentElement);
		int elementsCount = elements.size();
		Logger.debug("\tElements: total count=[%d] for processing=[%d]",ecCount, elementsCount);

		if ( elementsCount == 0 )
			return;
		
		String templateName = calcExpression(ctxTemplateName.expr());
		TemplateProcessor tp 	= new TemplateProcessor(templateName, getTemplateFolder());
		tp.setLineSeparator(this.getLineSeparator());

		//Set parameters
		if ( ctxTemplateParameters != null ) {
			String value = "";
			List<ExpressionContext> parms = ctxTemplateParameters.parameters().expression();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				Logger.debug("\tset parameter $%d = [%s]",i+1,value);
				tp.addParameter(value);
			}
		}
		
		//Set separator
		String separator = getLineSeparator();
		if (ctxSeparator != null ) {
			separator = calcExpression(ctxSeparator.expression());
		}
		
		//Execute template for each element
		StringWriter sw;
		StringBuffer sb;
		String		 breakLoop;
		for ( int i = 0, w = 0; i < elementsCount; i++ ) {
			obj = elements.get(i);
			sw = new StringWriter();
			tp.setOutput(sw);
			tp.setElement(obj);
			tp.setVariable("$COUNT", Integer.toString(elementsCount));
			tp.setVariable("$CURRENT", Integer.toString(i+1));
			tp.execute();
			sb = sw.getBuffer();
			if ( writer != null ) try {
				if ( sb.toString().trim().length() > 0 ) {
					if ( w > 0 ) writer.write(separator);
					writer.write(sb.toString());
					w++;
				}
			} catch (IOException e ) {
				Logger.error(ctxTemplateName,"Cannot write to output stream");
				break;
			}
			obj = null;
			breakLoop = tp.getVariableValue(BREAK);
			if (breakLoop != null && breakLoop.equalsIgnoreCase("true")) {
				break;
			}
		}
		obj 				= null;
		elementCollection 	= null;
		System.gc();		
	}
	
	/*
	 * Functions section
	 **************************************************************************/

	/*
	 * Filter section
	 **************************************************************************/
	@Override
	public void exitFilterMacro(FilterMacroContext ctx) {
		setFilter(ctx.compare_expr());
	}

	public void setFilter(Compare_exprContext ctx) {
		globalFilter = ctx;
	}
	
	public boolean evalFilter() {
		boolean eval = true;
		if ( globalFilter != null ) {
			
		}
		
		return eval;
	}
	/*
	 * Text section
	 **************************************************************************/
	private boolean isTextMode() {
		return (textLevel == 1 );
	}
	
	private boolean newLineSent = false;
	private int		lineLength  = 0;
	private void sendTextOut(String text, ParserRuleContext ctx) {
			if (isTextMode() && text != null) {
				newLineSent = (text.compareTo(System.lineSeparator()) == 0)
							||(text.compareTo(this.lineSeparator) 	  == 0);
				Token token = ctx.getStart();
				int   idx  = token.getTokenIndex();
				List<Token> channel = tokens.getHiddenTokensToLeft(idx, 1);
				if (channel != null ) {
					String ws = "";
					for(int i = 0; i < channel.size(); i++) {
						token = channel.get(i);
						if ( token == null ) break;
						ws += token.getText();
					};
					lineLength += ws.length();
					writeText(ws);
				}
				lineLength += text.length();
				writeText(text);
			}
	}
	
	private void finishLine() {
		if (isTextMode()) {
			if ( !newLineSent && lineLength != 0)
				writeText(lineSeparator);
			newLineSent = false;
			lineLength  = 0;
		}
	}

	private void writeText(String text) {
		if ( writer != null ) try {
			writer.write(text);
		} catch(IOException e) {
			Logger.error("Cannot write to output stream!");
			parser.removeParseListener(this);
		}
	}
	
	private void flashOutput() {
		if (writer != null ) try {
			writer.flush();
		} catch(IOException e) {
			
		}
	}
	
	private void executePiMacro(PiMacroContext ctx, Writer writer ) {
		//setLineSeparator(Utils.translateStringLiteral(ctx.stringLiteral().getText()));
		if ( writer != null ) try {
			writer.write(getLineSeparator());
		} catch (IOException e) {
			
		}
		if ( !ctx.expression().isEmpty() ) { 
			setLineSeparator(calcExpression(ctx.expression(0)));
		}
	}

	
	private int fileCounter = 0;
	private static final String MODES="override;append;new";
	@Override
	public void exitFileMacro(FileMacroContext ctx) {
		if ( !executionState.canProcessBranch()) return;

		Logger.debug("Executing exitFileMacro : canRedirectOutput = [%s], Context = [%s]"
				,isRedirectOutputEnabled() 
				,ctx.toString()
		);
		
		if ( !isRedirectOutputEnabled()) {
			// Output redirect is disabled when macro is called as part of 
			// an assignment statement
			return;
		} 
		
		String fileName = calcExpression(ctx.expr());
		File file = new File(fileName);
		if ( !file.isAbsolute() ) {
			file = new File(TemplateProcessor.getOutputFolder()+"/"+fileName);
		}
		Logger.debug("\tNew output file = [%s], Exist = %s",file.getAbsolutePath(), file.exists());
		
		String mode = "override";
		OverrideContext octx = ctx.override();
		if (octx != null ) {
			mode = calcExpression(octx.expr()).toLowerCase().trim();
			if ( !MODES.contains(mode)) {
				Logger.error(ctx,"Invalid file mode \""+mode+"\"");
				return;
			}
		}
		Logger.debug("\tFile mode = %s", mode);
		
		flashOutput();
		if ( fileCounter > 0 && writer != null ) try {
			//Close output file if it was open in this template
			writer.close();
		} catch ( IOException e ) {
			Logger.error(ctx,"Cannot close output file!");
		}
		
		if ( (mode.equalsIgnoreCase("new") && file.exists()) ) {
			//Prevent file from to be overwritten 
			setOutput(null);
		} else {
			setOutputFilename(file.getAbsolutePath());
			try {
				File folder = file.getParentFile();
				if ( folder != null && !folder.exists()) {
					//create destination folder
					folder.mkdirs();
				}
				FileWriter fw = new FileWriter(file
					, file.exists() && mode.equalsIgnoreCase("append") //Append mode
				);
				
				setOutput(fw);
				fileCounter++;
			} catch (IOException e) {
				Logger.error(ctx,"Cannot open file ["+fileName+"]");
			}
		}
	}
	
	
	@Override
	public void enterText(TextContext ctx) {
		if ( executionState.canProcessBranch() ) 
			textLevel++;
	}

	@Override
	public void exitText(TextContext ctx) {
		if ( !executionState.canProcessBranch() ) return;
		
		Logger.debug("exitText(%s) textMode = %s",ctx.getClass().getName(),this.isTextMode());
		int childCount = ctx.getChildCount();
		ParseTree c;
		for ( int i = 0; i < childCount; i++ ) {
			c = ctx.getChild(i);
			//Logger.debug("\t(%d) %s => [%s])",i,c.getClass().getName(),c.getText());
			if ( c instanceof FreeTextContext ) {
				sendTextOut(c.getText(), (FreeTextContext)c);
			} else if ( c instanceof VariableContext ) {
				sendTextOut(this.getVariableValue(c.getText()), (VariableContext)c);
			} else if ( c instanceof AttributeContext) {
				sendTextOut(element.getAttributeValue((AttributeContext)c),(AttributeContext)c);
			} else if ( c instanceof TagContext) {
				sendTextOut(element.getTagValue(c.getText()),(TagContext)c);
			} else if ( c instanceof ParameterContext) {
				sendTextOut(this.getParameter(c.getText()),(ParameterContext)c);
			} else if ( c instanceof StringLiteralContext) {
				sendTextOut(Utils.translateStringLiteral(c.getText()),(StringLiteralContext)c);
			} else if ( c instanceof MacrosContext) {
				processMacros((MacrosContext)c);
			}
		}
	}
	
	private String processTextMacro(TextMacrosContext ctx ) {
		String macro = ctx.getText();
		String text  = "";
		if ( macro.equals("%FILE%") ) {
			// not elegant but fastest way to implement FILE function
			text = this.getOutputFilename();
		} else {
			text = TextMacros.get(macro);
		}
		return text;
	}
	
	private void processMacros(MacrosContext ctx) {
		int childCount = ctx.getChildCount();
		ParseTree c;
		StringWriter sw = new StringWriter();
		for ( int i = 0; i < childCount; i++ ) {
			c = ctx.getChild(0);
			Logger.debug("\t\t(%d) %s)",i,c.getClass().getName());
			if ( c instanceof TextMacrosContext) {
				sendTextOut(processTextMacro((TextMacrosContext)c),(TextMacrosContext)c);
			} else if ( c instanceof ListMacroContext) {
				executeListMacro((ListMacroContext)c, sw);
			} else if ( c instanceof CallMacroContext) {
				executeCallMacro((CallMacroContext)c, sw);
			} else if ( c instanceof SplitMacroContext) {
				executeSplitMacro((SplitMacroContext)c, sw);
			} else if ( c instanceof QueryMacroContext) {
				executeQueryMacro((QueryMacroContext)c, sw);
			} else if ( c instanceof PiMacroContext ) {
				executePiMacro((PiMacroContext)c, null);
			} else if ( c instanceof FunctionsContext) {
				String value = calcFunction((FunctionsContext)c);
				if ( value != null ) {
					sw.write(value);
				}
			}
		}
		if ( sw.getBuffer().length() != 0 ) {
			sendTextOut(sw.getBuffer().toString(),ctx);
		}
	}
	/*
	 * Call Macro Section 
	 **************************************************************************/
    private EAElement getElementInScope(ElementInScopeContext ctx) {
    	EAElement obj = null;
		if ( ctx != null ) {
			String en = ctx.getText();
			if ( ctx.SRCE() != null ) {
				obj = element.getSource();
				en  = "source";
			} else if ( ctx.SROL() != null ) {
				obj = element.getSourceRole();
				en  = "sourceRole";
			} else if ( ctx.TRGT() != null ) {
				obj = element.getTarget();
				en  = "target";
			} else if ( ctx.TROL() != null ) {
				obj = element.getTargetRole();
				en  = "targetRole";
			} else if ( ctx.PCKG() != null ) {
				obj = element.getPackage();
				en  = "package";
			} else if ( ctx.PARN() != null ) {
				obj = element.getParent();
				en  = "parent";
			}
			if ( obj == null ) {
				Logger.error(ctx,"Element does not have the \""+en+"\" property!");
			}
		}
        return obj;
    }
    
	private void executeCallMacro( CallMacroContext ctx, Writer writer ) {

		//String name = translateStringLiteral(ctx.stringLiteral().getText());
		String templateName = calcExpression(ctx.templateName().expr());;

		Logger.debug(">> Opening template [%s]...", templateName);
		
		TemplateProcessor tp = new TemplateProcessor(templateName, getTemplateFolder());
		tp.setLineSeparator(this.getLineSeparator());
		tp.setOutput(writer);
		tp.enableRedirectOutput(!this.inAssignmentMode && this.isRedirectOutputEnabled());

		EAElement e = getElementInScope(ctx.elementInScope(0));
		//TODO: Add element filter
		if ( e == null ) {
			e = element;
		}
		
		tp.setElement(e);

		if ( ctx.templateParameters(0) != null ) {
			String value = "";
			List<ExpressionContext> parms = ctx.templateParameters(0).parameters().expression();
			for ( int i = 0; i < parms.size(); i++ ) {
				value = calcExpression(parms.get(i));
				Logger.debug("\t\tset parameter $%d = [%s]",i,value);
				tp.addParameter(value);
			}
		}

		tp.execute();
	}
	
	@Override
	public void enterLine_text(Line_textContext ctx) {
		//if ( ctx.NL() != null )
		//	finishLine();
	}
	@Override
	public void exitLine_text(Line_textContext ctx) {
		if ( ctx.NL() != null )
			finishLine();
		textLevel = 0;
	}
	
	@Override
	public void exitFile(FileContext ctx) {
		//System.out.println("Execution state stack is empyt = "+executionStates.empty());
		flashOutput();
		if ( !executionStates.empty()) {
			Logger.error("Missing %s!", "%endif%");
		}
	}
}
