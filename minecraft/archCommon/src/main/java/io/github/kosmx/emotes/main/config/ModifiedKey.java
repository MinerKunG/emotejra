package io.github.kosmx.emotes.main.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class ModifiedKey {
    private final InputConstants.Key key;
    private final int modifiers; // เก็บค่า Bitmask: 1=Shift, 2=Ctrl, 4=Alt

    public ModifiedKey(InputConstants.Key key, int modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    public InputConstants.Key getKey() { return key; }
    public int getModifiers() { return modifiers; }

    // เช็คว่าปุ่มที่กดตรงกับที่ตั้งไว้หรือไม่
    public boolean matches(InputConstants.Key inputKey, int inputModifiers) {
        // สนใจแค่ Ctrl, Shift, Alt (Bitmask 7)
        int requiredMods = modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_ALT);
        int activeMods = inputModifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_ALT);
        return key.equals(inputKey) && requiredMods == activeMods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModifiedKey that = (ModifiedKey) o;
        return modifiers == that.modifiers && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, modifiers);
    }

    // สร้างชื่อปุ่มที่จะโชว์ในเมนู เช่น "Ctrl + 1"
    public Component getDisplayName() {
        String prefix = "";
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) prefix += "Ctrl + ";
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) prefix += "Shift + ";
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) prefix += "Alt + ";

        return Component.literal(prefix).append(key.getDisplayName());
    }
}