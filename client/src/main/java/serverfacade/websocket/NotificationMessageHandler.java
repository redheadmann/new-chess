package serverfacade.websocket;

import com.google.gson.Gson;
import ui.GameplayClient;
import websocket.deserializers.MessageDeserializer;
import websocket.messages.ErrorMessage;
import websocket.messages.ServerMessage;

import javax.websocket.MessageHandler;

public class NotificationMessageHandler implements MessageHandler.Whole<String> {
    private final ServerMessageObserver observer;
    private final Gson serializer = MessageDeserializer.createSerializer();

    public NotificationMessageHandler(ServerMessageObserver observer) {
        this.observer = observer;
    }

    @Override
    public void onMessage(String message) {
        try {
            ServerMessage serverMessage = serializer.fromJson(message, ServerMessage.class);
            observer.notify(serverMessage);
        } catch (Exception ex) {
            observer.notify(new ErrorMessage(ex.getMessage()));
        }
    }
}
