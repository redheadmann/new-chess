package serverfacade.websocket;

import com.google.gson.Gson;
import repl.Repl;

import javax.websocket.MessageHandler;

public class NotificationMessageHandler implements MessageHandler.Whole<String> {
    private final Repl repl;
    private final Gson gson = new Gson();

    public NotificationMessageHandler(Repl repl) {
        this.repl = repl;
    }

    @Override
    public void onMessage(String s) {

    }
}
