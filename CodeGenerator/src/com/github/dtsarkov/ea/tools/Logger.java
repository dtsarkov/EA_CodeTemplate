package com.github.dtsarkov.ea.tools;

import org.antlr.v4.runtime.ParserRuleContext;

public class Logger {
	private static int errorCounter = 0;
	private static int warningCounter = 0;
	private static boolean verbose = false;
	static private boolean debug = false;
	
	static public void setVerbose(boolean mode) {
		verbose = mode;
	}
	static public boolean getVerbose() {
		return verbose;
	}

	static public void message(String s) {
		System.out.println(s);
	}
	static public void message(String format, Object... args) {
		message(String.format(format, args));
	}


	static public void error(ParserRuleContext ctx, String message) {
		error(ctx,message,true);
	}
	static public void error(ParserRuleContext ctx, String message,boolean printLine) {
		if ( ctx != null ) {
			error("Line (%d:%d) - %s"
				 ,ctx.getStart().getLine()
				 ,ctx.getStart().getCharPositionInLine()
				 ,message
			);
			if ( printLine ) {
				System.err.printf("   >%s\n",ctx.getText());
			}
		} else {
			error("%s",message);
		}
	}
	
	static public void error(String format, Object...args) {
		errorCounter++;
		System.err.println("ERROR: "+String.format(format, args));
	}

	static public void warning(ParserRuleContext ctx, String message) {
		warning("Line (%d:%d) - %s"
			 ,ctx.getStart().getLine()
			 ,ctx.getStart().getCharPositionInLine()
			 ,message
		);
	}
	static public void warning(String format, Object...args) {
		warningCounter++;
		System.err.println("WARNING: "+String.format(format, args));
	}
	
	static public void debug(String format, Object... args) {
		if ( debug ) {
			System.out.println(">>DBG :"+String.format(format, args));
		}
	}
	static public void setDebug(boolean mode) {
		debug = mode;
	}
	
	public static int getErrorCounter() {
		return errorCounter;
	}
	public static int getWarningCounter() {
		return warningCounter;
	}
	
	public static void Summary() {
		message("Warnings: %d\nErrors\t: %d",warningCounter,errorCounter);
	}

}
