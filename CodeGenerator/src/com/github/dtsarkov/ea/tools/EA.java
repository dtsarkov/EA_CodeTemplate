package com.github.dtsarkov.ea.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.sparx.Element;
import org.sparx.Repository;

import com.github.dtsarkov.ea.tools.load.ImportElement;

public final class EA {
	private static Repository model = null;
	
	public static boolean openModel( String fileName ) {
		closeModel();
		
		File file = new File(fileName);
		if ( !file.exists() ) {
			Logger.error("Could not find model file \"%s\"",fileName);
		} else try {
			fileName = file.getCanonicalPath();
			Logger.message("Opening model file \"%s\"...", fileName);
			model = new Repository();
			if ( !model.OpenFile(fileName) ) {
				model.Exit();
				Logger.error("Could not open model file \"%s\"!\n", fileName);
				model = null;
			} else {
				Logger.message("done.");
			}
		} catch (IOException e) {
			Logger.error("Incorret path\"%s\"",fileName);
		}
		return model != null;
	}

	public static void closeModel() {
        if ( model != null )  {
            Logger.message("Closing model file...");
            model.CloseFile();
            model.Exit();
            model = null;
            Logger.message("done.");
            
            Runtime.getRuntime().runFinalization();
            System.gc();
        }

	}
	
	public static Repository model() {
		return model;
	}
	
	public static Object executeGetMethod(Object obj, String methodName) {
		Object result = null;
		try {
			Method method = obj.getClass().getMethod("Get"+methodName);
			result = method.invoke(obj);
		} catch (NoSuchMethodException e) {
			Logger.debug("\t\tNo Such Method %s Exception!", methodName);
		} catch (Exception e) {
			Logger.debug("\t\t !!!Exception!!!");
		}
		return result;
	}

	private static HashMap<String, HashMap<String,ImportAttribute>> attributeMap =
			new HashMap<String, HashMap<String,ImportAttribute>>(); 

	public static boolean isAttribute(String attributeName, Class<? extends Object> elementClass) {
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		return attributeList.containsKey(attributeName.toUpperCase());
	}

	public static boolean isCollection(String attributeName, Class<? extends Object> elementClass) {
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		attributeName = attributeName.toUpperCase();
		if ( attributeList.containsKey(attributeName) ) {
			ImportAttribute a = attributeList.get(attributeName);
			return (a.parameterType.getName().equals("org.sparx.Collection"));
		}
		return false;
	}

	/*
	public static String getAttributeType(String attributeName, Class<? extends Object> elementClass) {
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		String type = "";
		attributeName = attributeName.toUpperCase();
		if ( attributeList.containsKey(attributeName) ) {
			type = attributeList.get(attributeName).typeName;
		}
		return type;
	}
	*/
	/*
	public static String getAttributeMethod(String attributeName, Class<? extends Object> elementClass) {
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		String method = "";
		attributeName = attributeName.toUpperCase();
		if ( attributeList.containsKey(attributeName) ) {
			method = attributeList.get(attributeName).methodName;
		}
		return method;
	}
	*/
	public static void executeMethod(Object element, String methodName) {
		String className = element.getClass().getName();
		try {
			Method method = element.getClass().getMethod(methodName);
			method.invoke(element);
			Logger.debug("Executed method %s.\"%s\""
					, className
					, methodName
			);
		} catch (NoSuchMethodException e) {
			Logger.error("Method \"%s.%s\" - No Such method exception", className,methodName);
		} catch (Exception e) {
			Logger.error("Method \"%s.%s\" - Exception:\n\t\t%s"
					, className, methodName, e.getMessage()
			);
		}
	}
	
