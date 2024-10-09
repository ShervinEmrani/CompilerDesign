package proj;
import java.util.*;
import gen.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class ProgramPrinter implements DustListener {





    SymbolTable symbolTable = new SymbolTable();
    private String currentMethodName;
    private String currentReturnType;
    private String currentScope;
    private String currPar;

    //////////////////
    private final SymbolTable2 symbolTable2 = new SymbolTable2();
    private String currentName = "";
    private String currentSymbol = "";
    public void printSymbolTable() {
        this.symbolTable2.printSymbolTable();
    }

    //////////////////

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
            int index = i+1;
            FinalOutput += ( "name: " + ctx.varDec(i).getChild(1)) + ", " + " " + "type: " +
                    (ctx.varDec(i).getChild(0) + " ," + "index: " + index);
        }
        return FinalOutput;
    }

    // function to return array size
    private int arr_size(String array) {
        int startIndex = array.indexOf('[');
        int endIndex = array.indexOf(']');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            String extractedNumber = array.substring(startIndex + 1, endIndex);
                return Integer.parseInt(extractedNumber);
        }
        return 0; // Default value if array extraction fails or number parsing fails
    }



    public int tab = 0;
    public boolean method_enter = false;


    @Override
    public void enterProgram(DustParser.ProgramContext ctx) {
        this.symbolTable.enterScope();
        this.symbolTable.addScope("program", ctx.getStart().getLine());
        currentScope = "program";
        System.out.println(indentation(tab) + "program start{");
        this.symbolTable2.enterBlock("program", ctx.getStart().getLine());
    }

    @Override
    public void exitProgram(DustParser.ProgramContext ctx) {
        this.symbolTable.exitScope();
        this.symbolTable.Print_table();
        System.out.println("}");
        this.symbolTable2.exitBlock();
        this.symbolTable2.printSymbolTable();
    }

    // classes imported in file
    private boolean check_class(File file, String name_class) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isInsideClass = false;
            String currentClassName = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("class ")) {
                    int classNameStartIndex = line.indexOf("class ") + 6;
                    int classNameEndIndex = line.indexOf(" ", classNameStartIndex);
                    if (classNameEndIndex == -1) {
                        classNameEndIndex = line.indexOf("{", classNameStartIndex);
                    }
                    currentClassName = line.substring(classNameStartIndex, classNameEndIndex);
                    isInsideClass = currentClassName.equals(name_class);
                }

                if (isInsideClass && line.equals("}")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // files containing given class
    public boolean check_file(String name_class) {
        String directoryPath = "src/";

        try (Stream<Path> filePathStream = Files.walk(Paths.get(directoryPath))) {
            return filePathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".ds"))
                    .anyMatch(path -> check_class(path.toFile(), name_class));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return false; // Error occurred or no matching class file found
    }



    @Override
    public void enterImportclass(DustParser.ImportclassContext ctx) {
        System.out.println(indentation(++tab) + "import class: " + ctx.CLASSNAME());
        String className = ctx.CLASSNAME().getText();
        int lineNumber = ctx.getStart().getLine();
        // Check if the imported file is valid
        if (!check_file(className)){
            System.out.println("at line: " + lineNumber + ", imported class doesn't exist");
        }

    }

    @Override
    public void exitImportclass(DustParser.ImportclassContext ctx) {tab--;
    }

    @Override
    public void enterClassDef(DustParser.ClassDefContext ctx) {
//        String classParent = "";
//        String className = ctx.CLASSNAME(0).toString();
//        try {
//            classParent = ctx.CLASSNAME().get(1).toString();
//        } catch (IndexOutOfBoundsException e) {
//            classParent = "object";
//        }
//
//        this.symbolTable.addSymbolClass(className, "Class", classParent);
//        this.symbolTable.enterScope();
//        this.symbolTable.addScope(className, ctx.getStart().getLine());

        System.out.println(indentation(++tab) + "class: " + ctx.CLASSNAME(0) + "/" + "class parents: object, { \n");

        String name_of_class = ctx.CLASSNAME().get(0).getText();
        String class_symbol = "Class_" + name_of_class;
        StringBuilder parents = new StringBuilder();
        int lineNumber = ctx.getStart().getLine();
        // get parents numbers
        int parentNumbers = ctx.CLASSNAME().size() - 1;

        if (parentNumbers != 0) {
            for (int i = 1; i < parentNumbers + 1; i++) {
                if (i == parentNumbers)  parents.append(ctx.CLASSNAME(i));
                else parents.append(ctx.CLASSNAME(i)).append(", ");

            }
        } else {
            parents = new StringBuilder("object");
        }

        this.symbolTable2.addSymbolClass(class_symbol, name_of_class, parents.toString(), "Class");
        symbolTable2.enterBlock(name_of_class, lineNumber);
    }

    @Override
    public void exitClassDef(DustParser.ClassDefContext ctx) {
//        this.symbolTable.exitScope();
        System.out.println(indentation(tab--) + "}");
        symbolTable2.exitBlock();
    }

    @Override
    public void enterClass_body(DustParser.Class_bodyContext ctx) {
    }

    @Override
    public void exitClass_body(DustParser.Class_bodyContext ctx) {

    }

    @Override
    public void enterVarDec(DustParser.VarDecContext ctx) {

        ++tab;
        if (!(ctx.parent instanceof DustParser.ParameterContext) & !(ctx.parent instanceof DustParser.AssignmentContext)){
            System.out.println(indentation(tab) + "field: " +  ctx.getChild(1) + "/ type= " + ctx.getChild(0));
        }
        if (ctx.getParent() instanceof DustParser.Class_bodyContext){
            String fieldName = ctx.getChild(1).toString();
            String fieldType = ctx.getChild(0).toString();
            boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
            this.symbolTable.addSymbolField(fieldName, fieldType, isDefined, "ClassField");

        }

        if (!(ctx.getParent() instanceof DustParser.ParameterContext)) {
            String var_name = ctx.ID().getText();
            String var_type = ctx.getChild(0).getText();
            boolean define = true;
            // search for defined values in scope
            SymbolTable2.SymbolNode check_define = this.symbolTable2.getVariableType(var_name, "Field_" + var_name);
            // if any value is found, the check define must be filled with it
            if (check_define != null) {
                // printing the error of declaring a variable more than once
                System.out.println(var_name + " causes duplicate define at line: " + ctx.getStart().getLine());

                // check for the value's type
                String check_define_type = check_define.getType();
                if (check_define_type.equals(var_type)) {
                    define = false;
                }
            }
            // if the type of value is defined, it is either defined in a method or a class
            if (define) {
                boolean var_type_defined = symbolTable2.isTypeDefined(var_type);
                String field_check = "";
                if (method_enter){
                    field_check = "MethodVar";
                }
                else{
                    field_check = "ClassField";
                }
                // append the variable to method/class
                symbolTable2.addSymbolField("Field_" + var_name, var_name, var_type, var_type_defined, field_check);
            }
        }

    }

    @Override
    public void exitVarDec(DustParser.VarDecContext ctx) {
        tab--;
    }

    @Override
    public void enterArrayDec(DustParser.ArrayDecContext ctx) {
        ++tab;
        String fieldType = ctx.getChild(0).toString();
        boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
        if(ctx.getParent() instanceof DustParser.Class_bodyContext){
            this.symbolTable.addSymbolField(splitter(ctx).toString(), fieldType, isDefined, "ClassField");
        }
        System.out.println(indentation(tab) + "field:" +  splitter(ctx) + "/ type= " + ctx.getChild(0));

        // the same as VarDec
        if (!(ctx.getParent() instanceof DustParser.ParameterContext)) {
            String arr_type = ctx.getChild(0).getText();
            String arr_name = ctx.ID().getText();
            String arr_size = ctx.getChild(2).getText();
            boolean define = true;
            SymbolTable2.SymbolNode check_define = this.symbolTable2.getVariableType(arr_name, "Field_" + arr_name);

            if (check_define != null) {
                System.out.println("error at line: " + ctx.getStart().getLine() + ", " + arr_name + " is already defined");

                String check_define_type = check_define.getType();

                if (check_define_type.equals(arr_type)){
                    define = false;
                }
            }
            if (define) {
                boolean isFieldDefined = symbolTable2.isTypeDefined(arr_type);
                String field_check = "";
                if (method_enter) {
                    field_check = "MethodVar";
                }else {
                    field_check = "ClassField";
                }

                symbolTable2.addSymbolField("Field_".concat(arr_name), arr_name, arr_type.concat("[" + arr_size + "]"), isFieldDefined, field_check);
            }
        }
    }


    @Override
    public void exitArrayDec(DustParser.ArrayDecContext ctx) {
        tab--;
    }

    @Override
    public void enterMethodDec(DustParser.MethodDecContext ctx) {
        method_enter = true;
        currentMethodName = ctx.getChild(2).toString();
        currentReturnType = ctx.getChild(1).toString();


        this.symbolTable.addSymbolMethod(currentMethodName, currentReturnType, currPar, "Method");

        this.symbolTable.enterScope();
        this.symbolTable.addScope(currentMethodName, ctx.getStart().getLine());


        System.out.println(indentation(++tab) + "class method: " + ctx.getChild(2) + "/ return type: " + ctx.getChild(1) + "{");

        for ( DustParser.StatementContext statementContext : ctx.statement()){
            if( statementContext.arrayDec() != null){
                String fieldName = statementContext.arrayDec().getChild(4).getText();
                String fieldType = statementContext.arrayDec().getChild(0).getText();
                boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
                this.symbolTable.addSymbolField(fieldName, fieldType, isDefined, "MethodVar");
            }
            else if(statementContext.varDec() != null){
                String fieldName = statementContext.varDec().getChild(1).getText();
                String fieldType = statementContext.varDec().getChild(0).getText();
                boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
                this.symbolTable.addSymbolField(fieldName, fieldType, isDefined, "MethodVar");
            }
        }

        String name_method = ctx.ID().getText();
        String type_return = ctx.getChild(1).getText();
        List<String> list_parameter = new ArrayList<>();
        if (ctx.parameter().size() > 0) {
            for (int i = 0; i < ctx.parameter(0).children.size(); i += 2) {
                String paramName = ctx.parameter(0).getChild(i).getChild(1).getText();
                String paramType = ctx.parameter(0).getChild(i).getChild(0).getText();
                list_parameter.add(paramType + " " + paramName);

            }
        }

        String name_symbol = "Method_" + name_method;
        symbolTable2.addSymbolMethod(name_symbol, name_method, type_return, type_return, list_parameter, "Method");
        symbolTable2.enterBlock(name_method, ctx.getStart().getLine());
        for (String param : list_parameter) {
            String paramName = param.split(" ")[1];
            String paramType = param.split(" ")[0];
            boolean isFieldDefined = symbolTable2.isTypeDefined(paramType);

            this.symbolTable2.addSymbolField(
                    "Field_".concat(paramName),
                    paramName,
                    paramType,
                    isFieldDefined,
                    "ParamField");
        }
    }

    @Override
    public void exitMethodDec(DustParser.MethodDecContext ctx) {
        System.out.println(indentation(tab--) + "}");
        this.symbolTable.exitScope();
        method_enter = false;
    }

    @Override
    public void enterConstructor(DustParser.ConstructorContext ctx) {

        // getting name of class
        String name_class = ctx.CLASSNAME().getText();
        // getting name of constructor
        String name_parent = this.symbolTable2.getCurrentNode().getName();

        System.out.println("\n" + indentation(++tab) + "class constructor: " + ctx.getChild(1) + "{");

        List<String> parameters = new ArrayList<>();
        for (DustParser.ParameterContext param : ctx.parameter()) {
            String paramName = param.getChild(0).getChild(1).getText();
            String paramType = param.getChild(0).getChild(0).getText();
            parameters.add(paramType + " " + paramName);
        }

        this.symbolTable2.addSymbolMethod("Constructor_".concat(name_class), name_class, name_class,
                "", parameters, "Constructor");


        this.symbolTable2.enterBlock(name_class, ctx.getStart().getLine());

        for (String param : parameters) {
            String paramName = param.split(" ")[1];
            String paramType = param.split(" ")[0];
            boolean isFieldDefined = symbolTable2.isTypeDefined(paramType);

            this.symbolTable2.addSymbolField("Field_".concat(paramName), paramName, paramType, isFieldDefined, "ParamField");
        }

        for (DustParser.StatementContext statementCtx : ctx.statement()) {

            // add fields defined in constructor to its scope
            if (statementCtx.varDec() != null) {
                String fieldName = statementCtx.varDec().getChild(1).getText();
                String fieldType = statementCtx.varDec().getChild(0).getText();

                boolean isFieldDefined = symbolTable2.isTypeDefined(fieldType);

                symbolTable2.addSymbolField("Field_".concat(fieldName), fieldName, fieldType, isFieldDefined, "MethodVar");
            }
            else if (statementCtx.arrayDec() != null) {
                String name = statementCtx.arrayDec().getChild(4).getText();
                String type = statementCtx.arrayDec().getChild(0).getText();

                boolean isFieldDefined = symbolTable2.isTypeDefined(type);

                symbolTable2.addSymbolField("Field_".concat(name), name, type, isFieldDefined, "ParamField");

            }
        }


        currentMethodName = ctx.CLASSNAME().getText() + " ";
        currentReturnType = "";

        this.symbolTable.addSymbolMethod(currentMethodName, currentReturnType, currPar, "Constructor");

        this.symbolTable.enterScope();
        this.symbolTable.addScope(currentMethodName, ctx.getStart().getLine());

//        System.out.println(indentation(++tab) + "class method: " + ctx.getChild(2) + "/ return type: " + ctx.getChild(1) + "{");

        for(DustParser.StatementContext statementContext : ctx.statement()){
            if(statementContext.arrayDec() != null){
                String fieldName = statementContext.arrayDec().getChild(4).getText();
                String fieldType = statementContext.arrayDec().getChild(0).getText();
                boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
                this.symbolTable.addSymbolField(fieldName, fieldType, isDefined, "MethodVar");
            }
            else if(statementContext.varDec() != null){
                String fieldName = statementContext.varDec().getChild(1).getText();
                String fieldType = statementContext.varDec().getChild(0).getText();
                boolean isDefined = this.symbolTable.checkIsDefined(fieldType);
                this.symbolTable.addSymbolField(fieldName, fieldType, isDefined, "MethodVar");
            }
        }

        int constructor_line = ctx.getStart().getLine();
        // check if constructor's name is equal to class name
        if (!name_parent.equals(name_class))
            System.out.println("constructor's name and class name are different at line: " + constructor_line);


    }

    @Override
    public void exitConstructor(DustParser.ConstructorContext ctx) {

        System.out.println(indentation(tab--) + "}");

        this.symbolTable.exitScope();
    }


    @Override
    public void enterParameter(DustParser.ParameterContext ctx) {
        tab++;
        int ParameterLength = ctx.getText().split(",").length;
        String FinalOutput;
        FinalOutput = concatVarTypePairs(ctx,ParameterLength);
        // add two brackets to each side of result
        FinalOutput = "[" + FinalOutput + "]";
        System.out.println(indentation(++tab) + FinalOutput);

        currPar = FinalOutput;

    }

    @Override
    public void exitParameter(DustParser.ParameterContext ctx) {
        --tab;
        this.symbolTable.exitScope();
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
        this.symbolTable.enterScope();
        this.symbolTable.addScope("if", ctx.getStart().getLine());
        this.symbolTable2.enterBlock("if", ctx.getStart().getLine());

    }

    @Override
    public void exitIf_statment(DustParser.If_statmentContext ctx) {
        this.symbolTable.exitScope();
        this.symbolTable2.exitBlock();
    }

    @Override
    public void enterWhile_statment(DustParser.While_statmentContext ctx) {
        this.symbolTable.enterScope();
        this.symbolTable.addScope("while", ctx.getStart().getLine());
        this.symbolTable2.enterBlock("while", ctx.getStart().getLine());

    }

    @Override
    public void exitWhile_statment(DustParser.While_statmentContext ctx) {
        this.symbolTable.exitScope();
        this.symbolTable2.exitBlock();
    }

    @Override
    public void enterIf_else_statment(DustParser.If_else_statmentContext ctx) {
        this.symbolTable.enterScope();
        this.symbolTable.addScope("if_else", ctx.getStart().getLine());
        this.symbolTable2.enterBlock("if_else", ctx.getStart().getLine());

    }

    @Override
    public void exitIf_else_statment(DustParser.If_else_statmentContext ctx) {
        this.symbolTable.exitScope();
        this.symbolTable2.exitBlock();

    }

    @Override
    public void enterPrint_statment(DustParser.Print_statmentContext ctx) {

    }

    @Override
    public void exitPrint_statment(DustParser.Print_statmentContext ctx) {

    }

    @Override
    public void enterFor_statment(DustParser.For_statmentContext ctx) {
        this.symbolTable.enterScope();
        this.symbolTable.addScope("for", ctx.getStart().getLine());
        this.symbolTable2.enterBlock("for", ctx.getStart().getLine());

    }

    @Override
    public void exitFor_statment(DustParser.For_statmentContext ctx) {
        this.symbolTable.exitScope();
        this.symbolTable2.exitBlock();

    }

    @Override
    public void enterMethod_call(DustParser.Method_callContext ctx) {


        int lineNumber = ctx.getStart().getLine();
        // get the method's name and search for it
        String name_method = ctx.ID(1).getText();
        SymbolTable2.SymbolNode method_in_table = this.symbolTable2.findNode(name_method, "Method_".concat(name_method));
        try {
            // if number of arguments do not much number of parameters
            int length_parameters = method_in_table.getParameterList().length;
            String[] method_parameters = ctx.args().getChild(1).getText().split(",");
            int user_argument_length = method_parameters.length;
            // comparison between number of arguments and parameters
            if (user_argument_length != length_parameters) {
                // printing error if numbers do not match
                System.out.println("more/less parameters needed at line: " + lineNumber);

            } else {
                // if any argument and parameter doesn't match the other
                // iterate on all arguments given by user
                for (int parameter = 0; parameter < length_parameters; parameter++) {
                    SymbolTable2.SymbolNode parameter_field = this.symbolTable2.getVariableType(method_parameters[parameter],
                            "Field_" + (method_parameters[parameter]));
                    // if the argument is not defined
                    if (parameter_field == null) {
                        System.out.println("at line: " + lineNumber + ", " + method_parameters[parameter] + " is not defined");
                    }

                    String type_parameter_field = parameter_field.getType();

                    String type_parameter_user = method_in_table.getParameterList()[parameter].split(", ")[1].replace("type: ", "").trim();
                    // check if the type of argument and parameter does not match
                    if (!type_parameter_field.equals(type_parameter_user))
                        System.out.println("method parameter doesn't match at line: " + lineNumber);

                }
            }
        }catch(NullPointerException ignored){

        }

    }

    @Override
    public void exitMethod_call(DustParser.Method_callContext ctx) {

    }

    @Override
    public void enterAssignment(DustParser.AssignmentContext ctx) {

        int lineNumber = ctx.getStart().getLine();

        tab++;

        System.out.println(indentation(tab) + ctx.getChild(0).getText() + " " + ctx.getChild(1).getText() + " " + ctx.getChild(2).getText());

        try {
            // varDec
            if (ctx.varDec() != null) {
                // left side of assignment
                var declare_variable = ctx.varDec();
                // right side of assignment
                String assign_value = ctx.exp().getText();
                // if the assigned value ends with ')' it means it's either a method or class
                if (assign_value.endsWith(")")) {
                    // extracting it's name
                    String assign_value_name = assign_value.substring(0, assign_value.indexOf('('));
                    // search for assigned method
                    SymbolTable2.SymbolNode assign_node = this.symbolTable2.findNode(assign_value_name, "Method_".concat(assign_value_name));
                    boolean check_class = declare_variable.TYPE().getText().equals(assign_value_name);
                    boolean check_method = assign_node != null;
                    // if there is no class or method defined, an undefined error should be printed
                    if (!check_method && !check_class) {
                        System.out.println("undefined variable, " + assign_value_name + " at line: " + lineNumber);
                    }

                } else {
                    // if the assigned value is a normal variable
                    // is it originally by dust
                    String assign_type = this.symbolTable2.getType(assign_value);
                    // or is it defined in a scope
                    SymbolTable2.SymbolNode field_type = this.symbolTable2.getVariableType(assign_value, "Field_".concat(assign_value));
                    // if neither, print an error for it
                    if (field_type == null && assign_type == null){
                        System.out.println("undefined variable, " + assign_value + " at line: " + lineNumber);
                    }
                }
            }  else {
                // array assignment
                try {
                    // left side of assignment
                    String assignment_left_side = ctx.prefixexp().getText();
                    // extract the array's name
                    int openingBracketIndex = assignment_left_side.indexOf('[');
                    String arrayName = openingBracketIndex != -1 ? assignment_left_side.substring(0, openingBracketIndex) : assignment_left_side;
                    // extract the index given in the array
                    String[] characters = assignment_left_side.split("\\[");
                    int index = Integer.parseInt(characters[1].replace("]", "").trim());

                    var assignment_right_side = ctx.exp();
                    String value = assignment_right_side.getText();

                    // search for assigned value to array
                    SymbolTable2.SymbolNode assigned_val = this.symbolTable2.getVariableType(value, "Field_" + value);

                    SymbolTable2.SymbolNode field_type = this.symbolTable2.getVariableType(arrayName, "Field_" + arrayName);

                    // if the variable is not defined print an error
                    if (field_type == null) {
                        System.out.println("undefined array, " + arrayName + " at line: " + lineNumber);
                    } else {
                        // check out of bound error
                        int arr_length = this.arr_size(field_type.getType());

                        if (index > arr_length){
                            System.out.println("out of bound array, " + arrayName + " at line: " + lineNumber);
                        }
                        else if(index < 0){
                            System.out.println("out of bound array, " + arrayName + " at line: " + lineNumber);
                        }
                        else if (assigned_val == null){
                            System.out.println("undefined variable, " + value + " at line: " + lineNumber);
                        }
                    }
                } catch (NullPointerException | IndexOutOfBoundsException ignored) { }
                // assignments without defining type
                try {
                    
                    var right_side_assign = ctx.exp();

                    String left_side_name = ctx.prefixexp().ID().getText();
                    String right_side_name = right_side_assign.prefixexp().getText();

                    boolean is_arr_assign = false;
                    if (right_side_name.contains("[") && right_side_name.contains("]")) {
                        // extract array name from right expression
                        right_side_name = ctx.exp().prefixexp().prefixexp().getText();
                        is_arr_assign = true;
                    }

                    else if (right_side_assign.prefixexp().prefixexp() != null) {
                        right_side_name = right_side_assign.prefixexp().ID().getText();
                    }

                    SymbolTable2.SymbolNode left_side_type = this.symbolTable2.getVariableType(left_side_name, "Field_".concat(left_side_name));
                    if (left_side_type == null) {
                        System.out.println("undefined variable, " + left_side_name + " at line: " + lineNumber);
                    }
                    SymbolTable2.SymbolNode right_side_type = this.symbolTable2.getVariableType(right_side_name, "Field_".concat(right_side_name));
                    if (is_arr_assign && right_side_type != null) {
                        int index = this.arr_size(ctx.exp().getText());
                        int size = this.arr_size(right_side_type.getType());
                        if (index > size || index < 0){
                            System.out.println("index out of bound, " + right_side_name + " at line: " + lineNumber);
                        }
                    }
                    SymbolTable2.SymbolNode right_side_method = this.symbolTable2.findNode(right_side_name, "Method_".concat(right_side_name));
                    if (right_side_type == null && right_side_method == null) {
                        System.out.println("undefined array, " + right_side_name + " at line: " + lineNumber);

                    }

                } catch (NullPointerException ignored) { }
            }
        } catch (NullPointerException ignored) {}

    }

    @Override
    public void exitAssignment(DustParser.AssignmentContext ctx) {
        --tab;
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
