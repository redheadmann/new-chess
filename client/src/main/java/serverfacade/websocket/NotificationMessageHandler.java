package serverfacade.websocket;

import com.google.gson.Gson;
import ui.GameplayClient;
import websocket.deserializers.MessageDeserializer;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import javax.websocket.MessageHandler;

public class NotificationMessageHandler implements MessageHandler.Whole<String> {
    private final GameplayClient client;
    private final Gson serializer = MessageDeserializer.createSerializer();

    public NotificationMessageHandler(GameplayClient client) {
        this.client = client;
    }

    @Override
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = serializer.fromJson(message, ServerMessage.class);
            client.notify(serverMessage);
        } catch (Exception ex) {
            client.notify(new ErrorMessage(ex.getMessage()));
        }
    }
}
