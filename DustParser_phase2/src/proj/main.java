package proj;

import gen.DustLexer;
import gen.DustParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Paths;

public class main {
    public static void main(String[] args) throws IOException{
        var path = Paths.get("src", "in.ds");
        var stream = CharStreams.fromFileName(path.toString());
        var lexer = new DustLexer(stream);
        var tokens = new CommonTokenStream(lexer);
        var parser = new DustParser(tokens);
        parser.setBuildParseTree(true);
        var tree = parser.program();
        var walker = new ParseTreeWalker();
        var listener = new ProgramPrinter();

        walker.walk(listener, tree);
    }
}