	public static Object getAttributeValue(Object element, String attributeName) {
		Object value = null;

		if ( element instanceof org.sparx.Element ) {
			Object packageElement = EA.model().GetPackageByGuid(
						((org.sparx.Element)element).GetElementGUID()
					);
			if ( packageElement != null ) {
				value = getAttributeValue(packageElement, attributeName);
				if (value != null ) return value;
			}
		}
		
		Class<? extends Object> elementClass = element.getClass();
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		attributeName = attributeName.toUpperCase();
		if ( !attributeList.containsKey(attributeName) ) {
			Logger.debug("Attribute \"%s\" does not exist in class [%s]!"
					, attributeName
					, elementClass.getName()
			);
			//TODO: Raise an exception
			return value;
		}
		ImportAttribute a = attributeList.get(attributeName);
		try {
			Logger.debug("Getting attribute \"%s\" value from %s", attributeName, elementClass.getName());
			Method method = elementClass.getMethod(a.getMethod);
			value = method.invoke(element);
			Logger.debug("Method %s.\"%s\" returned %s"
					, elementClass.getName()
					, a.getMethod
					, value
			);
		} catch (NoSuchMethodException e) {
			Logger.error("Attribute \"%s\" - No Such method \"%s\"exception", attributeName, a.getMethod);
		} catch (Exception e) {
			Logger.error("Attribute \"%s\" - Exception", attributeName);
		}
		return value;
	}
	
	public static void setAttribute(Object element, String attributeName, Object value) {
		Class<? extends Object> elementClass = element.getClass();
		HashMap<String,ImportAttribute> attributeList = getAttributeList(elementClass);
		attributeName = attributeName.toUpperCase();
		if ( !attributeList.containsKey(attributeName) ) {
			//TODO: Raise an exception
			return;
		}
		ImportAttribute a = attributeList.get(attributeName);
		try {
			Logger.debug("Setting attribute \"%s\" = [%s] using method %s(%s)"
					, attributeName
					, value.toString()
					, a.setMethod
					, a.parameterType.getName()
			);
			Method method = elementClass.getMethod(a.setMethod,a.parameterType);
			if ( a.parameterType.getName().equalsIgnoreCase("int") ) {
				method.invoke(element,Integer.valueOf(value.toString()));
			} else {
				method.invoke(element,value);
			}
			Logger.debug("\t\tall done");
		} catch (NoSuchMethodException e) {
			if ( element instanceof org.sparx.Package ) {
				setAttribute(((org.sparx.Package)element).GetElement(), attributeName, value);
			} else {
				Logger.error("Attribute \"%s\" - No Such method exception", attributeName);
			}
		} catch (Exception e) {
			Logger.error("Attribute \"%s\" - Exception [%s]",attributeName, e.getMessage());
		}
		
	}
	
	private static HashMap<String,ImportAttribute> getAttributeList(Class<? extends Object> elementClass) {
		String className = elementClass.getName();
		HashMap<String,ImportAttribute> attributeList;
		if ( !attributeMap.containsKey(className) ) {
			if ( className.equals("org.sparx.Package") ) {
				attributeList = getMethods(org.sparx.Element.class);
				attributeList.putAll(getMethods(elementClass));
			} else {
				attributeList = getMethods(elementClass);
			}
			
			attributeMap.put(className, attributeList);
		} else {
			attributeList = attributeMap.get(className);
		}
		return attributeList;
	}
	
	private static HashMap<String,ImportAttribute> getMethods(Class<? extends Object> elementClass) {
		HashMap<String,ImportAttribute> list = new HashMap<String,ImportAttribute>();
		
		String methodName;
		String key;
		ImportAttribute a = new ImportAttribute();
		for ( Method method : elementClass.getDeclaredMethods() ) {
			if ( (method.getModifiers() & Modifier.PUBLIC) == 1) {
				methodName = method.getName();
				key = methodName.substring(3).toUpperCase();
				if ( list.containsKey(key) ) {
					a = list.get(key);
				} else {
					a = new ImportAttribute();
				}
				if ( methodName.startsWith("Set") ) {
					a.setMethod 	= methodName;
					a.parameterType = method.getParameterTypes()[0];
				} else if ( methodName.startsWith("Get") ) {
					a.getMethod 	= methodName;
					a.parameterType = method.getReturnType();
					a.isCollection  = a.parameterType.getName().startsWith("org");
				}
				if ( a.parameterType != null ) {
					Logger.debug("%-25s|%-25s|%25s|%7s|%s"
							,a.getMethod, a.setMethod
							,a.parameterType.getName()
							,a.isCollection
							,key
							);
					list.put(key, a);
				}
			}
			
		}
		
		return list;
	}
	
