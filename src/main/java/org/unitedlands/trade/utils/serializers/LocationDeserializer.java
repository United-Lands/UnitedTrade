package org.unitedlands.trade.utils.serializers;

import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

public class LocationDeserializer implements JsonDeserializer<Location> {
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        var jsonObj = json.getAsJsonObject();

        var x = jsonObj.get("x").getAsDouble();
        var y = jsonObj.get("y").getAsDouble();
        var z = jsonObj.get("z").getAsDouble();
        var pitch = jsonObj.get("pitch").getAsFloat();
        var yaw = jsonObj.get("yaw").getAsFloat();
        var worldName = jsonObj.get("world").getAsString();

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
