package com.github.dtsarkov.ea.tools.load.json;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.github.dtsarkov.ea.tools.Logger;
import com.github.dtsarkov.ea.tools.ParserErrorListener;
import com.github.dtsarkov.ea.tools.codegenerator.Utils;
import com.github.dtsarkov.ea.tools.load.IImportFileParser;
import com.github.dtsarkov.ea.tools.load.ImportElement;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONBaseListener;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONLexer;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONParser;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONParser.FileContext;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONParser.ObjectContext;
import com.github.dtsarkov.ea.tools.load.json.parser.JSONParser.PairContext;

public class JSONProcessor extends JSONBaseListener implements IImportFileParser {
	
//element = JSON Object
//attribute = Name : Value	
//collection attribute = Name : JSON Array of JSON Objects
	
	
	private JSONParser			parser;
	private ArrayList<ImportElement> 	elements;
	
	public JSONProcessor() {
		this.elements	 	= new ArrayList<ImportElement>(10);
	}
	
	public ArrayList<ImportElement> execute(String fileName) {
		try {
			InputStream 		is 		= new FileInputStream(fileName);
			ANTLRInputStream  	input 	= new ANTLRInputStream(is);
			JSONLexer 			lexer 	= new JSONLexer(input);
			CommonTokenStream	tokens	= new CommonTokenStream(lexer);
			
			parser 	= new JSONParser(tokens);
			parser.addErrorListener(new ParserErrorListener());
			//parser.addParseListener(this);
			
			FileContext file = parser.file();
			for (int i = 0; i < file.object().size(); i++ ) {
				elements.add(processObject(file.object(i)));
			}
			
		} catch ( FileNotFoundException e ) {
			Logger.error("Could not find file [%s]",fileName);
		} catch ( IOException e ) {
			Logger.error("Could not read from file [%s]\n",fileName);
		}
		return elements;
	}
	
	private ImportElement processObject(ObjectContext ctx) {
		
		ImportElement element = new ImportElement();
		Logger.debug("Process element : %s",element.getName());
		PairContext pc;
		String 		attributeName;
		Object		attributeValue;

		for( int i = 0; i < ctx.pair().size(); i++) {
			pc = ctx.pair(i);
			attributeName 	= pc.STRING().getText().substring(1);
			attributeName 	= attributeName.substring(0,attributeName.length()-1);
			attributeValue 	= processValue(pc.value());

			Logger.debug("  %s => [%s]",attributeName,attributeValue);
			element.addAttriubute(attributeName, attributeValue);
		}
		
		return element;
	}
	
	private Object processValue(JSONParser.ValueContext ctx ) {
		Object valueObject 	= ctx.getChild(0);
		Object value = null;

		if ( valueObject instanceof TerminalNodeImpl ) {
			int valueType =((TerminalNodeImpl)valueObject).getSymbol().getType();
			String valueText =((TerminalNodeImpl)valueObject).getText();
			switch (valueType) {
				case JSONParser.STRING :
					value = Utils.translateStringLiteral(valueText);
					break;
				case JSONParser.INTEGER :
					value = new Integer(valueText);
					break;
				case JSONParser.DOUBLE :
					value = new Double(valueText);
				case JSONParser.BOOLEAN:
					value = new Boolean(valueText);
					break;
			}
		} else if ( valueObject instanceof JSONParser.ArrayContext ) {
			value = processArray((JSONParser.ArrayContext)valueObject);
		} else if ( valueObject instanceof JSONParser.ObjectContext ) {
			value = processObject((JSONParser.ObjectContext)valueObject);
		} else {
			//TODO: Raise an exception
		}
		return value;
	}

	private ArrayList<Object> processArray(JSONParser.ArrayContext ctx) {
		int	count = ctx.value().size();
		ArrayList<Object> array = new ArrayList<Object>(ctx.getChildCount());
		
		for( int i = 0; i < count; i++) {
			array.add(processValue(ctx.value(i)));
		}
		return array;
	}
}
