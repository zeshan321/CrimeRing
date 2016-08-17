package script;

import javax.script.CompiledScript;

public class ScriptObject {

    public String id;
    public String dir;
    public CompiledScript script;

    public ScriptObject(String id, String dir, CompiledScript script) {
        this.id = id;
        this.dir = dir;
        this.script = script;
    }
}
