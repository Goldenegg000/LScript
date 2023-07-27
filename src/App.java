import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import org.goldenegg.LScript.*;
import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.Types.*;

public class App {

    private static class MyInter extends LSInterpreter {

        private LFunction print = new LFunction(arg -> {
            if (arg.get("val") instanceof LString)
                try {
                    var out = arg.get("val").toType(LString.class).getString();
                    if (out == null)
                        return new LNull();
                    out = out.replace("\\n", "\n");
                    out = out.replace("\\t", "\t");
                    out = out.replace("\\s", "\s");
                    System.out.println(out);
                } catch (LSError f) {
                    f.printStackTrace();
                }
            else
                System.out.println(arg.get("val"));
            return new LNull();
        }, new ArrayList<>(Arrays.asList(new String[] { "val" })));

        @Override
        public LSValue getImport(String moduleName) {
            if (moduleName.equals("std")) {
                var e = new LObject();
                e.children.put("print", print);
                // System.out.println(e.children);
                return e;
            }
            return null;
        }
    }

    public static void main(String[] args) throws LSError, IOException {
        var code = Files.readString(Path.of("test.lsc"));
        var inter = new MyInter();

        // inter.setGlobalVariable("print", );

        inter.compile(code);

        var main = inter.getGlobalVariable("setup");
        if (main != null)
            if (main.getType().getString() == "Function") {
                var funcArgs = new ArrayList<LSValue>();
                main.toType(LFunction.class).call(funcArgs, inter);
            }

        System.out.println(inter.globalVariablesToString());
    }
}
