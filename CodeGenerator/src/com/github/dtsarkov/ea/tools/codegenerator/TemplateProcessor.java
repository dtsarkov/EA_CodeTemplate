package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
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
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FreeTextContext;
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
	private static PrintStream 	os = System.out;
	
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
	private Object 			 		element;

	private boolean isText = false;
	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor( String templateName ) {
		this.templateName = templateName;
	}
	
	/*
	public TemplateProcessor(EACodeTemplateParser parser, Element element) {
		this.parser  = parser;
		this.element = element;
	}
	*/
	/*
	 * 
	 **************************************************************************/
	private boolean openTemplateFile() {
		String 	fileName = templateFolder+"/"+templateName + templateExtention;
		boolean success = false;
		try {
			InputStream 			is 		= new FileInputStream(fileName);
			ANTLRInputStream  		input 	= new ANTLRInputStream(is);
			EACodeTemplateLexer 	lexer 	= new EACodeTemplateLexer(input);
			CommonTokenStream 		tokens	= new CommonTokenStream(lexer);
			
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
	}
	
	public Object getElement() {
		return element;
	}
	
	/*
	 * 
	 **************************************************************************/
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		String variable = ctx.variable().getText();
		Map<String,String> scope;
		
		if (variable.startsWith("$$")) {
			scope = globalVariables;
		} else {
			scope = localVariables;
		}
		
		
		String value = "";
		if ( ctx.op.getType() == parser.AEQ ) {
			value = scope.getOrDefault(variable, "");
		}
		
		ExprContext c;
		for ( int i = 0; i < ctx.expression().getChildCount(); i++ ) {
			c = ctx.expression().expr(i);
			if ( c != null )
				value += calcExpression(c);
		}
		System.out.println(">>Assignment: " + variable + "=" + value);
		scope.put(variable, value);
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
			}
			if ( v == null ) {
				s = null;
			} else {
				s += v;
			}
		}
		return s;
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
		Object attribute = null;
		
		try {
			Method m = element.getClass().getMethod("Get"+attributeName.substring(2));
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
	 * TAG section
	 **************************************************************************/
	
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
		isText = false;
	}
	
	@Override
	public void exitListMacro(ListMacroContext ctx) {
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
	}
	
	/*
	 * Text section
	 **************************************************************************/
	private void sendTextOut(String text) {
		if (isText && processBranch && !isBranchProcessed && text != null)
			os.print(text+" ");
	}
	
	private void finishLine() {
		if (isText && processBranch && !isBranchProcessed)
			os.print("\n");
	}

	@Override
	public void enterText(TextContext ctx) {
		isText = true;
	}

	@Override
	public void exitVariable(VariableContext ctx) {
		sendTextOut(getVariableValue(ctx.VAR().getText()));
	}

	@Override
	public void exitTag(TagContext ctx) {
	}
	
	@Override
	public void exitFreeText(FreeTextContext ctx) {
		sendTextOut(ctx.FreeText().getText());
	}

	@Override
	public void exitStringLiteral(StringLiteralContext ctx) {
		sendTextOut(ctx.StringLiteral().getText());
	}
	
	@Override
	public void exitAttribute(AttributeContext ctx) {
		if (isText) sendTextOut(getAttributeValue(ctx.getText()));
	}

	@Override
	public void exitTextMacros(TextMacrosContext ctx) {
		sendTextOut(TextMacros.getOrDefault(ctx.getText(),""));
	}

	@Override
	public void exitTemplateSubstitution(TemplateSubstitutionContext ctx) {
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
		isText = false;
	}

}
