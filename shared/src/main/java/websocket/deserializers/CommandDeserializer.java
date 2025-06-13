package websocket.deserializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.TypeAdapter;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

public class CommandDeserializer {

    public static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();


        TypeAdapter<UserGameCommand> defaultAdapter = gsonBuilder.create().getAdapter(UserGameCommand.class);

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
                (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                    if (el.isJsonObject()) {
                        String commandString =
                                el.getAsJsonObject().get("commandType").getAsString();
                        UserGameCommand.CommandType commandType =
                                UserGameCommand.CommandType.valueOf(commandString);
                        return switch (commandType) {
                            case CONNECT, LEAVE, RESIGN -> defaultAdapter.fromJsonTree(el);
                            case MAKE_MOVE -> ctx.deserialize(el, MakeMoveCommand.class);
                            default -> null;
                        };
                    } else {
                        return null;
                    }
                });

        return gsonBuilder.create();
    }


}
