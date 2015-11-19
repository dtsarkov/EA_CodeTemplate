package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateLexer;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser;

public class Generator {

	public static void main(String[] args) throws Exception {

		if (args.length == 0 ) return;
		
		String inputFileName = args[0];
		
		InputStream 			is 		= new FileInputStream(inputFileName);
		
		ANTLRInputStream  		input 	= new ANTLRInputStream(is);
		EACodeTemplateLexer 	lexer 	= new EACodeTemplateLexer(input);
		CommonTokenStream 		tokens	= new CommonTokenStream(lexer);
		EACodeTemplateParser 	parser 	= new EACodeTemplateParser(tokens);

		TreeListener			listener = new TreeListener(parser);
		
		parser.addParseListener(listener);
		ParseTree				tree	= parser.file();
		
		
	}

}
