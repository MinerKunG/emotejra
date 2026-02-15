package io.github.kosmx.emotes.main;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import dev.kosmx.playerAnim.core.util.Vec3d;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayer;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;

public abstract class EmoteHolder implements Supplier<UUID> {

    public final KeyframeAnimation emote;
    public final Component name;
    public final Component description;
    public final Component author;

    public AtomicInteger hash = null;
    public static UUIDMap<EmoteHolder> list = new UUIDMap<>();
    @Nullable
    public DynamicTexture nativeIcon = null;
    @Nullable
    private ResourceLocation iconIdentifier = null;

    @Nullable
    public INetworkInstance fromInstance = null;

    public EmoteHolder(KeyframeAnimation emote) {
        this.emote = emote;
        this.name = PlatformTools.fromJson(emote.extraData.get("name"));
        this.description = PlatformTools.fromJson(emote.extraData.get("description"));
        this.author = PlatformTools.fromJson(emote.extraData.get("author"));
    }

    public static void clearEmotes(){
        list.removeIf(emoteHolder -> {
            if(emoteHolder.fromInstance != null){
                return false;
            }
            if(emoteHolder.iconIdentifier != null){
                Minecraft.getInstance().getTextureManager().release(emoteHolder.iconIdentifier);
                assert emoteHolder.nativeIcon != null;
                emoteHolder.nativeIcon.close();
            }
            return true;
        });
    }

    public ResourceLocation getIconIdentifier(){
        if(iconIdentifier == null && this.emote.extraData.containsKey("iconData")){
            try {
                InputStream stream = new ByteArrayInputStream(Objects.requireNonNull(AbstractNetworkInstance.safeGetBytesFromBuffer((ByteBuffer) this.emote.extraData.get("iconData"))));
                assignIcon(stream);
                stream.close();
            }catch (IOException | NullPointerException e){
                e.printStackTrace();
                if(!((ClientConfig)EmoteInstance.config).neverRemoveBadIcon.get()){
                    this.emote.extraData.remove("iconData");
                }
            }
        }
        return iconIdentifier;
    }

    public void assignIcon(InputStream inputStream) {
        try {
            DynamicTexture nativeImageBackedTexture = new DynamicTexture(NativeImage.read(inputStream));
            this.iconIdentifier = PlatformTools.newIdentifier("icon" + this.hashCode());
            Minecraft.getInstance().getTextureManager().register(this.iconIdentifier, nativeImageBackedTexture);
            this.nativeIcon = nativeImageBackedTexture;
        } catch (Throwable var) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't open emote icon: " + var);
            this.iconIdentifier = null;
            this.nativeIcon = null;
        }
    }

    public KeyframeAnimation getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromUuid(UUID uuid){
        return list.get(uuid);
    }

    // แก้ไขจุดที่เป็น Error: ใช้ Anonymous Class เพื่อสร้าง Instance ของ Abstract Class
    public static void addEmoteToList(Iterable<KeyframeAnimation> emotes){
        for(KeyframeAnimation emote : emotes){
            EmoteHolder.list.add(new EmoteHolder(emote) {
                @Override
                public KeyframeAnimation getEmote() {
                    return this.emote;
                }
            });
        }
    }

    // แก้ไขจุดที่เป็น Error: ใช้ Anonymous Class
    public static EmoteHolder addEmoteToList(KeyframeAnimation emote){
        EmoteHolder newEmote = new EmoteHolder(emote) {
            @Override
            public KeyframeAnimation getEmote() {
                return this.emote;
            }
        };
        EmoteHolder old = newEmote.findIfPresent();
        if(old == null){
            list.add(newEmote);
            return newEmote;
        }
        else {
            return old;
        }
    }

    EmoteHolder findIfPresent()
    {
        if (list.contains(this)) {
            for (EmoteHolder obj : list) {
                if (obj.equals(this))
                    return obj;
            }
        }
        return null;
    }

    public static boolean playEmote(KeyframeAnimation emote, IEmotePlayerEntity player){
        return playEmote(emote, player, null);
    }

    public static boolean playEmote(KeyframeAnimation emote, IEmotePlayerEntity player, @Nullable EmoteHolder emoteHolder){
        if(canPlayEmote(player)){
            return ClientEmotePlay.clientStartLocalEmote(emote);
        }else{
            return false;
        }
    }

    private static boolean canPlayEmote(IEmotePlayerEntity entity){
        if(! canRunEmote(entity)) return false;
        if(!entity.isMainPlayer()) return false;
        return ! (IEmotePlayer.isRunningEmote(entity.emotecraft$getEmote()) && ! entity.emotecraft$getEmote().isLoopStarted());
    }

    public static boolean canRunEmote(IEmotePlayerEntity player){
        if(! TmpGetters.getClientMethods().isAbstractClientEntity(player)) return false;
        if(player.emotecraft$isNotStanding() && !ClientPacketManager.isRemoteTracking()) return false;
        Vec3d prevPos = player.emotecraft$getPrevPos();
        return ! (player.emotecraft$emotesGetPos().distanceTo(new Vec3d(prevPos.getX(), MathHelper.lerp(((ClientConfig)EmoteInstance.config).yRatio.get(), prevPos.getY(), player.emotecraft$emotesGetPos().getY()), prevPos.getZ())) > ((ClientConfig)EmoteInstance.config).stopThreshold.get());
    }

    public boolean playEmote(IEmotePlayerEntity playerEntity){
        return playEmote(this.emote, playerEntity, this);
    }

    @Override
    public int hashCode() {
        if(hash == null)
            hash = new AtomicInteger(this.emote.hashCode());
        return hash.get();
    }

    public UUID getUuid(){
        return this.emote.getUuid();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EmoteHolder){
            return (this.emote.equals(((EmoteHolder)o).emote));
        }
        return false;
    }

    @Override
    public UUID get() {
        return this.emote.get();
    }

    // เมธอดที่คืนค่า boolean เพื่อใช้กับ Mixin (Priority สูงสุด)
    public static boolean handleKeyPress(InputConstants.Key key){
        if(EmoteInstance.instance != null && EmoteHolder.canRunEmote(TmpGetters.getClientMethods().getMainPlayer())){
            UUID uuid = ((ClientConfig)EmoteInstance.config).emoteKeyMap.getL(key);
            if(uuid != null){
                EmoteHolder emoteHolder = list.get(uuid);
                if(emoteHolder != null) {
                    ClientEmotePlay.clientStartLocalEmote(emoteHolder);
                    return true;
                }
            }
        }
        return false;
    }

    public static EmoteHolder getNonNull(@Nonnull UUID emote){
        EmoteHolder emoteHolder = list.get(emote);
        if(emoteHolder == null)return new Empty(emote);
        return emoteHolder;
    }

    public static class Empty extends EmoteHolder{
        public Empty(UUID uuid) {
            super(new KeyframeAnimation.AnimationBuilder(AnimationFormat.UNKNOWN).setName("{\"color\":\"red\",\"text\":\"INVALID\"}").setUuid(uuid).build());
        }
    }
}