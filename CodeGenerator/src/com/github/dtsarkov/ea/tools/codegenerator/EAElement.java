package com.github.dtsarkov.ea.tools.codegenerator;

import java.lang.reflect.Method;

import org.antlr.v4.runtime.ParserRuleContext;
import org.sparx.Collection;
import org.sparx.Element;
import org.sparx.TaggedValue;

public class EAElement {
	private Object 		element;
	private Object		package_element;

	private boolean 	elementHasParent 	= true;
	private EAElement 	elementParent		= null;
	private boolean 	elementHasPackage	= true;
	private EAElement	elementPackage		= null;
	private boolean 	elementHasSource	= true;
	private EAElement	elementSource		= null;
	private EAElement	sourceRole			= null;
	private boolean 	elementHasTarget	= true;
	private EAElement	elementTarget		= null;
	private EAElement	targetRole			= null;
	
	public EAElement(Object element) {
		this.element 		= element;

		if ( this.element != null ) {
	        TemplateProcessor.debug("Created new EA Element for: %s"
	        		,element.getClass().getName()
			);
			if ( element instanceof org.sparx.Element ) {
				if ( ((Element)element).GetType().equalsIgnoreCase("Package") ) {
					this.package_element = TemplateProcessor.getEAModel().GetPackageByGuid(
							((Element)element).GetElementGUID()
					);
				}
			} else if ( element instanceof org.sparx.Package ) {
				this.package_element = element;
				this.element = ((org.sparx.Package)element).GetElement();
			}
		} else {
	        TemplateProcessor.debug("Created new NULL EA Element");
	        this.elementHasParent 	= false;
	        this.elementHasPackage	= false;
	        this.elementHasSource	= false;
	        this.elementHasTarget	= false;
		}
	}

	public EAElement getParent() {
		if ( elementParent == null && elementHasParent ) {
			//try to get element's parent
			TemplateProcessor.debug("Getting parent for elelment %s",element.getClass());
			try {
				Method m = element.getClass().getMethod("GetParentID");
                int parentID = Integer.parseInt(m.invoke(element).toString());
                TemplateProcessor.debug("\t\tParentID = %d",parentID);
                if ( parentID != 0 ) {
                    Object p = TemplateProcessor.getEAModel().GetElementByID(parentID);
                    elementHasParent = ( p != null );
                    if ( elementHasParent )
                    	elementParent = new EAElement(p);
                }
			} catch (NoSuchMethodException e ) {
				TemplateProcessor.debug("\t\t No Such Method Exception");
				elementHasParent = false;
			} catch (Exception e ) {
				elementHasParent = false;
				e.printStackTrace(System.err);
			}
		}
		return elementParent;
	}
	
	public EAElement getPackage() {
		if ( elementPackage == null && elementHasPackage ) {
			//try to get element's package
			TemplateProcessor.debug("getPackage() for element hashCode = %d", element.hashCode());
			try {
				Method m = element.getClass().getMethod("GetPackageID");
				int packageID = Integer.parseInt(m.invoke(element).toString()); 
                TemplateProcessor.debug("\t\tPackageID = %d",packageID);
				Object p = TemplateProcessor.getEAModel().GetPackageByID(
						packageID
				);
				elementHasPackage = (p != null);
				if ( elementHasPackage )
					elementPackage = new EAElement(p);
				
			} catch (NoSuchMethodException e ) {
				TemplateProcessor.debug("\t\t getPackage(): No Such Method Exception");
				elementHasPackage = false;
			} catch (Exception e ) {
				elementHasPackage = false;
				e.printStackTrace(System.err);
			}
			
		}
		return elementPackage;
	}
	
	public EAElement getSource() {
		if ( elementSource == null && elementHasSource ) {
			//try to get element's source
			try {
				Method m = element.getClass().getMethod("GetClientID");
				Object s = TemplateProcessor.getEAModel().GetElementByID(
						Integer.parseInt(m.invoke(element).toString())
				);
				elementHasSource = ( s != null );
				if ( elementHasSource ) 
					elementSource = new EAElement(s);
			} catch (NoSuchMethodException e ) {
				TemplateProcessor.debug("\t\t No Such Method Exception");
				elementHasSource = false;
			} catch (Exception e ) {
				elementHasSource = false;
				e.printStackTrace(System.err);
			}
		}
		return elementSource;
	}

	public EAElement getSourceRole() {
		if ( sourceRole == null && elementHasSource ) {
			Object t = executeGetMethod(element, "ClientEnd");
			elementHasSource = (t != null);
			if ( elementHasSource )
				sourceRole = new EAElement(t);
		}
		return sourceRole;
	}
	
	public EAElement getTarget() {
		if ( elementTarget == null && elementHasTarget ) {
			//try to get element's target
			try {
				Method m = element.getClass().getMethod("GetSupplierID");
				Object t = TemplateProcessor.getEAModel().GetElementByID(
						Integer.parseInt(m.invoke(element).toString())
				);
				elementHasTarget = (t != null);
				if ( elementHasTarget )
					elementTarget = new EAElement(t);
				
			} catch (NoSuchMethodException e ) {
				TemplateProcessor.debug("\t\t No Such Method Exception");
				elementHasTarget = false;
			} catch (Exception e ) {
				elementHasTarget = false;
				e.printStackTrace(System.err);
			}
		}
		return elementTarget;
	}
	
