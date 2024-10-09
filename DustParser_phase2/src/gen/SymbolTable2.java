package gen;
import java.util.*;

public class SymbolTable2 {
    private SymbolNode rootNode;
    private SymbolNode currentNode;
    private static final ArrayList<SymbolNode> SymbolList = new ArrayList<>();
    String dashes = "----------";
    String endScope = "=============================================================================";




    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isString(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    private boolean isBool(String value) {
        return value.equals("true") || value.equals("false");
    }

    private boolean isClass(String value) {
        // Check if value matches your class name rules
        return value.matches("^[A-Z][a-zA-Z0-9_]*$");
    }

    public String getType(String value) {
        if (isInteger(value)) {
            return "int";
        } else if (isFloat(value)) {
            return "float";
        } else if (isString(value)) {
            return "String";
        } else if (isBool(value)) {
            return "bool";
        } else if (isClass(value)) {
            return "class";
        }

        return null; // Unknown type
    }



    public SymbolTable2() {
        rootNode = new SymbolNode(null, null, null);
        currentNode = rootNode;
    }

    public SymbolNode getCurrentNode() {
        return this.currentNode;
    }

    public boolean isTypeDefined (String type) {
        String[] types = {"int", "bool", "String", "float", "class"};

        for (String _type : types) {
            if (type.equalsIgnoreCase(_type)) return true;
        }
        return false;
    }

    public void addSymbolClass(String SymbolName, String name,  String parent, String val) {
        currentNode.addChild(new SymbolNode(SymbolName, name, parent, val, currentNode));
    }

    public void addSymbolField(String SymbolName, String name, String type, Boolean isDefined, String val) {
        currentNode.addChild(new SymbolNode(SymbolName, name, type, isDefined, val, currentNode));
    }

    public void addSymbolMethod(String symbolName, String name, String type, String returnType, List<String> parameterList, String val) {
        currentNode.addChild(new SymbolNode(symbolName, name, type, returnType, parameterList , val, currentNode));
    }

    public boolean containsSymbol(String name, String symbolName) {
        return findNode(name, symbolName) != null;
    }


    public String getSymbolType(String name, String symbolName) {
        SymbolNode node = findNode(name, symbolName);
        return node != null ? node.getType() : null;
    }

    public void enterBlock(String name, int lineNumber) {
        currentNode = currentNode.addChild(new SymbolNode(name, lineNumber, currentNode));
    }


    public void exitBlock() {
        currentNode = currentNode.getParent();
    }

    public void printSymbolTable() {
        SymbolList.add(rootNode);
        printNode();
    }
    private void printMethod (SymbolNode node) {
        String value = String.format("Key: %s | Value: %s (name: %s) (return type: [%s]) (parameter list: %s)",
                node.getSymbolName(),
                node.getVal(),
                node.getName(),
                !node.getReturnType().equals("void") ? node.getReturnType() : "",
                Arrays.toString(node.getParameterList()));
        System.out.println(value);
    }
    private void printField(SymbolNode node) {
        String value = String.format("Key: %s | Value: %s (name: %s) (type: %s, isDefined: %s)",
                node.getSymbolName(),
                node.getVal(),
                node.getName(),
                node.getType(),
                node.getDefined());

        System.out.println(value);
    }
    private void printClass(SymbolNode node) {
        String value = String.format("Key: %s | Value: %s (name: %s) (parent: %s)",
                node.getSymbolName(),
                node.getVal(),
                node.getName(),
                node.getParentName());

        System.out.println(value);
    }
    private void printNode() {
        while (!SymbolList.isEmpty()) {
            SymbolNode node = SymbolList.get(0);

            if (node.getLineNumber() != 0) {
                System.out.printf(("%s %s:%d %s%n"), dashes, node.getName(), node.getLineNumber(), dashes);
            }

            for (SymbolNode child : node.getChildren()) {

                if (child.getSymbolName() != null) {
                    if (child.getSymbolName().startsWith("Method_") || child.getSymbolName().startsWith("Constructor_")) {
                        printMethod(child);
                    } else if (child.getSymbolName().startsWith("Field_")) {
                        printField(child);
                    } else if (child.getSymbolName().startsWith("Class_")) {
                        printClass(child);
                    }
                }

                if (child.getChildren().size() > 0 || child.getLineNumber() != 0) {
                    SymbolList.add(child);
                }
            }

            System.out.println(endScope);

            SymbolList.remove(0);
        }
    }
    public SymbolNode findNodeOneDepth(String name, String symbolName) {
        for (SymbolNode child : this.currentNode.getChildren()) {
            if (child.getName().equals(name) && child.getSymbolName().equals(symbolName)) {
                return child;
            }
        }
        return null;
    }
    private SymbolNode findNodeDFS(SymbolNode node, String name, String symbolName) {
        // Check if the current node matches the target name and symbolName
        if (node.getName() != null && node.getName().equals(name) &&
                node.getSymbolName() != null && node.getSymbolName().equals(symbolName)) {
            return node;
        }

        // Recursively search all children of the current node
        for (SymbolNode child : node.getChildren()) {
            SymbolNode result = findNodeDFS(child, name, symbolName);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
    public SymbolNode findNode(String name, String symbolName) {
        return findNodeDFS(rootNode, name, symbolName);
    }

    public SymbolNode getVariableType(String name, String symbolName) {
        return findVariableNode(name, symbolName);
    }
    private SymbolNode findVariableNode(String name, String symbolName) {

        SymbolNode currentNode = getCurrentNode();

        while (currentNode != null) {
            SymbolNode variableNode = findVariableNodeInScope(name, symbolName, currentNode);

            if (variableNode != null) {
                return variableNode;
            }
            currentNode = currentNode.getParent();
        }
        return null;
    }
    private SymbolNode findVariableNodeInScope(String name, String symbolName, SymbolNode scopeNode) {
        for (SymbolNode child : scopeNode.getChildren()) {
            if (child.getName().equals(name) && child.getSymbolName().equals(symbolName)) {
                return child;
            }
        }
        return null;
    }

    public static class SymbolNode {
        private String name;
        private String type;
        private Boolean isDefined;
        private String returnType;
        private String[] parameterList;
        private String val;
        private int lineNumber = 0;
        private String symbolName;
        private String parentName;
        private SymbolNode parent;
        private List<SymbolNode> children;

        public SymbolNode(String name, int lineNumber, SymbolNode parent) {
            this.name = name;
            this.lineNumber = lineNumber;
            this.parent = parent;
            children = new ArrayList<>();
        }
        public SymbolNode (String name, String type, SymbolNode parent) {
            this.name = name;
            this.type = type;
            this.parent = parent;
            children = new ArrayList<>();
        }
        public SymbolNode(String symbolName, String name, String type, Boolean isDefined, String val, SymbolNode parent) {
            this.name = name;
            this.type = type;
            this.isDefined = isDefined;
            this.val = val;
            this.symbolName = symbolName;
            this.parent = parent;
            children = new ArrayList<>();
        }
        public SymbolNode(String symbolName, String name, String type, String returnType, List<String> parameterList, String val, SymbolNode parent) {
            this.name = name;
            this.type = type;
            this.returnType = returnType;
            this.parameterList = parameterList.toArray(new String[0]);
            this.val = val;
            this.symbolName = symbolName;
            this.parent = parent;
            children = new ArrayList<>();
        }
        public SymbolNode(String symbolName, String name, String parentName, String val, SymbolNode parent) {
            this.name = name;
            this.symbolName = symbolName;
            this.parentName = parentName;
            this.val = val;
            this.parent = parent;
            children = new ArrayList<>();
        }
        public String getSymbolName() {
            return symbolName;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public SymbolNode getParent() {
            return parent;
        }

        public List<SymbolNode> getChildren() {
            return children;
        }

        public Boolean getDefined() {
            return isDefined;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getReturnType() {
            return returnType;
        }

        public String getVal() {
            return val;
        }

        public String[] getParameterList() {
            String[] parameters = new String[this.parameterList.length];

            for (int i = 0; i < this.parameterList.length; i++) {
                String paramType = this.parameterList[i].split(" ")[0];
                String paramName = this.parameterList[i].split(" ")[1];
                parameters[i] = String.format("[name: %s, type: %s, index: %d]", paramName, paramType, i + 1);
            }
            return parameters;
        }
        public String getParentName() {
            return parentName;
        }

        public SymbolNode addChild(SymbolNode child) {
            children.add(child);
            return child;
        }
    }

}
