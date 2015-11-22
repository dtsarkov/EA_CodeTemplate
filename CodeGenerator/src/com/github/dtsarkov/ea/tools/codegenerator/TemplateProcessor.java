package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sparx.Element;

import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateBaseListener;
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
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.PredicateContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.StringLiteralContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextMacrosContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.VariableContext;

public class TemplateProcessor extends EACodeTemplateBaseListener {
	private static Map<String,String> TextMacros;
	static {
		TextMacros = new HashMap<String,String>();
		TextMacros.put("%eq%", "=");
		TextMacros.put("%pc%", "%");
		TextMacros.put("%dl%", "$");
		TextMacros.put("%qt%", "\"");
		//TextMacros.put("", "");
	}
	
	private EACodeTemplateParser parser;
	private Element 			 element;
	private PrintStream os = System.out;

	private boolean isText = false;
	private Map<String,String> localVariables = new HashMap<String,String>();

	public TemplateProcessor(EACodeTemplateParser parser, Element element) {
		this.parser  = parser;
		this.element = element;
	}
	
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		String variable = ctx.variable().getText();
		ExprContext c;
		
		String value = "";
		if ( ctx.op.getType() == parser.AEQ ) {
			value = localVariables.getOrDefault(variable, "");
		}
		
		for ( int i = 0; i < ctx.expression().getChildCount(); i++ ) {
			c = ctx.expression().expr(i);
			if ( c != null )
				value += calcExpression(c);
		}
		System.out.println(">>Assignment: " + variable + "=" + value);
		localVariables.put(variable, value);
	}

	private String calcExpression(ExprContext ctx ) {
		ParseTree 	c;
		String 		s = "";
		for ( int i = 0; i < ctx.getChildCount(); i++ ) {
			c = ctx.getChild(i);
			//System.out.println(" "+c.getClass().toString());
			if ( c instanceof StringLiteralContext ) {
				s += translateStringLiteral(c.getText());
			} else if ( c instanceof VariableContext ) {
				s += localVariables.getOrDefault(c.getText(),"");
			} else if ( c instanceof AttributeContext ) {
				s += getAttributeValue(c.getText());
			}
		}
		return s;
	}
	
	private String translateStringLiteral( String s ) {
		//TODO: Translate escape codes
		return s.replaceAll("\"", "");
	}
	
	private String getAttributeValue(String attributeName) {
		String value = "";
		
		try {
			Method m = element.getClass().getMethod("Get"+attributeName.substring(2));
			value = m.invoke(element, null).toString();
			//System.out.printf("Attribute value = [%s]\n", value);
		} catch (NoSuchMethodException e) {
			System.err.printf("Could not find \"%s\" attribute\n",attributeName);
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
		return expressionValue;
	}

	
	@Override
	public void exitEndtempalte_stmt(Endtempalte_stmtContext ctx) {
		System.out.println(">>Removing listener...");
		parser.removeParseListener(this);
	}

	private boolean evalPredicate( PredicateContext ctx ) {
		String exp1 = calcExpression(ctx.expr(0));
		String exp2 = calcExpression(ctx.expr(1));
		boolean equal = exp1.compareTo(exp2) == 0;
		
		System.out.printf(">>Compare [%s] with [%s] == %s\n",exp1,exp2,equal);
		
		if ( ctx.test_op().getText().compareTo("==") == 0 ) 
			return equal;
		
		return !equal;
	}
	/*
	 * Text section
	 **************************************************************************/
	private void sendTextOut(String text) {
		if (isText && processBranch && !isBranchProcessed)
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
		sendTextOut(localVariables.getOrDefault(ctx.VAR().getText(), ""));
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
		sendTextOut(getAttributeValue(ctx.getText()));
	}

	@Override
	public void exitTextMacros(TextMacrosContext ctx) {
		sendTextOut(TextMacros.getOrDefault(ctx.getText(),""));
	}

	@Override
	public void exitLine(LineContext ctx) {
		finishLine();
		isText = false;
	}

}
