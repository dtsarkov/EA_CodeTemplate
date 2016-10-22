package com.github.dtsarkov.ea.tools;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ParserErrorListener extends BaseErrorListener {
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		
		Logger.error("Line (%d:%d) - %s", line, charPositionInLine,msg);
		//System.out.println(recognizer.getInputStream().getClass());
		//System.out.println(recognizer.getInputStream().getSourceName());
	}
	

}
