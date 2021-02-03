package me.clicky.mapsaver;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.nio.file.Path;

@Mod(modid = mapsaver.MOD_ID, clientSideOnly = true)
public class mapsaver {
    public static final String MOD_ID = "mapsaver";
    public static Path modFolder;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        initDirs();
    }

    private void initDirs() {
        modFolder = Minecraft.getMinecraft().gameDir.toPath().resolve("mapsaver");
        File directory = new File(String.valueOf(modFolder));
        if (!directory.exists())
            directory.mkdir();
        directory = new File(String.valueOf(modFolder.resolve("maps")));
        if (!directory.exists())
            directory.mkdir();
    }
}
