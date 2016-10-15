package com.github.dtsarkov.ea.tools.codegenerator;

import java.util.Arrays;
import java.util.regex.Matcher;

public class Utils {

	public static void main(String[] args) {
		String s = "[A mutable sequence of characters.]"
				 + System.lineSeparator()
				 + "[This class provides an API compatible with StringBuffer, "
				 + "but with no guarantee of synchronization.]"
				 +""+System.lineSeparator()
				 + "[This class is designed for use as a drop-in replacement for "
				 + "StringBuffer in places where the string buffer was being used "
				 + "by a single thread (as is generally the case).]"
				;
		System.out.println("--- Wrap text start ---");
		System.out.println(">         1         2         3         4         5         6         7         8         9        10");
		System.out.println(">123456789|123456789|123456789|123456789|123456789|123456789|123456789|123456789|123456789|123456789|");
		System.out.println(wrapText(s,80,">  |","|"));
		System.out.println("--- Wrap text end   ---");
		System.out.println(s.replace("\n", "").replace("\r", ""));
	}

	public static String wrapText(String text, int width, String prefix, String suffix) {
		String[] lines = text.replace("\r", "").split("\n");

		StringBuilder buffer = new StringBuilder(text.length()+lines.length*2);
		
		int len0;
		width -= ( prefix.length() + suffix.length());
		char fill[] = new char[width+5];
		Arrays.fill(fill, ' ');
		
		char[] chars;
		//System.out.printf("line # %d\n",lines.length);
		for ( int i = 0; i < lines.length; i ++) {
			len0 = lines[i].length();
			chars = lines[i].toCharArray();
			int sp = -1, nsp = -1;
			int s = 0, e = 0;
			buffer.append(prefix);
			for ( int c = 0,  nl = 1; c < len0; c++, nl++ ) {
				if ( chars[c] != ' ' ) { 
					nsp = c;
				} else {
					sp = c;
				}
				if ( nl == width ) {
					if ( sp > nsp || (c < len0-1 && chars[c+1] != ' ' ) ) {
						e = sp;
					} else {
						e = nsp+((c == len0-1) ? 0:1);
					}
					if ( e == -1 ) e = s+width-1;
					
					//System.out.printf("w=%d, s=%d, e=%d, e-s=%d, [sp=%d, nsp=%d] [c=%d,nl=%d]\n",width,s,e,e-s,sp,nsp,c,nl);
					buffer.append(chars, s, e-s);
					if (e != len0 || i < lines.length-1) {
						//buffer.append('|');
						int sp_len = width - e + s + 1;
						if (sp_len > 0 ) {
							buffer.append(fill, 0, sp_len);
						}
						buffer.append(suffix);
						buffer.append(System.lineSeparator());
						buffer.append(prefix);
					}
					c  = e; 
					s  = e+((e != len0-1) ? 1 : 0);
					nl = 1;
					sp=-1; nsp=-1;
				}
			}
			//System.out.printf("%d, %d, %d, %d, %d <\n",width,len0,s,e,e-s);
			if ( s <= len0 ) {
				buffer.append(chars, s, len0-s);
				buffer.append(fill, 0, width-(len0-s)+1);
				buffer.append(suffix);
				if (i < lines.length-1) { 
					buffer.append(System.lineSeparator());
					//buffer.append(prefix);
				}
			}
		}
		return buffer.toString();
	}

	static private final String escapes[][] = {
			 {Matcher.quoteReplacement("\\t"), 	"\t"}
			,{Matcher.quoteReplacement("\\n"), 	"\n"}
			,{Matcher.quoteReplacement("\\f"), 	"\f"}
			,{Matcher.quoteReplacement("\\r"), 	"\r"}
			,{Matcher.quoteReplacement("\\\""), "\""}
			,{Matcher.quoteReplacement("\\'"), 	"'" }
			,{Matcher.quoteReplacement("\\\\"), Matcher.quoteReplacement("\\")}
	};
	public static String translateStringLiteral( String s ) {
		String s1 = s.substring(1,s.length()-1); //remove leading and trailing double quotes
		for ( int i = 0; i < escapes.length; i++ )
			s1 = s1.replaceAll(escapes[i][0], escapes[i][1]);
		return s1; 
	}

}
