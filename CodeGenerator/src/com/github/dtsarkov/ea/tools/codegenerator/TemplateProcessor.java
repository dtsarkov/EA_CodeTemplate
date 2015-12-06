package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
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
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.LineContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ListMacroContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PredicateContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.StringLiteralContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TagContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TemplateSubstitutionContext;
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
	
	/*
	 * 
	 **************************************************************************/
	private String 					templateName;
	private boolean					isOpen;
	private EACodeTemplateParser 	parser;
	private CommonTokenStream 		tokens;
	private EACodeTemplateLexer 	lexer;
	private Object 			 		element;
	private int						textLevel 	= 0;
	private Writer 					writer;

	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor( String templateName ) {
		this.templateName = templateName;
		writer = new PrintWriter(System.out);
	}
	
	/*
	public TemplateProcessor(EACodeTemplateParser parser, Element element) {
		this.parser  = parser;
		this.element = element;
	}
	*/
	
	public void setOutput(Writer writer) {
		this.writer = writer;
	}
	
	public Writer getOutput() {
		return writer;
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
			System.err.printf("Could not find template file [%s]\n",fileName);
		} catch ( IOException e ) {
			System.err.printf("Could not read from template file [%s]\n",fileName);
		}
		return success;
	}
	
	public void execute() {
		if ( !isOpen ) {
			isOpen = openTemplateFile();
			if ( !isOpen ) return;
		}
		
		parser.addParseListener(this);
		parser.file();
		parser.removeParseListeners();
		parser.reset();
	}
	
	public void setElement(Object element) {
		this.element = element;
		if ( element instanceof Element ) {
			if ( ((Element)element).GetType().equalsIgnoreCase("Package") ) {
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
		System.out.printf(">>Assignment (%d): %s=%s\n",textLevel, variable,value);
		scope.put(variable, value);
		
		textLevel = 0;
	}

	private String calcExpression(ExprContext ctx ) {
		ParseTree 	c;
		String 		s = "", v = null;
		for ( int i = 0; i < ctx.getChildCount(); i++ ) {
			c = ctx.getChild(i);
			//System.out.println(" "+c.getClass().toString());
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
		int parmCount = ctx.expr().size();
		
		System.out.printf(">>Function call [%s] with [%d] parameter(s)\n",function,parmCount);
		if ( parmCount < 1 ) {
			System.err.println("Function call should have at lease one parameter");
			return value;
		}
		String firstParameter = calcExpression(ctx.expr(0)); 

		if (function.equalsIgnoreCase("%LOWER") ) {
			value = firstParameter.toLowerCase();
		} else if (function.equalsIgnoreCase("%UPPER") ) {
			value = firstParameter.toUpperCase();
		} else if (function.equalsIgnoreCase("%REPLACE") ) {
			if (parmCount < 3 ) {
				System.err.println("Incorrect function call %REPLACE( string, regexp, replacement )%\n");
				return value;
			}
			value = firstParameter.replaceAll(
						calcExpression(ctx.expr(1))
					   ,calcExpression(ctx.expr(2))
					);
		} else {
			System.err.printf("Unknown function %s\n", function);
		}
		return value;
	}
	
	private String translateStringLiteral( String s ) {
		//TODO: Translate escape codes
		return s.replaceAll("\"", "");
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
		System.out.printf(">>getAttribute([%s] [%s]\n",name[0],name[1]);
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
		
		if ( element != null ) try {
			Method m = element.getClass().getMethod("Get"+name[1]);
			attribute = m.invoke(element, null);
			//System.out.printf("Attribute value = [%s]\n", value);
		} catch (NoSuchMethodException e) {
			System.err.printf("Could not find \"%s\" attribute\n",attributeName);
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
		System.out.printf(">>Getting tag [%s] value\n", tagName);
		
		Object obj 		= element;
		if ( element instanceof org.sparx.Package ) {
			System.out.println(">>\tSwitch to Package.Element");
			obj = ((org.sparx.Package)element).GetElement();
		}

		try {
			Method m = obj.getClass().getMethod("GetTaggedValues");
			Object tags = m.invoke(obj, null);
			//System.out.printf(">>\t class name = %s\n", tags.getClass().getName());
			TaggedValue tag = (TaggedValue)(((Collection)tags).GetByName(tagName));
			if ( tag != null ) {
				value = tag.GetValue();
			} else {
				System.err.printf("Tag \"%s\" not found\n",tagName);
			}
		} catch (NoSuchMethodException e) {
			System.err.printf("Element does not support tags.\n");
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
					System.err.printf("Unsupported operator \"%s\"\n",op);
				}
			}
		}
		System.out.println(">>\tevalCompareExpr = "+expressionValue);
		return expressionValue;
	}

	
	@Override
	public void exitEndtempalte_stmt(Endtempalte_stmtContext ctx) {
		if (processBranch) {
			System.out.println(">>Removing listener...");
			flashOutput();
			parser.removeParseListener(this);
		}
	}

	private boolean evalPredicate( PredicateContext ctx ) {
		String exp1 = calcExpression(ctx.expr(0));
		String exp2 = calcExpression(ctx.expr(1));
		String op   = ctx.test_op().getText();
		
		System.out.printf(">>Eval([%s] %s [%s]) is ",exp1,op,exp2);
		
		if ( exp1 == null || exp2 == null ) {
			System.out.println("false");
			return false;
		}
		
		boolean equal = (exp1.compareTo(exp2) == 0);
		
		if ( op.compareTo("!=") == 0 ) {
			equal = !equal;
		}

		System.out.println(equal);
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
		String name = ctx.templateParameter().stringLiteral().getText();
		//Remove surrounding double quotes
		name = name.substring(1,name.length()-1);

		System.out.printf(">>List macro: attribute=[%s] template=[%s]\n",attr,name);

		Object attribute = getAttribute(attr);
		if (attribute == null)  return;

		System.out.printf(">>\tattribute.Class = [%s]\n"
				,attribute.getClass().getName()
		);
		if ( !(attribute instanceof Collection) ) {
			System.err.printf("Attribute \"%s\" is not a Collection\n",attr);
			attribute = null;
			return;
		}
		
		TemplateProcessor tp 	= new TemplateProcessor(name);
		tp.setOutput(this.writer);

		Collection 		  ec 	= (Collection)attribute;
		Object			  obj	= null;

		System.out.printf(">>\tCount = [%d]\n"
				,ec.GetCount()
		);
		for ( short i = 0; i < ec.GetCount(); i++ ) {
			obj = ec.GetAt(i);
			tp.setElement(obj);
			tp.execute();
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
						//System.out.printf(">> i = %d\n",i);
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
		if (isTextMode())
			writeText("\n");
	}

	private void writeText(String text) {
		try {
			writer.write(text);
		} catch(IOException e) {
			System.err.println("Cannot write to output stream!");
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
		System.out.printf(">>Exit attribute (%d) [%s]\n",textLevel,ctx.getText());
		if (isTextMode()) sendTextOut(getAttributeValue(ctx.getText()),ctx);
	}

	@Override
	public void exitTextMacros(TextMacrosContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;
		sendTextOut(TextMacros.getOrDefault(ctx.getText(),""),ctx);
	}

	@Override
	public void exitTemplateSubstitution(TemplateSubstitutionContext ctx) {
		if ( !canExecute() || !isTextMode() ) return;

		String name = ctx.getText();
		//Remove surrounding '%' characters
		name = name.substring(1,name.length()-1);
		
		System.out.printf(">>Opening template [%s]...\n", name);
		TemplateProcessor tp = new TemplateProcessor(name);
		tp.setElement(element);
		tp.execute();
	}
	
	@Override
	public void exitLine(LineContext ctx) {
		finishLine();
		textLevel = 0;
	}
	
	@Override
	public void exitFile(FileContext ctx) {
		flashOutput();
	}
	
	

}
