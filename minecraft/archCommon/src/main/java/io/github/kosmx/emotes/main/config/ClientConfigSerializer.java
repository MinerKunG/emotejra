package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.config.ConfigSerializer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;


public class ClientConfigSerializer extends ConfigSerializer {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ClientConfig config = (ClientConfig) super.deserialize(json, typeOfT, context);

        clientDeserialize(json.getAsJsonObject(), config);

        return config;
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = super.serialize(config, typeOfSrc, context).getAsJsonObject();

        if(config instanceof ClientConfig) clientSerialize((ClientConfig) config, node);

        return node;
    }

    @Override
    protected SerializableConfig newConfig() {
        return new ClientConfig();
    }

    private void clientDeserialize(JsonObject node, SerializableConfig sconfig) {
        ClientConfig config = (ClientConfig) sconfig;

        // --- ส่วนที่ 1: อ่านค่า moddedFastMenuPages จาก JSON ---
        if (node.has("fastMenuPages")) {
            config.moddedFastMenuPages.set(node.get("fastMenuPages").getAsInt());
        }

        EmoteFixer emoteFixer = new EmoteFixer(config.configVersion);
        if(node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config, emoteFixer);
        if(node.has("keys")) keyBindsDeserializer(node.get("keys"), config, emoteFixer);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config, EmoteFixer fixer){
        // แก้ไข: วนลูปตามจำนวนหน้าสูงสุดจาก Config (เช่น 100) แทนค่าคงที่ 10
        int pages = config.moddedFastMenuPages.get();
        for(int j = 0; j < pages; j++){
            if (node.has(Integer.toString(j))) {
                JsonElement subNode = node.get(Integer.toString(j));
                if (subNode.isJsonObject()) {
                    for (int i = 0; i != 8; i++) {
                        if (subNode.getAsJsonObject().has(Integer.toString(i))) {
                            config.fastMenuEmotes[j][i] = fixer.getEmoteID(subNode.getAsJsonObject().get(Integer.toString(i)));
                        }
                    }
                } else {
                    // รองรับไฟล์เซฟเก่า (Legacy)
                    config.fastMenuEmotes[0][j] = fixer.getEmoteID(subNode);
                }
            }
        }
    }

    private void keyBindsDeserializer(JsonElement node, ClientConfig config, EmoteFixer fixer){
        if(config.configVersion < 4){
            oldKeyBindsSerializer(node.getAsJsonArray(), config, fixer);
        } else {
            for (Map.Entry<String, JsonElement> element : node.getAsJsonObject().entrySet()) {
                String str = element.getValue().getAsString();
                config.emoteKeyMap.put(UUID.fromString(element.getKey()), InputConstants.getKey(str));
            }
        }
    }

    private void oldKeyBindsSerializer(JsonArray node, ClientConfig config, EmoteFixer fixer){
        for(JsonElement jsonElement : node){
            JsonObject n = jsonElement.getAsJsonObject();
            String str = n.get("key").getAsString();
            config.emoteKeyMap.add(new Pair<>(fixer.getEmoteID(n.get("id")), InputConstants.getKey(str)));
        }
    }

    private void clientSerialize(ClientConfig config, JsonObject node){
        // --- ส่วนที่ 2: เขียนค่า moddedFastMenuPages ลง JSON ---
        node.addProperty("fastMenuPages", config.moddedFastMenuPages.get());

        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        // แก้ไข: วนลูปเขียนข้อมูลตามจำนวนหน้าจริงที่มีใน Config
        int pages = config.moddedFastMenuPages.get();
        for(int j = 0; j < pages; j++) {
            if (j < config.fastMenuEmotes.length && config.fastMenuEmotes[j] != null) {
                JsonObject subNode = new JsonObject();
                boolean hasData = false;
                for (int i = 0; i != 8; i++) {
                    if (config.fastMenuEmotes[j][i] != null) {
                        subNode.addProperty(Integer.toString(i), config.fastMenuEmotes[j][i].toString());
                        hasData = true;
                    }
                }
                if (hasData) {
                    node.add(Integer.toString(j), subNode);
                }
            }
        }
        return node;
    }

    private JsonObject keyBindsSerializer(ClientConfig config){
        JsonObject array = new JsonObject();
        for(Pair<UUID, InputConstants.Key> emote : config.emoteKeyMap){
            array.addProperty(emote.getLeft().toString(), emote.getRight().getName());
        }
        return array;
    }
}