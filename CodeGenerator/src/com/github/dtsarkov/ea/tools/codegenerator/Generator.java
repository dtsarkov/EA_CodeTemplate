package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.Repository;

public class Generator {

	
	public static void main(String[] args) throws Exception {
		System.out.println("EACodeGenerator v 0.39");

		CommandLine cmd = parseCommandLine(args);
		if ( cmd == null ) return;

		String modelFile	 	= cmd.getOptionValue("m");
		ArrayList<ProcessingEntry> batch = new ArrayList<ProcessingEntry>();

		String templateFolder 	= ".";
		String templateExt  	= "";
		boolean verbose			= cmd.hasOption('v');
		ProcessingEntry pe		= null;
		
		if ( cmd.hasOption("batch") ) {
			templateFolder = cmd.getOptionValue('f');
			templateExt	   = cmd.getOptionValue('x');
			loadProcessingEntries(batch, cmd.getOptionValue('b'));
		} else {
			pe = new ProcessingEntry(
					 cmd.getOptionValue('e')
					,cmd.getOptionValue('t')
					,cmd.getOptionValue('o')
			);
			batch.add(pe);
			
			File templateFile = new File(pe.template);
			templateFolder = templateFile.getParent();
			if ( templateFolder == null )
				templateFolder = ".";

			pe.template = templateFile.getName();
			int idx = pe.template.lastIndexOf(".");
			
			if ( idx != -1 ) {
				templateExt = pe.template.substring(idx+1);
				pe.template = pe.template.substring(0, idx);
			}
		}
		
		String queryName = "Element Name";
		if ( cmd.hasOption('q') ) {
			queryName = cmd.getOptionValue('q');
		}
		
		if ( verbose ) {
			System.out.printf("Model: %s\nTemplate Folder: %s\nTemplate Extentions: %s\nQuery Name: %s\n"
					,modelFile
					,templateFolder
					,templateExt
					,queryName
			);
		}
		
        Repository  		model   = null;
        Element     		element = null;
		FileWriter 			fw 		= null;
		TemplateProcessor 	tp		= null;
        
        if ( !modelFile.equalsIgnoreCase("not-required") ) {
            model = openModel(modelFile);
            if ( TemplateProcessor.getErrorCounter() > 0 )
            	return;
        }

		if (cmd.hasOption("variable") ) {
			if ( verbose ) 
				System.out.println("Global Variables:");
			Properties vars = cmd.getOptionProperties("variable");
			for( Object key : vars.keySet() ) {
				if ( verbose ) {
					System.out.printf("\t%-30s\t= %s\n", key, vars.get(key));
				}
				TemplateProcessor.addVariable("$$"+key.toString(), vars.get(key).toString());
			}
		}
		
		TemplateProcessor.setDebug(cmd.hasOption('d'));
		TemplateProcessor.setVerbose(verbose);

		TemplateProcessor.setTemplateExtention(templateExt);
		TemplateProcessor.setEAModel(model);
		for ( int i=0; i < batch.size(); i++ ) {
			pe = batch.get(i);
			
			TemplateProcessor.message(
					"Generate output file [%s] using template [%s] and element [%s] as an entry point..."
					,pe.outputFile ,pe.template ,pe.element
			);

			if ( model != null ) {
	            @SuppressWarnings("rawtypes")
				Collection elements = model.GetElementsByQuery(queryName, pe.element);
	            if ( elements.GetCount() == 0 ) {
	            	TemplateProcessor.error("Could not find any elments using query \"%s\" and search term \"%s\"", queryName, pe.element);
	                break;
	            }
	            element = (Element)elements.GetAt((short)0);
			}

    		try { 
    			fw = new FileWriter(pe.outputFile);
    			TemplateProcessor.setOutputFolder(pe.outputFile.getAbsoluteFile().getParent());
    		} catch ( IOException e ) {
    			TemplateProcessor.error("Could not create output file \"%s\"", pe.outputFile);
                break;
    		}
    		//System.out.printf("Output folder: [%s]\n",TemplateProcessor.getOutputFolder());
    		if ( verbose ) {		
	    		TemplateProcessor.message("Processing Elements...");
	    		TemplateProcessor.message("%-30s|%-20s|%-30s","Name","Type","Template");
	    		TemplateProcessor.message("%-30s %-20s %-30s"
	    				,"------------------------------"
	    				,"--------------------"
	    				,"------------------------------"
	    		);
    		}
    		tp = new TemplateProcessor(pe.template);
    		
    		tp.setTemplateFolder(templateFolder);
    		tp.setOutput(fw);
    		tp.setElement(element);
    		tp.execute();

    		fw.flush();
    		fw.close();
    		
    		if ( verbose ) {
    			TemplateProcessor.message("-------------------------------------------------------------------------------");
    		}
    		    		
		}
		TemplateProcessor.message("Warnings\t: %d",TemplateProcessor.getWarningCounter());
		TemplateProcessor.message("Errors  \t: %d",TemplateProcessor.getErrorCounter());

        if ( model != null )  {
            element = null;
            System.out.printf("Closing model file \"%s\"...", modelFile);
            model.CloseFile();
            model.Exit();
            model = null;
            System.out.println("done.");
            
            Runtime.getRuntime().runFinalization();
            System.gc();
        }

	}

