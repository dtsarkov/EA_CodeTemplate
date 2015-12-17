package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Repository;

public class Generator {


	public static void main(String[] args) throws Exception {
		System.out.println("EACodeGenerator <EA Model File> <EA Element> <Template File> <Output File>");
		if (args.length < 4 ) return;
		
		String modelFile	 	= args[0];
		String elementName	 	= args[1];
		String templateFileName = args[2];
		String outputFileName	= args[3];
		
		File templateFile = new File(templateFileName);
		String templateName = templateFile.getName();
		String templateExt  = "";
		
		int idx = templateName.lastIndexOf(".");
		
		if ( idx != -1 ) {
			templateExt = templateName.substring(idx+1);
			templateName = templateName.substring(0, idx);
		}

        Repository  model       = null;
        Element     element     = null;
        if ( !modelFile.equalsIgnoreCase("not-required") ) {
            model = openModel(modelFile);

            Collection elements = model.GetElementsByQuery("Element Name", elementName);
            if ( elements.GetCount() == 0 ) {
                System.err.printf("Could not find any elments with name \"%s\"\n", elementName);
                model.CloseFile();
                return;
            }
            element = (Element)elements.GetAt((short)0);
        }
        
		FileWriter fw = null; 
		try { 
			fw = new FileWriter(outputFileName);
		} catch ( IOException e ) {
			System.err.printf("Could not create output file \"%s\"\n", outputFileName);
			if ( model != null ) model.CloseFile();
            return;
		}
		
		TemplateProcessor.setTemplateFolder(templateFile.getParent());
		TemplateProcessor.setTemplateExtention(templateExt);
		TemplateProcessor.setEAModel(model);
		if (args.length == 5 && args[4].equalsIgnoreCase("debug")) {
			TemplateProcessor.setDebug(true);
		}

		TemplateProcessor.message("Processing Elements...");
		TemplateProcessor.message("%-30s|%-20s|%-30s","Name","Type","Template");
		TemplateProcessor.message("%-30s %-20s %-30s"
				,"------------------------------"
				,"--------------------"
				,"------------------------------"
		);
		TemplateProcessor tp = new TemplateProcessor(templateName);
		tp.setOutput(fw);
		tp.setElement(element);
		tp.execute();

		fw.flush();
		fw.close();

        if ( model != null )  {
            element = null;
            System.out.printf("Closing model file \"%s\"...", modelFile);
            model.CloseFile();
            model = null;
            System.out.println("done.");
            
            Runtime.getRuntime().runFinalization();
            System.gc();
        }
		
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
