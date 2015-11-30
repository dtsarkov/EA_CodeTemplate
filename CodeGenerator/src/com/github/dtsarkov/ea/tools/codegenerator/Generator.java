package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Repository;

import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateLexer;
import com.github.dtsarkov.ea.tools.codegenerator.parser.EACodeTemplateParser;

public class Generator {

	public static void main(String[] args) throws Exception {

		if (args.length == 0 ) return;
		
		String modelFile	 	= args[0];
		String elementName	 	= args[1];
		String templateFileName = args[2];
		
		File templateFile = new File(templateFileName);
		String templateName = templateFile.getName();
		String templateExt  = "";
		
		int idx = templateName.lastIndexOf(".");
		
		if ( idx != -1 ) {
			templateExt = templateName.substring(idx+1);
			templateName = templateName.substring(0, idx);
		}
		System.out.printf("File:\n\tName = [%s]\n\tPath=[%s]\n---\nIndex=[%d]\nName=%s\nExt=%s\n"
				,templateFile.getName()
				,templateFile.getParent()
				,idx
				,templateName
				,templateExt
				//,templateFile.getPath()
		);
		Repository model = openModel(modelFile);

		TemplateProcessor.setTemplateFolder(templateFile.getParent());
		TemplateProcessor.setTemplateExtention(templateExt);
		TemplateProcessor.setEAModel(model);
		
		Collection elements = model.GetElementsByQuery("Element Name", elementName);
		if ( elements.GetCount() == 0 ) {
			System.out.printf("Could not find any elments with name \"%f\"\n", elementName);
			model.CloseFile();
			return;
		}
		TemplateProcessor tp = new TemplateProcessor(templateName);
		
		Element element = (Element)elements.GetAt((short)0);

		tp.setElement(element);
		System.out.println("===============================================");
		System.out.println("First Execution");
		System.out.println("===============================================");
		tp.execute();

//		System.out.println("===============================================");
//		System.out.println("Second Execution");
//		System.out.println("===============================================");
//		tp.execute();

/*		
		InputStream 			is 		= new FileInputStream(templateFileName);
		
		ANTLRInputStream  		input 	= new ANTLRInputStream(is);
		EACodeTemplateLexer 	lexer 	= new EACodeTemplateLexer(input);
		CommonTokenStream 		tokens	= new CommonTokenStream(lexer);
		EACodeTemplateParser 	parser 	= new EACodeTemplateParser(tokens);

		TemplateProcessor			listener = new TemplateProcessor(parser,element);
		
		parser.addParseListener(listener);
		ParseTree				tree	= parser.file();	
*/		
		
		//TODO: Figure out how to close EA application
		System.out.printf("Closing model file \"%s\"...", modelFile);
		element = null;
		
		model.CloseFile();
		model = null;
		System.out.println("done.");
		
		
		Runtime.getRuntime().gc();
	}
	
	private static Repository openModel( String fileName ) {
		System.out.printf("Opening model file \"%s\"...", fileName);
		Repository r = new Repository();
		if ( r.OpenFile(fileName) ) {
			System.out.println("done.");
		} else {
			System.out.println(" failed!");
		}

		return r;
	}
	

}
