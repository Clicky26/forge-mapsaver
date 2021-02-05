package me.clicky.mapsaver.function;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

public class ChatUtil {

    public final static Minecraft mc = Minecraft.getMinecraft();

    public static ChatFormatting g = ChatFormatting.GOLD;
    public static ChatFormatting b = ChatFormatting.BLUE;
    public static ChatFormatting a = ChatFormatting.DARK_AQUA;
    public static ChatFormatting r = ChatFormatting.RESET;

    public static String opener = a + "Map Saver" + ChatFormatting.GRAY + " > " + r;

    public static void client_message_simple(String message) {
        if (mc.player != null) {
            message = opener + message;
            final ITextComponent itc = new TextComponentString(message).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("mapsaver"))));
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(itc, 5936);
        }
    }
}
