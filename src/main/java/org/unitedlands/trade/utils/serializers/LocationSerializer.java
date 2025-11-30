package org.unitedlands.trade.utils.serializers;

import org.bukkit.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LocationSerializer implements JsonSerializer<Location> {
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {

        var jsonLocation = new JsonObject();
        jsonLocation.addProperty("x", src.getX());
        jsonLocation.addProperty("y", src.getY());
        jsonLocation.addProperty("z", src.getZ());
        jsonLocation.addProperty("pitch", src.getPitch());
        jsonLocation.addProperty("yaw", src.getYaw());
        jsonLocation.addProperty("world", src.getWorld().getName());

        return jsonLocation;
    }
}