	public EAElement getTargetRole() {
		if ( targetRole == null && elementHasTarget ) {
			Object t = executeGetMethod(element, "SupplierEnd");
			elementHasTarget = (t != null);
			if ( elementHasTarget )
				targetRole = new EAElement(t);
		}
		return targetRole;
	}

	private Object getAttribute(EAElement element, String attributeName) {
		Object attribute 		= null;
		if ( element != null ) {
			if ( element.package_element != null ) {
				//Try to get attribute from package element
				TemplateProcessor.debug("\tfrom package_element");
				attribute = executeGetMethod(element.package_element,attributeName);
			}
			if ( attribute == null ) {
				TemplateProcessor.debug("\tfrom element");
				attribute = executeGetMethod(element.element,attributeName);
			}
		}
		return attribute;
	}
	
	public String getAttributeValue(ParserRuleContext ctx) {
		Object attribute = getAttribute(ctx.getText(),ctx);
		
		if ( attribute == null )
			return null;
		else 
			return attribute.toString();
	}
	
	public Object getAttribute(String attributeFullName, ParserRuleContext ctx) {
		if ( this.element == null ) return null;

		EAElement elementInScope= null;
		Object attribute 		= null;
		String name[] 			= attributeFullName.split("\\.");
		
		TemplateProcessor.debug("getAttribute(%s) => [%s] [%s]"
				,attributeFullName, name[0],name[1]
		);

		if ( name[0].equalsIgnoreCase("$") || name[0].equalsIgnoreCase("$this") ) {
			elementInScope = this;
		} else if ( name[0].equalsIgnoreCase("$parent") ) {
			elementInScope = getParent();
		} else if ( name[0].equalsIgnoreCase("$package") ) {
			elementInScope = getPackage();
		} else if ( name[0].equalsIgnoreCase("$source") ) {
			elementInScope = getSource();
		} else if ( name[0].equalsIgnoreCase("$sourceRole") ) {
			elementInScope = getSourceRole();
		} else if ( name[0].equalsIgnoreCase("$target") ) {
			elementInScope = getTarget();
		} else if ( name[0].equalsIgnoreCase("$targetRole") ) {
			elementInScope = getTargetRole();
		} else {
			TemplateProcessor.error(ctx, "Invalid scope modifier \""+name[0]+"\"!");
		}

		if (elementInScope != null ) {
			attribute = getAttribute(elementInScope,name[1]);
			if ( attribute == null && ctx != null ) {
				TemplateProcessor.error(ctx, "Attribute \""+name[1]+"\" does not exist!");
			}
		}
		return attribute;
	}

	public String getTagValue( String tagName ) {
		if ( this.element == null ) return ("UNKNOWN");

		String value 	= null;

		String name[] = tagName.split("\\.");
		name[1] = name[1].substring(1, name[1].length()-1);

		TemplateProcessor.debug("getTagValue(%s) => [%s] [%s]"
				,tagName
				,name[0]
				,name[1]
				);

		TaggedValue tag = null; 
		if ( name[0].equalsIgnoreCase("$") || name[0].equalsIgnoreCase("$this") ) {
			tag = getTag(this,name[1]);
		} else if ( name[0].equalsIgnoreCase("$parent") ) {
			tag = getTag(getParent(),name[1]);
		} else if ( name[0].equalsIgnoreCase("$package") ) {
			tag = getTag(getPackage(),name[1]);
		} else if ( name[0].equalsIgnoreCase("$source") ) {
			tag = getTag(getSource(),name[1]);
		} else if ( name[0].equalsIgnoreCase("$sourceRole") ) {
			tag = getTag(getSourceRole(),name[1]);
		} else if ( name[0].equalsIgnoreCase("$target") ) {
			tag = getTag(getTarget(),name[1]);
		} else if ( name[0].equalsIgnoreCase("$targetRole") ) {
			tag = getTag(getTargetRole(),name[1]);
		} else {
			TemplateProcessor.error("Invalid scope modifier \"%s\"!", name[1]);
		}
		if ( tag != null ) {
			value = tag.GetValue();
		}
		return value;
	}

	@SuppressWarnings("rawtypes")
	private TaggedValue getTag(EAElement obj, String tagName) {
		TaggedValue tag = null;
		if ( obj != null ) {
			Object tags = null;
			if ( obj.package_element != null ) {
				TemplateProcessor.debug("\tgetting tagged values for package...");
				tags = executeGetMethod(obj.package_element, "TaggedValues");
			}
			if ( tags == null ) {
				TemplateProcessor.debug("\tgetting tagged values for element...");
				tags = executeGetMethod(obj.element, "TaggedValues");
			}
			if ( tags != null ) {
				//GetByName is case-sensitive. 
				//tag = (TaggedValue)(((Collection)tags).GetByName(tagName));
				//Replaced by non case-sensitive version.
				short count = ((Collection)tags).GetCount();
				for ( short i = 0; i < count; i++ ) {
					tag = (TaggedValue)((Collection)tags).GetAt(i);
					if ( tag.GetName().equalsIgnoreCase(tagName) ) 
						return tag;
				}
				tag = null;
			}
		}
		return tag;
	}
	
	private Object executeGetMethod(Object obj, String methodName) {
		Object result = null;
		try {
			Method method = obj.getClass().getMethod("Get"+methodName);
			result = method.invoke(obj);
		} catch (NoSuchMethodException e) {
			TemplateProcessor.debug("\t\tNo Such Method %s Exception!", methodName);
		} catch (Exception e) {
			TemplateProcessor.debug("\t\t !!!Exception!!!");
		}
		return result;
	}
	
}
