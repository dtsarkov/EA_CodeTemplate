// Generated from EACodeTemplate.g4 by ANTLR 4.5.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EACodeTemplateParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, STRING=14, CHAR=15, ID=16, PI=17;
	public static final int
		RULE_file = 0, RULE_line = 1, RULE_assignment = 2, RULE_op = 3, RULE_expression = 4, 
		RULE_expr = 5, RULE_variable = 6, RULE_comment = 7, RULE_macros = 8, RULE_procInstruction = 9, 
		RULE_list = 10, RULE_templateCall = 11;
	public static final String[] ruleNames = {
		"file", "line", "assignment", "op", "expression", "expr", "variable", 
		"comment", "macros", "procInstruction", "list", "templateCall"
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

	@Override
	public String getGrammarFileName() { return "EACodeTemplate.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public EACodeTemplateParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class FileContext extends ParserRuleContext {
		public List<LineContext> line() {
			return getRuleContexts(LineContext.class);
		}
		public LineContext line(int i) {
			return getRuleContext(LineContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitFile(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << STRING) | (1L << CHAR) | (1L << ID) | (1L << PI))) != 0)) {
				{
				{
				setState(24);
				line();
				setState(26);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(25);
					match(T__0);
					}
				}

				setState(28);
				match(T__1);
				}
				}
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}

						System.out.println("");
					
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LineContext extends ParserRuleContext {
		public Token txt;
		public List<AssignmentContext> assignment() {
			return getRuleContexts(AssignmentContext.class);
		}
		public AssignmentContext assignment(int i) {
			return getRuleContext(AssignmentContext.class,i);
		}
		public List<CommentContext> comment() {
			return getRuleContexts(CommentContext.class);
		}
		public CommentContext comment(int i) {
			return getRuleContext(CommentContext.class,i);
		}
		public List<MacrosContext> macros() {
			return getRuleContexts(MacrosContext.class);
		}
		public MacrosContext macros(int i) {
			return getRuleContext(MacrosContext.class,i);
		}
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_line);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1+1 ) {
					{
					setState(42);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						setState(37);
						assignment();
						}
						break;
					case 2:
						{
						setState(38);
						comment();
						}
						break;
					case 3:
						{
						setState(39);
						macros();
						}
						break;
					case 4:
						{
						setState(40);
						((LineContext)_localctx).txt = matchWildcard();

									System.out.print((((LineContext)_localctx).txt!=null?((LineContext)_localctx).txt.getText():null));
								
						}
						break;
					}
					} 
				}
				setState(46);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssignmentContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public OpContext op() {
			return getRuleContext(OpContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitAssignment(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			variable();
			setState(48);
			op();
			setState(49);
			expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OpContext extends ParserRuleContext {
		public OpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitOp(this);
		}
	}

	public final OpContext op() throws RecognitionException {
		OpContext _localctx = new OpContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			_la = _input.LA(1);
			if ( !(_la==T__2 || _la==T__3) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_expression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			expr();
			setState(58);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(54);
					match(T__4);
					setState(55);
					expr();
					}
					} 
				}
				setState(60);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode STRING() { return getToken(EACodeTemplateParser.STRING, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_expr);
		try {
			setState(64);
			switch (_input.LA(1)) {
			case T__5:
				enterOuterAlt(_localctx, 1);
				{
				setState(61);
				variable();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				setState(62);
				list();
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(63);
				match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(EACodeTemplateParser.ID, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(T__5);
			setState(67);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(EACodeTemplateParser.STRING, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitComment(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(T__6);
			setState(70);
			match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MacrosContext extends ParserRuleContext {
		public ProcInstructionContext procInstruction() {
			return getRuleContext(ProcInstructionContext.class,0);
		}
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TemplateCallContext templateCall() {
			return getRuleContext(TemplateCallContext.class,0);
		}
		public MacrosContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macros; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterMacros(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitMacros(this);
		}
	}

	public final MacrosContext macros() throws RecognitionException {
		MacrosContext _localctx = new MacrosContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_macros);
		try {
			setState(75);
			switch (_input.LA(1)) {
			case T__7:
				enterOuterAlt(_localctx, 1);
				{
				setState(72);
				procInstruction();
				}
				break;
			case T__10:
				enterOuterAlt(_localctx, 2);
				{
				setState(73);
				list();
				}
				break;
			case T__12:
				enterOuterAlt(_localctx, 3);
				{
				setState(74);
				templateCall();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ProcInstructionContext extends ParserRuleContext {
		public List<TerminalNode> PI() { return getTokens(EACodeTemplateParser.PI); }
		public TerminalNode PI(int i) {
			return getToken(EACodeTemplateParser.PI, i);
		}
		public ProcInstructionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procInstruction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterProcInstruction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitProcInstruction(this);
		}
	}

	public final ProcInstructionContext procInstruction() throws RecognitionException {
		ProcInstructionContext _localctx = new ProcInstructionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_procInstruction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(T__7);
			setState(78);
			match(T__2);
			setState(79);
			match(T__8);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PI) {
				{
				{
				setState(80);
				match(PI);
				}
				}
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(86);
			match(T__9);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(EACodeTemplateParser.ID, 0); }
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitList(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		ListContext _localctx = new ListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(T__10);
			setState(89);
			match(T__2);
			setState(90);
			match(T__8);
			setState(91);
			match(ID);
			setState(92);
			match(T__8);
			setState(93);
			match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TemplateCallContext extends ParserRuleContext {
		public Token ID;
		public TerminalNode ID() { return getToken(EACodeTemplateParser.ID, 0); }
		public TemplateCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_templateCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).enterTemplateCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EACodeTemplateListener ) ((EACodeTemplateListener)listener).exitTemplateCall(this);
		}
	}

	public final TemplateCallContext templateCall() throws RecognitionException {
		TemplateCallContext _localctx = new TemplateCallContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_templateCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			match(T__12);
			setState(96);
			((TemplateCallContext)_localctx).ID = match(ID);
			setState(97);
			match(T__12);

						System.out.print("!template!."+(((TemplateCallContext)_localctx).ID!=null?((TemplateCallContext)_localctx).ID.getText():null));
					
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\23g\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\4\r\t\r\3\2\3\2\5\2\35\n\2\3\2\3\2\7\2!\n\2\f\2\16\2$\13\2\3\2"+
		"\3\2\3\3\3\3\3\3\3\3\3\3\7\3-\n\3\f\3\16\3\60\13\3\3\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\6\3\6\3\6\7\6;\n\6\f\6\16\6>\13\6\3\7\3\7\3\7\5\7C\n\7\3\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\n\3\n\3\n\5\nN\n\n\3\13\3\13\3\13\3\13\7\13T\n\13\f"+
		"\13\16\13W\13\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3.\2\16\2\4\6\b\n\f\16\20\22\24\26\30\2\3\3\2\5\6f\2\"\3\2\2"+
		"\2\4.\3\2\2\2\6\61\3\2\2\2\b\65\3\2\2\2\n\67\3\2\2\2\fB\3\2\2\2\16D\3"+
		"\2\2\2\20G\3\2\2\2\22M\3\2\2\2\24O\3\2\2\2\26Z\3\2\2\2\30a\3\2\2\2\32"+
		"\34\5\4\3\2\33\35\7\3\2\2\34\33\3\2\2\2\34\35\3\2\2\2\35\36\3\2\2\2\36"+
		"\37\7\4\2\2\37!\3\2\2\2 \32\3\2\2\2!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%"+
		"\3\2\2\2$\"\3\2\2\2%&\b\2\1\2&\3\3\2\2\2\'-\5\6\4\2(-\5\20\t\2)-\5\22"+
		"\n\2*+\13\2\2\2+-\b\3\1\2,\'\3\2\2\2,(\3\2\2\2,)\3\2\2\2,*\3\2\2\2-\60"+
		"\3\2\2\2./\3\2\2\2.,\3\2\2\2/\5\3\2\2\2\60.\3\2\2\2\61\62\5\16\b\2\62"+
		"\63\5\b\5\2\63\64\5\n\6\2\64\7\3\2\2\2\65\66\t\2\2\2\66\t\3\2\2\2\67<"+
		"\5\f\7\289\7\7\2\29;\5\f\7\2:8\3\2\2\2;>\3\2\2\2<:\3\2\2\2<=\3\2\2\2="+
		"\13\3\2\2\2><\3\2\2\2?C\5\16\b\2@C\5\26\f\2AC\7\20\2\2B?\3\2\2\2B@\3\2"+
		"\2\2BA\3\2\2\2C\r\3\2\2\2DE\7\b\2\2EF\7\22\2\2F\17\3\2\2\2GH\7\t\2\2H"+
		"I\7\20\2\2I\21\3\2\2\2JN\5\24\13\2KN\5\26\f\2LN\5\30\r\2MJ\3\2\2\2MK\3"+
		"\2\2\2ML\3\2\2\2N\23\3\2\2\2OP\7\n\2\2PQ\7\5\2\2QU\7\13\2\2RT\7\23\2\2"+
		"SR\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2VX\3\2\2\2WU\3\2\2\2XY\7\f\2\2"+
		"Y\25\3\2\2\2Z[\7\r\2\2[\\\7\5\2\2\\]\7\13\2\2]^\7\22\2\2^_\7\13\2\2_`"+
		"\7\16\2\2`\27\3\2\2\2ab\7\17\2\2bc\7\22\2\2cd\7\17\2\2de\b\r\1\2e\31\3"+
		"\2\2\2\n\34\",.<BMU";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}