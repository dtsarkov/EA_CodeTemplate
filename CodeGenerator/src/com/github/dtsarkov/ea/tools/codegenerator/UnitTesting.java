package com.github.dtsarkov.ea.tools.codegenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class UnitTesting {

	public static void main(String[] args) throws Exception {
		String[] testArgs = new String[] {
				 "--model", "..\\CodeGeneratorDemo.EAP"
				,"-b", "TestCases\\TestCases.lst", "-f", "TestCases", "-x", "template", "-v" //,"-d"
				, "--variable", "CMD_VAR_1=Global variable CMD_VAR_1 value" 
				, "-D", "CMD_VAR_2=Global variable CMD_VAR_2 value" 
				//,"-t", "TestCases\\listConnectors.template"	,"-e", "Activity2" ,"-o", "test.out", "-v", //"-q", "Package:Element" //, "-d"
				//,"-v"
				//,"-d"
			};
		System.out.println(
				"*** Delete .out files ********************************************************************"
		);
		File outFolder = new File(".");
		File[] outFiles = outFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".out");
			}
		});
		for( File f : outFiles ) {
			f.delete();
		}

		System.out.println(
				"*** Unit Testing Start ******************************************************************"
		);
		Generator.main(testArgs);

		System.out.println(
				"*** Unit Testing Comparing files ********************************************************"
		);
		verifyResults();
		System.out.println(
				"*** Unit Testing End ********************************************************************"
		);
	}

	private static void verifyResults() {
		File testFolder = new File("TestCases");
		File[] baselineFiles = testFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".baseline");
			}
		});
		
		File outFile = null;
		String [] fileName;
		LineNumberReader	baseline, output;
		String				line1, line2;
		int					lineCounter;
		for (int i = 0; i < baselineFiles.length; i++) {
			fileName = baselineFiles[i].getName().split("\\.");
			System.out.printf("%-80s ...", baselineFiles[i]);
			outFile = new File(fileName[0]+".out");
			
			if ( !outFile.exists() ) {
				System.out.println("FAILED!");
				System.out.printf("\t\tFile \"%s\" does not exist\n", outFile);
				continue;
			}
			
			try {
				baseline = new LineNumberReader(new FileReader(baselineFiles[i]));
				output	 = new LineNumberReader(new FileReader(outFile));
			} catch (FileNotFoundException e) {
				System.out.println("FAILED!");
				System.out.printf("\t\tCould not find baseline \"%s\" \n\t\tor output \"%s\" \n"
						,baselineFiles[i], outFile
				);
				continue;
			}
			
			try {
				line1 = baseline.readLine();
				line2 = output.readLine();
				
				lineCounter = 0;
				while(line1 != null) {
					lineCounter++;
					
					if ( line2 == null || line1.compareTo(line2) != 0) {
						System.out.println("FAILED!");
						System.out.printf("\t\tMissmatch in line %d\n\t\tbase:[%s]\n\t\tout :[%s]\n"
								,lineCounter, line1, line2
						);
						break;
					}
					line1 = baseline.readLine();
					line2 = output.readLine();
				}
				baseline.close();
				output.close();
				
				if ( line1 != null ) continue;
				if ( line2 != null ) {
					System.out.println("FAILED!");
					System.out.printf("\t\tNew output file contains more lines\n");
					continue;
				}
			} catch( IOException e ) {
				System.out.println("FAILED!");
				System.out.printf("\t\tCould not read baseline \"%s\" \n\t\tor output \"%s\" \n"
						,baselineFiles[i], outFile
				);
			}
			System.out.println("OK");
		}
	}
}
