// Generated from EACodeTemplate.g4 by ANTLR 4.5.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EACodeTemplateLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, STRING=14, CHAR=15, ID=16, PI=17;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "STRING", "CHAR", "ID", "PI"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\r'", "'\n'", "'='", "'+='", "'+'", "'$'", "'$COMMENT='", "'%PI'", 
		"'\"'", "'\"%'", "'%list'", "'@separator='", "'%'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, "STRING", "CHAR", "ID", "PI"
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


	public EACodeTemplateLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "EACodeTemplate.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\23t\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16"+
		"\3\16\3\17\3\17\7\17\\\n\17\f\17\16\17_\13\17\3\17\3\17\3\20\3\20\3\21"+
		"\3\21\7\21g\n\21\f\21\16\21j\13\21\3\22\3\22\3\22\3\22\3\22\6\22q\n\22"+
		"\r\22\16\22r\3]\2\23\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27"+
		"\r\31\16\33\17\35\20\37\21!\22#\23\3\2\4\4\2C\\c|\5\2\62;C\\c|x\2\3\3"+
		"\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2"+
		"\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3"+
		"\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3"+
		"%\3\2\2\2\5\'\3\2\2\2\7)\3\2\2\2\t+\3\2\2\2\13.\3\2\2\2\r\60\3\2\2\2\17"+
		"\62\3\2\2\2\21<\3\2\2\2\23@\3\2\2\2\25B\3\2\2\2\27E\3\2\2\2\31K\3\2\2"+
		"\2\33W\3\2\2\2\35Y\3\2\2\2\37b\3\2\2\2!d\3\2\2\2#p\3\2\2\2%&\7\17\2\2"+
		"&\4\3\2\2\2\'(\7\f\2\2(\6\3\2\2\2)*\7?\2\2*\b\3\2\2\2+,\7-\2\2,-\7?\2"+
		"\2-\n\3\2\2\2./\7-\2\2/\f\3\2\2\2\60\61\7&\2\2\61\16\3\2\2\2\62\63\7&"+
		"\2\2\63\64\7E\2\2\64\65\7Q\2\2\65\66\7O\2\2\66\67\7O\2\2\678\7G\2\289"+
		"\7P\2\29:\7V\2\2:;\7?\2\2;\20\3\2\2\2<=\7\'\2\2=>\7R\2\2>?\7K\2\2?\22"+
		"\3\2\2\2@A\7$\2\2A\24\3\2\2\2BC\7$\2\2CD\7\'\2\2D\26\3\2\2\2EF\7\'\2\2"+
		"FG\7n\2\2GH\7k\2\2HI\7u\2\2IJ\7v\2\2J\30\3\2\2\2KL\7B\2\2LM\7u\2\2MN\7"+
		"g\2\2NO\7r\2\2OP\7c\2\2PQ\7t\2\2QR\7c\2\2RS\7v\2\2ST\7q\2\2TU\7t\2\2U"+
		"V\7?\2\2V\32\3\2\2\2WX\7\'\2\2X\34\3\2\2\2Y]\7$\2\2Z\\\13\2\2\2[Z\3\2"+
		"\2\2\\_\3\2\2\2]^\3\2\2\2][\3\2\2\2^`\3\2\2\2_]\3\2\2\2`a\7$\2\2a\36\3"+
		"\2\2\2bc\t\2\2\2c \3\2\2\2dh\5\37\20\2eg\t\3\2\2fe\3\2\2\2gj\3\2\2\2h"+
		"f\3\2\2\2hi\3\2\2\2i\"\3\2\2\2jh\3\2\2\2kq\7\"\2\2lm\7^\2\2mq\7p\2\2n"+
		"o\7^\2\2oq\7v\2\2pk\3\2\2\2pl\3\2\2\2pn\3\2\2\2qr\3\2\2\2rp\3\2\2\2rs"+
		"\3\2\2\2s$\3\2\2\2\7\2]hpr\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}