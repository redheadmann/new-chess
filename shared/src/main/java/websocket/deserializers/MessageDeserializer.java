package websocket.deserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class MessageDeserializer {

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ServerMessage.class,
                (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
                    if (el.isJsonObject()) {
                        String typeString =
                                el.getAsJsonObject().get("serverMessageType").getAsString();
                        ServerMessage.ServerMessageType serverMessageType =
                                ServerMessage.ServerMessageType.valueOf(typeString);
                        return switch (serverMessageType) {
                            case LOAD_GAME -> ctx.deserialize(el, LoadGameMessage.class);
                            case NOTIFICATION -> ctx.deserialize(el, NotificationMessage.class);
                            case ERROR -> ctx.deserialize(el, ErrorMessage.class);
                            default -> throw new JsonParseException("Error parsing ServerMessage");
                        };
                    } else {
                        return null;
                    }
                });

        return gsonBuilder.create();
    }

}
