package gen;
import java.util.*;

public class SymbolTable {
    private Map<String, Symbol> currentScope;
    private Deque<Map<String, Symbol>> scopeStack;
    private String dashes = "---------";
    private String endScope = "=============================================================================";


    public void Print_table(){

        Map<String, Symbol> methodScope = scopeStack.getFirst();
        scopeStack.removeFirst();
        while (!scopeStack.isEmpty()) {
            Map<String, Symbol> currScope = scopeStack.pop();

            List<String> outputList = new ArrayList<>();

            for (Map.Entry<String, Symbol> entry : currScope.entrySet()) {
                String key = entry.getKey();
                Symbol value = entry.getValue();

                if (value.getLine_number() != 0) {
                    String line = String.format("%s %s:%d %s", dashes, value.getName(), value.getLine_number(), dashes);
//                    outputList.add(line);
                    System.out.println(line);
                }


                if (value.getVal() != null && value.getVal().equals("Class")) {
                    String classVal = "Key: " + "Class_" + value.getName() + " | " + " Value: " +
                            value.getVal() + " (name: " + value.getName() + " )" + " (parent: " +
                            value.getParent() + ")";
                    outputList.add(classVal);

                }
                else if(value.getVal() != null && value.getVal().equals("ClassField")){
                    String fieldVal = "Key: " + "Field_" + value.getName() + " | " + " Value: " +
                            value.getVal() + " (name: " + value.getName() + " )" + " (type: " +
                            value.getType() + ", isDefined: " + value.getDefined() + ")";
                    outputList.add(fieldVal);

                }
                else if (value.getVal() != null && value.getVal().equals("MethodVar")){
                    String fieldVal = "Key: " + "Field_" + value.getName() + " | " + " Value: " +
                            value.getVal() + " (name: " + value.getName() + " )" + " (type: " +
                            value.getType() + ", isDefined: " + value.getDefined() + ")";
                    outputList.add(fieldVal);

                }

                else if (value.getVal() != null && value.getVal().equals("Method")) {
                    String prefix = "Method_";

                    String methodVal = "Key: " + prefix + value.getName() + " | " + " Value: " +
                            value.getVal() + " (name: " + value.getName() + " )" + " (return type: [" +
                            value.getReturn_type() + "])" + "(parameter list: " + value.getPar() + ")";
//                    System.out.println(methodVal);
                    outputList.add(methodVal);

                }

                else if (value.getVal() != null && value.getVal().contains("Constructor")) {
                    String prefix = "Constructor_";

                    String methodVal = "Key: " + prefix + value.getName() + " | " + " Value: " +
                            value.getVal() + " (name: " + value.getName() + " )" + " (return type: [" +
                            value.getReturn_type() + "])" + "(parameter list: " + value.getPar() + ")";


//                    System.out.println(methodVal);
                    for (Map.Entry<String, Symbol> entry1 : methodScope.entrySet()) {
                        String key1 = entry1.getKey();
                        Symbol value1 = entry1.getValue();

                        String prefix1 = "Method_";

                        String methodVal1 = "Key: " + prefix1 + value1.getName() + " | " + " Value: " +
                                value1.getVal() + " (name: " + value1.getName() + " )" + " (return type: [" +
                                value1.getReturn_type() + "])" + "(parameter list: " + value1.getPar() + ")";
//                    System.out.println(methodVal);
                        outputList.add(methodVal1);

                    }
                    outputList.add(methodVal);

                }

            }



            // Print the output strings in reverse order
            for (int i = outputList.size() - 1; i >= 0; i--) {
                System.out.println(outputList.get(i));
            }


        }


    }

    public SymbolTable() {
        currentScope = new HashMap<>();
        scopeStack = new ArrayDeque<>();
        scopeStack.add(currentScope);
    }

    public boolean checkIsDefined (String type) {
        String[] types = {"int", "string", "float", "bool"};

        for (String _type : types) {
            if (_type.equals(type)) return true;
        }
        return false;
    }

    public void enterScope() {

        Map<String, Symbol> newScope = new HashMap<>();
        scopeStack.add(newScope);
        currentScope = newScope;


    }

    public void exitScope() {
        currentScope = scopeStack.peek();
    }

    public void addSymbol(String name, String type) {
        currentScope.put(name, new Symbol(name, type));
    }

    public void addSymbolMethod(String name, String return_type, String par, String val){
        currentScope.put(name, new Symbol(name, val, return_type, par));
    }

    public void addSymbolField(String name, String type, Boolean isDefined, String val){
        currentScope.put(name, new Symbol(name, type, isDefined, val));
    }

    public void addSymbolClass(String name, String val, String parent){
        currentScope.put(name, new Symbol(name, val, parent));
    }

    public void addScope(String name, int line_number){
        currentScope.put(name, new Symbol(name, line_number));
    }

    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopeStack) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    public static class Symbol {

        private String name;
        private String type;
        private Boolean isDefined;
        private String val;
        private String par;
        private String return_type;
        private String parent;
        private int line_number = 0;

        public Symbol(String name, String type, Boolean isDefined, String val) {
            this.name = name;
            this.type = type;
            this.isDefined = isDefined;
            this.val = val;
        }

        public Symbol(String name, String val, String return_type, String par){
            this.name = name;
            this.val = val;
            this.return_type = return_type;
            this.par = par;
        }

        public Symbol(String name, String val, String parent){
            this.name = name;
            this.val = val;
            this.parent = parent;
        }

        public Symbol(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public Symbol(String name, int line_number){
            this.name = name;
            this.line_number = line_number;
        }

        public String getName() {
            return name;
        }

        public String getVal(){
            return val;
        }

        public String getPar() {
            return par;
        }

        public void setPar(String par) {
            this.par = par;
        }

        public String getReturn_type() {
            return return_type;
        }

        public String getParent() {
            return parent;
        }

        public Boolean getDefined() {
            return isDefined;
        }

        public int getLine_number() {
            return line_number;
        }

        public String getType() {
            return type;
        }
    }

}
