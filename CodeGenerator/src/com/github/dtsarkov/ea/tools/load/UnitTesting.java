package com.github.dtsarkov.ea.tools.load;

public final class UnitTesting {

	public static void main(String[] args) {
		String[] testArgs = new String[] {""
				,"--model", "..\\CodeGeneratorDemo.EAP"
				//,"-e","GDW 2.0 Databases"
				,"-e","\"TCF Stream\"::Shadow (678)"
				//,"-v"
				,"-d"
				,"C:\\Work\\Development\\EA\\EA_CodeTemplate\\CodeGenerator\\TestCases\\Classes.json"
				,""
				//,"C:\\Work\\Development\\EA\\EA_CodeTemplate\\CodeGenerator\\Stream_865_1.json"
				//,"C:\\Work\\Development\\EA\\EA_CodeTemplate\\CodeGenerator\\Stream_865_2.json"
				//,"C:\\Work\\Development\\EA\\EA_CodeTemplate\\CodeGenerator\\Stream_865_3.json"
			};
//		String flag;
//		for ( Method method : org.sparx.Element.class.getDeclaredMethods() ) {
//			if ( (method.getModifiers() & Modifier.PUBLIC) == 1) {
//				flag = "";
//				if ( method.getName().startsWith("Set") ) {
//					flag = method.getParameterTypes()[0].getName();
//				} else if ( method.getName().startsWith("Get") && method.getReturnType().getName().startsWith("org")) {
//					flag = "array";
//				}
//				
//				System.out.printf("%-30s|%25s|%s\n"
//						,method.getName()
//						,method.getReturnType().getName()
//						,flag
//						);
//				}
//			
//		}
		
		ImportFile.main(testArgs);

	}

}
