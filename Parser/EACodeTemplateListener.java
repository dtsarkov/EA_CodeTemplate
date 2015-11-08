// Generated from EACodeTemplate.g4 by ANTLR 4.5.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EACodeTemplateParser}.
 */
public interface EACodeTemplateListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#file}.
	 * @param ctx the parse tree
	 */
	void enterFile(EACodeTemplateParser.FileContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#file}.
	 * @param ctx the parse tree
	 */
	void exitFile(EACodeTemplateParser.FileContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(EACodeTemplateParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(EACodeTemplateParser.LineContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(EACodeTemplateParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(EACodeTemplateParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#op}.
	 * @param ctx the parse tree
	 */
	void enterOp(EACodeTemplateParser.OpContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#op}.
	 * @param ctx the parse tree
	 */
	void exitOp(EACodeTemplateParser.OpContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(EACodeTemplateParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(EACodeTemplateParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(EACodeTemplateParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(EACodeTemplateParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(EACodeTemplateParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(EACodeTemplateParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(EACodeTemplateParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(EACodeTemplateParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#macros}.
	 * @param ctx the parse tree
	 */
	void enterMacros(EACodeTemplateParser.MacrosContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#macros}.
	 * @param ctx the parse tree
	 */
	void exitMacros(EACodeTemplateParser.MacrosContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#procInstruction}.
	 * @param ctx the parse tree
	 */
	void enterProcInstruction(EACodeTemplateParser.ProcInstructionContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#procInstruction}.
	 * @param ctx the parse tree
	 */
	void exitProcInstruction(EACodeTemplateParser.ProcInstructionContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(EACodeTemplateParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(EACodeTemplateParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link EACodeTemplateParser#templateCall}.
	 * @param ctx the parse tree
	 */
	void enterTemplateCall(EACodeTemplateParser.TemplateCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link EACodeTemplateParser#templateCall}.
	 * @param ctx the parse tree
	 */
	void exitTemplateCall(EACodeTemplateParser.TemplateCallContext ctx);
}