package com.github.dtsarkov.ea.tools.codegenerator;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

//TODO: Remove this class and replace it by ea.tools.ParserErrorListener
public class ErrorListener extends BaseErrorListener {
	
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		
		TemplateProcessor.error("Line (%d:%d) - %s", line, charPositionInLine,msg);
		
		//super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
	}
	

}
