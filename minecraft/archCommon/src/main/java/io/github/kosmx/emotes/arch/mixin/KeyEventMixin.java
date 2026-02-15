package io.github.kosmx.emotes.arch.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft; // Import ตัวนี้เพิ่มมา
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = KeyboardHandler.class, priority = 1000)
public class KeyEventMixin {

    @Inject(method = "keyPress", at = @At(value = "HEAD"))
    private void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // action == 1 คือการกด (Press)
        if (action == 1) {
            // --- ส่วนที่แก้ไข: เช็คว่ามีหน้าจอ GUI เปิดอยู่หรือไม่ ---
            // ถ้า screen == null แปลว่าผู้เล่นกำลังเล่นเกมปกติ (ไม่ได้เปิด inventory/chat)
            if (Minecraft.getInstance().screen == null) {
                // ส่งสัญญาณปุ่มไปที่ Emote Mod เฉพาะตอนเล่นปกติ
                EmoteHolder.handleKeyPress(InputConstants.getKey(key, scancode));
            }
            // ---------------------------------------------------

            // "สำคัญ": ยังคงไม่ใส่ ci.cancel() เพื่อให้ TaCZ (ซึ่งอยู่ที่ TAIL)
            // หรือระบบอื่นๆ ของเกม ได้รับสัญญาณปุ่มนี้และทำงานต่อไปได้
        }
    }
}