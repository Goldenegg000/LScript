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

        @Override
        public LSValue getImport(String moduleName) {
            if (moduleName.equals("std")) {
                return this.std;
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
                funcArgs.add(new LString("POGGERS"));
                main.toType(LFunction.class).call(funcArgs, inter);
            }

        System.out.println(inter.globalVariablesToString());
    }
}
