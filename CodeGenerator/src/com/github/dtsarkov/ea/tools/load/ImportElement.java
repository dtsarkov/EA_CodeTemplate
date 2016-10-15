package com.github.dtsarkov.ea.tools.load;

import java.util.HashMap;

public class ImportElement {
	static private int elementCount = 0;
	
	private String name;
	private String type;
	

	private HashMap<String,Object> attributes;
	
	public ImportElement() {
		elementCount++;
		//Assign default name
		setName(name = "Elment "+elementCount);
		setType("Class");
		attributes = new HashMap<String,Object>(10);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void addAttriubute(String name, Object value) {
		if ( name.equalsIgnoreCase("name") ) {
			setName(value.toString());
		}
		if ( name.equalsIgnoreCase("type") ) {
			setType(value.toString());
		} else {
			attributes.put(name, value);
		}
	}
	
	public String[] getAttributes() {
		String[] list = new String[0];
		return attributes.keySet().toArray(list);
	}
	
	public Object getAttributeValue(String name) {
		return attributes.get(name);
	}
}
