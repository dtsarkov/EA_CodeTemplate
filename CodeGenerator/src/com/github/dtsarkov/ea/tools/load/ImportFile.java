package com.github.dtsarkov.ea.tools.load;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.sparx.Element;

import com.github.dtsarkov.ea.tools.EA;
import com.github.dtsarkov.ea.tools.IComparator;
import com.github.dtsarkov.ea.tools.Logger;
import com.github.dtsarkov.ea.tools.load.json.JSONProcessor;

public final class ImportFile {

	public static void main(String[] args) {
		System.out.println("EAImport v 0.01");
		
		CommandLine cmd = parseCommandLine(args);
		if ( cmd == null ) return;

		String modelFile	 	= cmd.getOptionValue("m");
		String rootElementName	= cmd.getOptionValue("e");
	
		Logger.setDebug(cmd.hasOption("d"));
		Logger.setVerbose(cmd.hasOption("v"));
		
		IImportFileParser parser = new JSONProcessor();

		String[] files = cmd.getArgs();
		if ( files.length == 0 ) {
			Logger.message("There is no files to import!");
			return;
		}
		
		ArrayList<ImportElement> elements = new ArrayList<ImportElement>();
		ArrayList<ImportElement> tempList;
		for( String importFile : files  ) {
			if ( importFile.length() == 0 )
				continue;
			Logger.message("Parsing import file \"%s\"...",importFile);
			tempList = parser.execute(importFile);
			if ( Logger.getErrorCounter() > 0 ) {
				Logger.Summary();
				return;
			}
			elements.addAll(tempList);
		}
		
		
		if (elements.size() == 0 ) {
			Logger.warning("There is nothing to import from specified file(s)!");
		} else {
			if ( EA.openModel(modelFile) ) {
		        org.sparx.Element 	eaElement = EA.searchElementByName(rootElementName);
		        if ( eaElement != null ) {
		    		Logger.message("Loading data into element [%s]...",rootElementName);
		    		
			        //Element can be a package/packaging component
			        org.sparx.Package	eaPackage = EA.model().GetPackageByGuid(eaElement.GetElementGUID());
 
			        for ( ImportElement impElement : elements ) {
			        	loadElement(impElement, (eaPackage == null) ? eaElement : eaPackage, null);
			        }
		        } else {
		        	Logger.error("Could not find element \"%s\"", rootElementName);
		        }
			}
		}

		EA.closeModel();
		Logger.Summary();
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void loadElement(ImportElement sourceElement, Object targetElement, Object parentElement) {

    	Class targetClass = targetElement.getClass();
    	Object attributeValue;
    	//Assign all attributes from the sourceElement to attributes in the targetElement 
    	for ( String attributeName : sourceElement.getAttributes() ) {
    		if ( EA.isAttribute(attributeName, targetClass) ) {
    			attributeValue = sourceElement.getAttributeValue(attributeName);
    			Logger.debug("Setting attribute \"%s\"", attributeName);
    			
    			if ( EA.isCollection(attributeName, targetClass) ) {
    				if ( attributeValue instanceof ArrayList<?> ) {
    		        	//Save item in the model before assigning collection attribute 
    		        	EA.executeMethod(targetElement,"Update");
    					loadCollection(
    						 targetElement
    						,(org.sparx.Collection)EA.getAttributeValue(targetElement, attributeName)
    						,(ArrayList<ImportElement>)attributeValue
    					);
    				} else {
    					//TODO: Raise an exception
    				}
    			} else {
    				if ( attributeValue instanceof String && ((String)attributeValue).startsWith("@") ) {
    					//Value is a reference to another element and needs to be replaced by element ID
    					String refName = ((String)attributeValue).substring(1);
    					
    					Logger.debug("Resolving an element reference \"%s\"", refName);
    					org.sparx.Element e = EA.searchElementByName(refName);
    					if ( e == null ) {
    						Logger.error("Could not find element referenced by \"%s\"", refName);
    						attributeValue = null;
    					} else {
    						org.sparx.Package p = EA.model().GetPackageByGuid(e.GetElementGUID());
    						//Element is Package return PackageID as reference 
    						if ( p != null ) {
    							attributeValue = EA.getAttributeValue(p, "PackageID");
    						} else {
    							attributeValue = EA.getAttributeValue(e, "ElementID");
    						}
    						Logger.debug("\t\tElementID=%s", attributeValue);
    					}
    				}
    				
    				if ( attributeValue != null ) {
    					if ( targetElement instanceof org.sparx.Connector && attributeName.equalsIgnoreCase("ClientID")) {
    						int id = 0;
    						if (parentElement instanceof org.sparx.Package ) {
    							id = ((org.sparx.Package)parentElement).GetPackageID();
    						} else {
    							id = ((org.sparx.Element)parentElement).GetElementID();
    						}
    						Logger.debug("\t\tre-pointing Supplier end to parent element ID=%d", id);
    						((org.sparx.Connector)targetElement).SetSupplierID(id);
    					}
    					
    					EA.setAttribute(targetElement,attributeName,attributeValue);
    				}
    			}
    		} else {
    			Logger.error("Class \"%s\" does not have attribute \"%s\"!"
    					, targetElement.getClass().getSimpleName()
    					, attributeName
    			);
    		}
    	}
    	//Save item in the model
    	EA.executeMethod(targetElement,"Update");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private static void loadCollection(Object parentElement, org.sparx.Collection collection, ArrayList<ImportElement> elements) {

    	int		collectionCount = collection.GetCount();
    	boolean checkExisting 	= true; //collectionCount != 0;
    	boolean supportSearch 	= true;
    	
    	Logger.debug("collectionCount = %d, checkExistng = %s collection type = \"%s\""
    			, collectionCount
    			, checkExisting
    			, collection.GetObjectType().toString()
    	);
    	
    	Object targetElement;
    	String sourceElementName, sourceElementType;
    	IComparator cmp;
        for ( ImportElement sourceElement : elements ) {
        	sourceElementName = sourceElement.getName();
        	sourceElementType = sourceElement.getType();
        	
        	Logger.debug("Import element \"%s\" - \"%s\"", sourceElementName,sourceElementType);
        	
    		targetElement = null;
        	if ( checkExisting ) {
        		if (supportSearch) try {
            		// GetByName throws "Index out of bound" exception when element does not exist
            		targetElement = collection.GetByName(sourceElementName);
            	} catch ( Exception e ) {
            		if ( e.getMessage().equalsIgnoreCase("Action is not supported")) {
            			Logger.debug("Collection does not support GetByName method.");
            			supportSearch = false;
            		} else {
	            		Logger.debug("\t\tElement \"%s\" does not exist. Exception: \"%s\""
	            				, sourceElementName
	            				, e.getMessage()
	            		);
            		}
            		targetElement = null;
            	}
        		if ( !supportSearch ) {
        			for ( short i = 0; i < collectionCount; i++) {
        				targetElement = collection.GetAt(i);
        				cmp = EA.getComparator(targetElement);
        				if ( cmp.compare(targetElement, sourceElement)) {
        					break;
        				} else {
        					targetElement = null;
        				}
        			}
        		}
        	}
        	if ( targetElement == null ) {
        		//Element was not found
        		Logger.debug("\t\tCreating new element");
	        	targetElement = collection.AddNew(sourceElementName, sourceElementType );
	        	if ( !(targetElement instanceof org.sparx.Connector) ) {
		        	//Save item in the model
		        	EA.executeMethod(targetElement,"Update");
	        	}
	        	
	        	//TODO: Capture exception "Invalid Type"
	        	if ( targetElement == null ) {
	        		Logger.error("Could not create new element!");
	        		return;
	        	}
	        	collection.Refresh();
        	}
        	loadElement(sourceElement,targetElement,parentElement);
        	
        }
    	//Refresh collection to reflect changes
    	collection.Refresh();
	}
	
	/*
	 * 
	 ***************************************************************************/
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
				.builder("e").longOpt("element")
				.desc("package where elements will be imported")
				.required(true)
				.hasArg(true).argName("element name")
				.build()
		);
		
		options.addOption("d", "debug"	, false, "print debug information"	);
		options.addOption("v", "verbose", false, "set verbose mode"			);
		options.addOption("h", "help"	, false, "print this message"		);


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
		} 
		
		if ( needHelp ) {
			HelpFormatter formater = new HelpFormatter();
			System.out.println(message);
			formater.setWidth(120);
			formater.printHelp(
				 "ImportFile -m <file name> -e <element name> [OPTIONS] <file1> <fileN> \n"
				, options
				, false
			);
			cmd = null;
		}
		return cmd;
	}
}
