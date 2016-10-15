// Generated from ..\Parser\JSON.g4 by ANTLR 4.5.1
package com.github.dtsarkov.ea.tools.load.json.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSONLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, BOOLEAN=7, NULL=8, STRING=9, 
		DOUBLE=10, INTEGER=11, WS=12;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "BOOLEAN", "NULL", "STRING", 
		"ESC", "UNICODE", "HEX", "DOUBLE", "INTEGER", "INT", "EXP", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'{'", "','", "'}'", "':'", "'['", "']'", null, "'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "BOOLEAN", "NULL", "STRING", 
		"DOUBLE", "INTEGER", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JSONLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSON.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\16\u0089\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\5\b;\n\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\7\nE\n\n"+
		"\f\n\16\nH\13\n\3\n\3\n\3\13\3\13\3\13\5\13O\n\13\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\r\3\r\3\16\5\16Z\n\16\3\16\3\16\3\16\6\16_\n\16\r\16\16\16`\3\16"+
		"\5\16d\n\16\3\16\5\16g\n\16\3\16\3\16\3\16\5\16l\n\16\3\17\5\17o\n\17"+
		"\3\17\3\17\3\20\3\20\3\20\7\20v\n\20\f\20\16\20y\13\20\5\20{\n\20\3\21"+
		"\3\21\5\21\177\n\21\3\21\3\21\3\22\6\22\u0084\n\22\r\22\16\22\u0085\3"+
		"\22\3\22\2\2\23\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\2\27\2\31"+
		"\2\33\f\35\r\37\2!\2#\16\3\2\n\4\2$$^^\n\2$$\61\61^^ddhhppttvv\5\2\62"+
		";CHch\3\2\62;\3\2\63;\4\2GGgg\4\2--//\5\2\13\f\17\17\"\"\u0091\2\3\3\2"+
		"\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17"+
		"\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2#\3\2\2"+
		"\2\3%\3\2\2\2\5\'\3\2\2\2\7)\3\2\2\2\t+\3\2\2\2\13-\3\2\2\2\r/\3\2\2\2"+
		"\17:\3\2\2\2\21<\3\2\2\2\23A\3\2\2\2\25K\3\2\2\2\27P\3\2\2\2\31V\3\2\2"+
		"\2\33k\3\2\2\2\35n\3\2\2\2\37z\3\2\2\2!|\3\2\2\2#\u0083\3\2\2\2%&\7}\2"+
		"\2&\4\3\2\2\2\'(\7.\2\2(\6\3\2\2\2)*\7\177\2\2*\b\3\2\2\2+,\7<\2\2,\n"+
		"\3\2\2\2-.\7]\2\2.\f\3\2\2\2/\60\7_\2\2\60\16\3\2\2\2\61\62\7v\2\2\62"+
		"\63\7t\2\2\63\64\7w\2\2\64;\7g\2\2\65\66\7h\2\2\66\67\7c\2\2\678\7n\2"+
		"\289\7u\2\29;\7g\2\2:\61\3\2\2\2:\65\3\2\2\2;\20\3\2\2\2<=\7p\2\2=>\7"+
		"w\2\2>?\7n\2\2?@\7n\2\2@\22\3\2\2\2AF\7$\2\2BE\5\25\13\2CE\n\2\2\2DB\3"+
		"\2\2\2DC\3\2\2\2EH\3\2\2\2FD\3\2\2\2FG\3\2\2\2GI\3\2\2\2HF\3\2\2\2IJ\7"+
		"$\2\2J\24\3\2\2\2KN\7^\2\2LO\t\3\2\2MO\5\27\f\2NL\3\2\2\2NM\3\2\2\2O\26"+
		"\3\2\2\2PQ\7w\2\2QR\5\31\r\2RS\5\31\r\2ST\5\31\r\2TU\5\31\r\2U\30\3\2"+
		"\2\2VW\t\4\2\2W\32\3\2\2\2XZ\7/\2\2YX\3\2\2\2YZ\3\2\2\2Z[\3\2\2\2[\\\5"+
		"\37\20\2\\^\7\60\2\2]_\t\5\2\2^]\3\2\2\2_`\3\2\2\2`^\3\2\2\2`a\3\2\2\2"+
		"ac\3\2\2\2bd\5!\21\2cb\3\2\2\2cd\3\2\2\2dl\3\2\2\2eg\7/\2\2fe\3\2\2\2"+
		"fg\3\2\2\2gh\3\2\2\2hi\5\37\20\2ij\5!\21\2jl\3\2\2\2kY\3\2\2\2kf\3\2\2"+
		"\2l\34\3\2\2\2mo\7/\2\2nm\3\2\2\2no\3\2\2\2op\3\2\2\2pq\5\37\20\2q\36"+
		"\3\2\2\2r{\7\62\2\2sw\t\6\2\2tv\t\5\2\2ut\3\2\2\2vy\3\2\2\2wu\3\2\2\2"+
		"wx\3\2\2\2x{\3\2\2\2yw\3\2\2\2zr\3\2\2\2zs\3\2\2\2{ \3\2\2\2|~\t\7\2\2"+
		"}\177\t\b\2\2~}\3\2\2\2~\177\3\2\2\2\177\u0080\3\2\2\2\u0080\u0081\5\37"+
		"\20\2\u0081\"\3\2\2\2\u0082\u0084\t\t\2\2\u0083\u0082\3\2\2\2\u0084\u0085"+
		"\3\2\2\2\u0085\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0087\3\2\2\2\u0087"+
		"\u0088\b\22\2\2\u0088$\3\2\2\2\21\2:DFNY`cfknwz~\u0085\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}