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
		CommandLine cmd = parseCommandLine(args);
		if ( cmd == null ) return;

		String modelFile	 	= cmd.getOptionValue("m");
		String packageName		= cmd.getOptionValue("e");
	
		Logger.setDebug(cmd.hasOption("d"));
		Logger.setVerbose(cmd.hasOption("v"));
		
		IImportFileParser parser = new JSONProcessor();

		String[] files = cmd.getArgs();
		if ( files.length == 0 ) {
			Logger.message("There is no files to import!");
			return;
		}
		
		Logger.message("Importing elements..."
				+"\n\tinto package [%s]"
				+"\n\tin model [%s]"
				,packageName, modelFile
				);
		
		ArrayList<ImportElement> elements = new ArrayList<ImportElement>();
		ArrayList<ImportElement> tempList;
		for( String importFile : files  ) {
			if ( importFile.length() == 0 )
				continue;
			Logger.message("Loading import file \"%s\"...",importFile);
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
	        ArrayList<ImportElement> packages = new ArrayList<ImportElement>();
	        for ( ImportElement element : elements ) {
	        	if (element.getType().equalsIgnoreCase("package")) {
	        		packages.add(element);
	        	}
	        }
	        for (ImportElement element : packages ) {
        		elements.remove(element);
	        }
			Logger.message("\tthere are %d element(s) and %d package(s) to import"
					,elements.size()
					,packages.size()
			);
			if ( EA.openModel(modelFile) ) {
				String queryName = "Element Name";
				
				org.sparx.Collection list = EA.model().GetElementsByQuery(queryName, packageName);
		        if ( list.GetCount() == 0 ) {
		        	Logger.error("Could not find any elments using query \"%s\" and search term \"%s\"", queryName, packageName);
		        	return;
		        }
		        org.sparx.Element 	packageElement 	= (Element)list.GetAt((short)0);
		        org.sparx.Package	rootPackage		= EA.model().GetPackageByGuid(packageElement.GetElementGUID());

		        if ( packages.size() > 0 ) {
			        Logger.debug("Importing packages into package \"%s\"...", rootPackage.GetName());
			        importElements(rootPackage, rootPackage.GetPackages(), packages);
		        }
		        Logger.debug("Importing elements into package \"%s\"...", rootPackage.GetName());
		        importElements(rootPackage, rootPackage.GetElements(), elements);
			}
		}

		EA.closeModel();
		Logger.Summary();
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private static void importElements(Object parentElement, org.sparx.Collection collection, ArrayList<ImportElement> elements) {

    	Object 	element;
    	Object	value;
    	String 	name;
    	int		collectionCount = collection.GetCount();
    	boolean checkExisting 	= true; //collectionCount != 0;
    	boolean supportSearch 	= true;
    	IComparator cmp;
    	
    	Logger.debug("collectionCount = %d, checkExistng = %s collection type = \"%s\""
    			, collectionCount
    			, checkExisting
    			, collection.GetObjectType().toString()
    	);
        for ( ImportElement importElement : elements ) {
        	Logger.debug("Import element \"%s\" - \"%s\"", importElement.getName(),importElement.getType());
        	
    		element = null;
        	if ( checkExisting ) {
        		if (supportSearch) try {
            		// GetByName throws "Index out of bound" exception when element does not exist
            		element = collection.GetByName(importElement.getName());
            	} catch ( Exception e ) {
            		if ( e.getMessage().equalsIgnoreCase("Action is not supported")) {
            			Logger.debug("Collection does not support GetByName method.");
            			supportSearch = false;
            		} else {
	            		Logger.debug("\t\tElement \"%s\" does not exist. Exception: \"%s\""
	            				, importElement.getName()
	            				, e.getMessage()
	            		);
            		}
            		element = null;
            	}
        		if ( !supportSearch ) {
        			for ( short i = 0; i < collectionCount; i++) {
        				element = collection.GetAt(i);
        				cmp = EA.getComparator(element);
        				if ( cmp.compare(element, importElement)) {
        					break;
        				} else {
        					element = null;
        				}
        			}
        		}
        	}
        	if ( element == null ) {
        		Logger.debug("\t\tCreating new element");
	        	element = collection.AddNew(
	        			importElement.getName(), importElement.getType()
	        	);
	        	if ( !(element instanceof org.sparx.Connector) ) {
		        	//Save item in the model
		        	EA.executeMethod(element,"Update");
	        	}
	        	
	        	//TODO: Capture exception "Invalid Type"
	        	if ( element == null ) {
	        		Logger.error("Could not create new element!");
	        		return;
	        	}
	        	collection.Refresh();
        	}
        	
        	for ( String attributeName : importElement.getAttributes() ) {
        		if ( EA.isAttribute(attributeName, element.getClass()) ) {
        			value = importElement.getAttributeValue(attributeName);
        			Logger.debug("Setting attribute \"%s\"", attributeName);
        			if ( EA.isCollection(attributeName, element.getClass()) ) {
        				if ( value instanceof ArrayList<?> ) {
        		        	//Save item in the model
        		        	EA.executeMethod(element,"Update");
        					importElements(
        						 element
        						,(org.sparx.Collection)EA.getAttributeValue(element, attributeName)
        						,(ArrayList<ImportElement>)value
        					);
        				} else {
        					//TODO: Raise an exception
        				}
        			} else {
        				if ( value instanceof String && ((String)value).startsWith("@") ) {
        					//Value is a name of another element and needs to be replaced by element ID
        					String refName 		= ((String)value).substring(1);
        					
        					Logger.debug("Trying to find element referenced by \"%s\"", refName);
        					org.sparx.Element e = EA.searchElementByName(refName);
        					if ( e == null ) {
        						Logger.error("Could not find element \"%s\"", refName);
        						value = null;
        					} else {
        						org.sparx.Package p = EA.model().GetPackageByGuid(e.GetElementGUID());
        						//Element is Package return PackageID as reference 
        						if ( p != null ) {
        							value = EA.getAttributeValue(p, "PackageID");
        						} else {
        							value = EA.getAttributeValue(e, "ElementID");
        						}
        						Logger.debug("\t\tElementID=%s", value);
        					}
        				}
        				if ( value != null ) {
        					if ( element instanceof org.sparx.Connector && attributeName.equalsIgnoreCase("ClientID")) {
        						int id = 0;
        						if (parentElement instanceof org.sparx.Package ) {
        							id = ((org.sparx.Package)parentElement).GetPackageID();
        						} else {
        							id = ((org.sparx.Element)parentElement).GetElementID();
        						}
        						Logger.debug("\t\tre-pointing Supplier end to parrent element ID=%d", id);
        						((org.sparx.Connector)element).SetSupplierID(id);
        					}
        					EA.setAttribute(element,attributeName,value);
        				}
        			}
        		} else {
        			Logger.error("Class \"%s\" does not have attribute \"%s\"!"
        					, element.getClass().getSimpleName()
        					, attributeName
        			);
        		}
        	}
        	//Save item in the model
        	EA.executeMethod(element,"Update");
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
		
//		options.addOption(Option
//				.builder("i").longOpt("input")
//				.desc("import elements from the specified file")
//				.required(true)
//				.hasArg(true).argName("file name")
//				.build()
//		);
		
	
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
