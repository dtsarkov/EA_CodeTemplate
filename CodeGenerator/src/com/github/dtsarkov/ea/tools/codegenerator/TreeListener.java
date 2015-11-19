package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateBaseListener;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.AssignmentContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.ExprContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.FreeTextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.LineContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.StringLiteralContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.TextContext;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser.VariableContext;

public class TreeListener extends EACodeTemplateBaseListener {
	
	private EACodeTemplateParser parser;
	private PrintStream os = System.out;

	private boolean isText = false;
	private StringBuffer  buffer = new StringBuffer();
	
	private Map<String,String> variables = new HashMap<String,String>();

	public TreeListener(EACodeTemplateParser parser) {
		this.parser = parser;
	}
//	@Override
//	public void enterLine(LineContext ctx) {
//		isText = true;
//		super.enterLine(ctx);
//	}
	
	@Override
	public void exitAssignment(AssignmentContext ctx) {
		String variable = ctx.variable().getText();
		ExprContext c;
		
		String value = "";
		if ( ctx.op.getType() == parser.AEQ ) {
			value = variables.getOrDefault(variable, "");
		}
		
		for ( int i = 0; i < ctx.expression().getChildCount(); i++ ) {
			c = ctx.expression().expr(i);
			if ( c != null )
				value += calcExpression(c);
		}
		System.out.println(">>Assignment: " + variable + "=" + value);
		variables.put(variable, value);
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
				s += variables.getOrDefault(c.getText(),"");
			}
		}
		return s;
	}
	
	private String translateStringLiteral( String s ) {
		return s.replaceAll("\"", "");
	}
	
	@Override
	public void enterText(TextContext ctx) {
		isText = true;
	}


	@Override
	public void exitVariable(VariableContext ctx) {
		if ( isText ) {
			os.print(variables.getOrDefault(ctx.VAR().getText(), ""));
		}
	}

	@Override
	public void exitFreeText(FreeTextContext ctx) {
		System.out.print(ctx.FreeText().getText()+" ");
	}

	@Override
	public void exitStringLiteral(StringLiteralContext ctx) {
		if ( isText )
			System.out.print("|"+ctx.StringLiteral().getText()+"| ");
	}

	@Override
	public void exitLine(LineContext ctx) {
		if ( isText ) {
			os.println("");//buffer);
		}
		isText = false;

	}

}
