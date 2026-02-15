package io.github.kosmx.emotes.main.config;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.common.tools.BiMap;
import java.util.UUID;

public class ClientConfig extends SerializableConfig {

    public final BooleanConfigEntry dark = new BooleanConfigEntry("dark", false, false, basics);
    public final BooleanConfigEntry oldChooseWheel = new BooleanConfigEntry("oldChooseWheel", false, false, basics);
    public final ConfigEntry<Boolean> enablePerspective = new BooleanConfigEntry("perspective", true, false, basics);
    public final BooleanConfigEntry frontAsTPPerspective = new BooleanConfigEntry("default3rdPersonFront", false, false, basics);
    public final ConfigEntry<Boolean> showIcons = new BooleanConfigEntry("showicon", "showIcon", true, false, basics);
    public final ConfigEntry<Boolean> enableNSFW = new BooleanConfigEntry("enableNSFW", false, true, basics);
    public final ConfigEntry<Boolean> alwaysOpenEmoteScreen = new BooleanConfigEntry("alwaysOpenScreen", false, true, basics);

    // --- ส่วนที่เพิ่ม: ตัวแปรสำหรับระบบ Disable Emote ---
    public boolean disableEmotes = false;
    // ----------------------------------------------

    // แก้ไข: เปลี่ยนชื่อตัวแปรเป็น 'moddedFastMenuPages' เพื่อไม่ให้ Mixin ของม็อดอื่น (emote_extended) มาฉีดโค้ดทับจน Crash
    public final io.github.kosmx.emotes.common.SerializableConfig.IntConfig moddedFastMenuPages = new io.github.kosmx.emotes.common.SerializableConfig.IntConfig("fastMenuPages", 100, true, basics);

    public final ConfigEntry<Boolean> alwaysValidate = new BooleanConfigEntry("alwaysValidateEmote", false, true, expert);
    public final ConfigEntry<Boolean> enablePlayerSafety = new BooleanConfigEntry("playersafety", true, true, expert);
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry("stopthreshold", "stopThreshold", 0.04f, true, expert, "options.generic_value", -3.912f, 8f, 0f){
        @Override
        public double getConfigVal() { return Math.log(this.get()); }
        @Override
        public void setConfigVal(double newVal) { this.set((float) Math.exp(newVal)); }
    };
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry("yratio", "yRatio", 0.75f, true, expert, "options.percent_value", 0, 100, 1){
        @Override
        public double getConfigVal() { return this.get()*100f; }
        @Override
        public void setConfigVal(double newVal) { this.set((float) (newVal/100f)); }
        @Override
        public double getTextVal() { return this.getConfigVal(); }
    };
    public final ConfigEntry<Boolean> showHiddenConfig = new BooleanConfigEntry("showHiddenConfig", false, true, expert, false);
    public final ConfigEntry<Boolean> neverRemoveBadIcon = new BooleanConfigEntry("neverRemoveBadIcon", false, expert, true);
    public final ConfigEntry<Boolean> exportBuiltin = new BooleanConfigEntry("exportBuiltin", false, expert, true);

    public ClientConfig(){
        loadEmotesServerSide.set(false);
    }

    public BiMap<UUID, InputConstants.Key> emoteKeyMap = new BiMap<>();

    // ขยายขนาดเป็น 100 หน้า
    public UUID[][] fastMenuEmotes = new UUID[100][8];

    public final ConfigEntry<Boolean> hideWarningMessage = new BooleanConfigEntry("hideWarning", false, expert, true);
}