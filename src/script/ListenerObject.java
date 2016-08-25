package script;

import javax.script.ScriptEngine;

public class ListenerObject {

    public ScriptEngine engine;
    public String method;

    public ListenerObject(ScriptEngine engine, String method) {
        this.engine = engine;
        this.method = method;
    }
}