	private static CommandLine parseCommandLine(String[] args) {
		Options options = new Options();
		options.addOption(Option
				.builder("m").longOpt("model")
				.desc("Enterprise Architect EAP file")
				.required(true)
				.hasArg(true).argName("file name")
				.build()
		);
		
		options.addOption(Option
				.builder("b").longOpt("batch")
				.desc("executes the CodeGenerator in the batch mode.\n"
					 +"Batch file is a CSV file containing <element name>,<template name>, <output file name>.")
				.hasArg(true).argName("file name")
				.build()
		);
		
		options.addOption(Option
				.builder("f").longOpt("folder")
				.desc("search template(s) in the given folder."
					 //+"\nDefault: folder of the template file specified in option -t"
				 )
				.hasArg(true).argName("folder")
				.build()
		);
		
		options.addOption(Option
				.builder("x").longOpt("extension")
				.desc("template file extension"
					 //+"\nDefault: extension of the template file specified in option -t"
				 )
				.hasArg(true).argName("extention")
				.build()
		);
		
		options.addOption(Option
				.builder("t").longOpt("template")
				.desc("use specified template file")
				.hasArg(true).argName("file name")
				.build()
		);
		
		options.addOption(Option
				.builder("e").longOpt("element")
				.desc("use given element name as an entry point")
				.hasArg(true).argName("element name")
				.build()
		);
		
		options.addOption(Option
				.builder("o").longOpt("output")
				.desc("use specified file to save generated code")
				.required(false)
				.hasArg(true).argName("file name")
				.build()
		);
		
		options.addOption(Option
				.builder("q").longOpt("query")
				.desc("select element using specified query name.\n Default: Simple")
				.required(false)
				.hasArg(true).argName("query name")
				.build()
		);
		
		
		options.addOption(Option.builder("D")
				.longOpt("variable")
				.desc("assign <value> to a global variable <name> ")
				.hasArgs().valueSeparator('=').argName("name=value")
				.build()
		);
		
		options.addOption("d", "debug", false, "print debug information");
		options.addOption("v", "verbose", false, "set verbose mode");
		options.addOption("h", "help", false, "print this message");


		CommandLineParser cmdParser = new DefaultParser();
		CommandLine 	  cmd		= null;
		String 			  message	= "";
		try {
			cmd = cmdParser.parse(options, args);
		} catch(ParseException e) {
			message = e.getMessage();
		}
	
		boolean needHelp = false;
		if (cmd == null ||cmd.hasOption("help")) {
			needHelp = true;
		} else if ((cmd.hasOption('b') && !(cmd.hasOption('f') && cmd.hasOption('x')))) {
			needHelp = true;
			message = "Batch mode: incomplete parameters!\n";
		} else if ((!cmd.hasOption('b') && !(cmd.hasOption('e') && cmd.hasOption('t') && cmd.hasOption('o')))) {
			needHelp = true;
			message = "Single execution: incomplete parameters!\n";
		}
		
		if ( needHelp ) {
			HelpFormatter formater = new HelpFormatter();
			System.out.println(message);
			formater.setWidth(120);
			formater.printHelp(
				 "EACodeGenerator -m <file name> -e <element name> -t <file name> -o <file name> [OPTIONS]\n"
				+"              or\n"
				+"       EACodeGenerator -m <file name> -b <file name> -f <folder> -x <extention> [OPTIONS]\n"
				, options
				, false
			);
			cmd = null;
		}
		return cmd;
	}
	
	private static boolean loadProcessingEntries(ArrayList<ProcessingEntry> batch, String fileName) {
		boolean				loaded = false;
		try {
			FileReader 			fr = new FileReader(fileName);
			LineNumberReader 	lr = new LineNumberReader(fr);
			
			int			lineNumber 	= 0;
			String[] 	values		= null;

			String 		line 		= lr.readLine();
			while ( line != null ) {
				lineNumber++;
				values = line.split(",");
				if ( values.length != 3 ) {
					System.out.printf("%s line: %d - wrong number of parameters\n",
							fileName, lineNumber
					);
					break;
				}
				batch.add(new ProcessingEntry(values[0], values[1], values[2]));
				line 		= lr.readLine();
			}
			loaded = ( line == null );
			lr.close();
		} catch (FileNotFoundException e) {
			System.out.printf("File %s not found\n", fileName);
		} catch (IOException e) {
			System.out.printf("Cannot read file %s \n", fileName);
		}
		return loaded;
	}
	
	private static Repository openModel( String fileName ) {
		File file = new File(fileName);
		if ( !file.exists() ) {
			TemplateProcessor.error("Could not find model file \"%s\"",fileName);
			return null;
		}
		try {
			fileName = file.getCanonicalPath();
		} catch (IOException e) {
			TemplateProcessor.error("Incorret path\"%s\"",fileName);
			return null;
		}
		TemplateProcessor.message("Opening model file \"%s\"...", fileName);
		Repository r = new Repository();
		if ( !r.OpenFile(fileName) ) {
			r.Exit();
			TemplateProcessor.error("Could not open model file \"%s\"!\n", fileName);
		}

		return r;
	}

	private static class ProcessingEntry {
		public String template;
		public String element;
		public File outputFile;
		
		ProcessingEntry(String element, String template, String outputFile) {
			this.element 	= element.trim();
			this.template 	= template.trim();
			this.outputFile	= new File(outputFile.trim());
		}
	}

}
