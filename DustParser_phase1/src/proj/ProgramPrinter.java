package proj;

import gen.DustLexer;
import gen.DustListener;
import gen.DustParser;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;


public class ProgramPrinter implements DustListener {

    // indentations
    public String indentation(int tab){
        String indentation = "";
        for(int i=0;i<tab*4;i++) indentation += " ";
        return indentation;
    }

    // array type
    public String splitter(DustParser.ArrayDecContext ctx){
        Object[] arrayPart = ctx.getTokens(DustParser.ID).toString().split("");
        String out = " ";
        for(int i=1;i<arrayPart.length-1;i++) out += arrayPart[i];
        return out;
    }

    // making parameters list using comma
    public String concatVarTypePairs(DustParser.ParameterContext ctx, int ParameterLength){
        String FinalOutput = "";
        for (int i=0;i<ParameterLength;i++){
            // adding comma between parameters of result up to the last parameter
            if (i!=0) FinalOutput += ", ";
            FinalOutput += (ctx.varDec(i).getChild(0))+ " " + (ctx.varDec(i).getChild(1));
        }
        return FinalOutput;
    }

    int tab = 0;


    @Override
    public void enterProgram(DustParser.ProgramContext ctx) {
        System.out.println(indentation(tab) + "program start{");
    }

    @Override
    public void exitProgram(DustParser.ProgramContext ctx) {
        System.out.println("}");
    }


    @Override
    public void enterImportclass(DustParser.ImportclassContext ctx) {
        System.out.println(indentation(++tab) + "import class: " + ctx.CLASSNAME());

    }

    @Override
    public void exitImportclass(DustParser.ImportclassContext ctx) {
        --tab;
    }

    @Override
    public void enterClassDef(DustParser.ClassDefContext ctx) {
        System.out.println( indentation(++tab) + "class: " + ctx.CLASSNAME(0) + "/" + "class parents: object, { \n");
    }

    @Override
    public void exitClassDef(DustParser.ClassDefContext ctx) {
        System.out.println(indentation(tab--) + "}");
    }

    @Override
    public void enterClass_body(DustParser.Class_bodyContext ctx) {

    }

    @Override
    public void exitClass_body(DustParser.Class_bodyContext ctx) {

    }

    @Override
    public void enterVarDec(DustParser.VarDecContext ctx) {
        tab++;
        if (!(ctx.parent instanceof DustParser.ParameterContext) & !(ctx.parent instanceof DustParser.AssignmentContext)){
            System.out.println(indentation(tab) + "field: " +  ctx.getChild(1) + "/ type= " + ctx.getChild(0));
        }
    }

    @Override
    public void exitVarDec(DustParser.VarDecContext ctx) {
        --tab;
    }

    @Override
    public void enterArrayDec(DustParser.ArrayDecContext ctx) {
        tab++;
        System.out.println(indentation(tab) + "field:" +  splitter(ctx) + "/ type= " + ctx.getChild(0));
    }


    @Override
    public void exitArrayDec(DustParser.ArrayDecContext ctx) {
        tab--;
    }

    @Override
    public void enterMethodDec(DustParser.MethodDecContext ctx) {
        System.out.println(indentation(++tab) + "class method: " + ctx.getChild(2) + "/ return type: " + ctx.getChild(1) + "{");

    }

    @Override
    public void exitMethodDec(DustParser.MethodDecContext ctx) {
        System.out.println(indentation(tab--) + "}");
    }

    @Override
    public void enterConstructor(DustParser.ConstructorContext ctx) {
        System.out.println("\n" + indentation(++tab) + "class constructor: " + ctx.getChild(1) + "{");


    }

    @Override
    public void exitConstructor(DustParser.ConstructorContext ctx) {
        System.out.println(indentation(tab--) + "}");
    }


    @Override
    public void enterParameter(DustParser.ParameterContext ctx) {
        tab++;
        int ParameterLength = ctx.getText().split(",").length;
        String FinalOutput;
        FinalOutput = concatVarTypePairs(ctx,ParameterLength);
        // add two brackets to each side of result
        FinalOutput = "[" + FinalOutput + "]";
        System.out.println(indentation(tab) + "parameter list: " + FinalOutput);
    }

    @Override
    public void exitParameter(DustParser.ParameterContext ctx) {
        tab--;
    }

    @Override
    public void enterStatement(DustParser.StatementContext ctx) {

    }

    @Override
    public void exitStatement(DustParser.StatementContext ctx) {

    }

    @Override
    public void enterReturn_statment(DustParser.Return_statmentContext ctx) {

    }

    @Override
    public void exitReturn_statment(DustParser.Return_statmentContext ctx) {

    }

    @Override
    public void enterCondition_list(DustParser.Condition_listContext ctx) {

    }

    @Override
    public void exitCondition_list(DustParser.Condition_listContext ctx) {

    }

    @Override
    public void enterCondition(DustParser.ConditionContext ctx) {

    }

    @Override
    public void exitCondition(DustParser.ConditionContext ctx) {

    }

    @Override
    public void enterIf_statment(DustParser.If_statmentContext ctx) {

    }

    @Override
    public void exitIf_statment(DustParser.If_statmentContext ctx) {

    }

    @Override
    public void enterWhile_statment(DustParser.While_statmentContext ctx) {

    }

    @Override
    public void exitWhile_statment(DustParser.While_statmentContext ctx) {

    }

    @Override
    public void enterIf_else_statment(DustParser.If_else_statmentContext ctx) {

    }

    @Override
    public void exitIf_else_statment(DustParser.If_else_statmentContext ctx) {

    }

    @Override
    public void enterPrint_statment(DustParser.Print_statmentContext ctx) {

    }

    @Override
    public void exitPrint_statment(DustParser.Print_statmentContext ctx) {

    }

    @Override
    public void enterFor_statment(DustParser.For_statmentContext ctx) {

    }

    @Override
    public void exitFor_statment(DustParser.For_statmentContext ctx) {

    }

    @Override
    public void enterMethod_call(DustParser.Method_callContext ctx) {

    }

    @Override
    public void exitMethod_call(DustParser.Method_callContext ctx) {

    }

    @Override
    public void enterAssignment(DustParser.AssignmentContext ctx) {
    }

    @Override
    public void exitAssignment(DustParser.AssignmentContext ctx) {

    }

    @Override
    public void enterExp(DustParser.ExpContext ctx) {

    }

    @Override
    public void exitExp(DustParser.ExpContext ctx) {

    }

    @Override
    public void enterPrefixexp(DustParser.PrefixexpContext ctx) {

    }

    @Override
    public void exitPrefixexp(DustParser.PrefixexpContext ctx) {

    }

    @Override
    public void enterArgs(DustParser.ArgsContext ctx) {

    }

    @Override
    public void exitArgs(DustParser.ArgsContext ctx) {

    }

    @Override
    public void enterExplist(DustParser.ExplistContext ctx) {

    }

    @Override
    public void exitExplist(DustParser.ExplistContext ctx) {

    }

    @Override
    public void enterArithmetic_operator(DustParser.Arithmetic_operatorContext ctx) {

    }

    @Override
    public void exitArithmetic_operator(DustParser.Arithmetic_operatorContext ctx) {

    }

    @Override
    public void enterRelational_operators(DustParser.Relational_operatorsContext ctx) {

    }

    @Override
    public void exitRelational_operators(DustParser.Relational_operatorsContext ctx) {

    }

    @Override
    public void enterAssignment_operators(DustParser.Assignment_operatorsContext ctx) {

    }

    @Override
    public void exitAssignment_operators(DustParser.Assignment_operatorsContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }
}