	public static IComparator getComparator(Object element) {
		if ( element instanceof org.sparx.Connector )
			return connectorComparator;
		
		return defaultComparator;
	}
	private static final IComparator defaultComparator = new IComparator() {
		//By default elements are compared by name 
		public boolean compare(Object eaElement, ImportElement importElement) {
			String name = (String)EA.getAttributeValue(eaElement, "Name");
			return name.equalsIgnoreCase(importElement.getName());
		}
	};
	private static final IComparator connectorComparator = new IComparator() {
		//Connectors are compared by target name 
		public boolean compare(Object eaElement, ImportElement importElement) {
			boolean direct = true;
			Object value = importElement.getAttributeValue("SupplierID");
			if ( value == null ) {
				direct = false;
				value  = importElement.getAttributeValue("ClientID");
			}
			if ( value != null ) {
				String 	id1 = value.toString();
				int 	id2 = 0;
				if ( direct ) {
					id2 = ((org.sparx.Connector)eaElement).GetSupplierID();
				} else { 
					id2 = ((org.sparx.Connector)eaElement).GetClientID();
				}
				if ( id1.startsWith("@") ) { //It's a reference not an ID
					String name1 = id1.substring(1);
					org.sparx.Element target = EA.model().GetElementByID(id2);
					String name2 = target.GetName();
					if ( name1.indexOf('.') != -1 ) { //Reference contains package name
						org.sparx.Package p = EA.model().GetPackageByID(target.GetPackageID());
						if ( p != null ) {
							name2 = p.GetName()+"."+name2;
						}
					}
					if ( name1.indexOf("::") != -1 ) { //Reference contains element stereotype
						name2 = "\""+target.GetStereotype()+"\"::"+name2;
					}
					
					return name2.equalsIgnoreCase(name1);
				} else {
					return id1.compareTo(String.valueOf(id2)) == 0;
				}
			} else { 
				return false;
			}
		}
	};
	public static org.sparx.Element searchElement(String searchString, String query) {
		org.sparx.Element element 	= null;
		if ( query == null ) {
			element = searchElementByName(searchString);
		} else if (query.equalsIgnoreCase("GUID") ) {
			element = model().GetElementByGuid(searchString);
		} else {
			@SuppressWarnings("rawtypes")
			org.sparx.Collection elements = model().GetElementsByQuery(query, searchString);
            if ( elements.GetCount() != 0 ) {
                element = (Element)elements.GetAt((short)0);
            }
		}
		return element;
	}
	
	static private final String queryByName = 
			 "select  o.Object_ID from t_object o "
			+"where %s o.Name = '%s' order by o.Object_ID"
			;
	static private final String queryParentChild = 
			 "select  o.Object_ID from t_package p, t_object o "
			+"where o.Package_ID = p.Package_ID "
			+"and p.Name+'.'+%s o.Name = '%s' order by o.Object_ID"
			;
	public static org.sparx.Element searchElementByName(String name) {
		Logger.debug("Searching for an element \"%s\"...", name);
		
		org.sparx.Element element 	= null;
		String query 	= "";
		if ( name.indexOf("::") != -1 ) { //References uses element Stereotype e.g. "Stereotype"::<Element Name>
			query = "'\"'+o.Stereotype+'\"::'+";
		} else if ( name.indexOf("~") != -1 ) { //References uses element's Type e.g. Type~<Element Name>
			query = "o.Object_Type+'~'+";
		}
		String n = name.replaceAll("\\\\.", ""); //Remove dots '\.'
		name = name.replaceAll("\\\\.", ".");
		System.out.println(n + " = "+name);
		if ( n.indexOf('.') != -1 ) {
			query = String.format(queryParentChild, query, name);
		} else {
			query = String.format(queryByName, query, name);
		}
		Logger.debug("\t\tusing query [%s]",query);
		String xml =  model().SQLQuery(query);
		int idStart = xml.indexOf("<Object_ID>");
		if (idStart != -1) {
			int idEnd = xml.indexOf("</Object_ID>",idStart+1);
			if ( idEnd != -1) {
				String id = xml.substring(idStart+11, idEnd);
				element = model().GetElementByID(Integer.valueOf(id));
			}
		}
		return element;
	}
	/*
	 * ************************************************************************/
	private static class ImportAttribute {
		public String setMethod			= null;
		public String getMethod			= null;
		public Class<?>  parameterType	= null;
		public boolean 	isCollection 	= false;
	}
	
}
