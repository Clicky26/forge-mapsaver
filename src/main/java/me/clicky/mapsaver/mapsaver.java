package me.clicky.mapsaver;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.nio.file.Path;

@Mod(modid = mapsaver.MOD_ID, clientSideOnly = true)
public class mapsaver {
    public static final String MOD_ID = "mapsaver";
    public static Path modFolder;

    public static Boolean ExportAll;
    public static Integer MergeRadius;
    public static Integer IdThreshold;

    @Mod.Instance
    public mapsaver instance;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        initDirs();

        Configuration config = new Configuration(new File(Minecraft.getMinecraft().gameDir.toPath().resolve("mapsaver") + "/config.cfg"));
        config.load();

        ExportAll = config.get("Export Stream", "Export All", false).getBoolean();
        config.addCustomCategoryComment("Export Stream", "Set exportall to true to constantly load maps when rendered");
        config.addCustomCategoryComment("Threshold", "Set idthreshold to the maximum difference in map IDs you want to merge. Higher is less accurate");
        config.addCustomCategoryComment("Radius", "Change mergeradius to the radius you wan't to search around the target map for similar maps to merge");
        MergeRadius = config.get("Radius", "Merge Radius", 1).getInt();
        IdThreshold = config.get("Threshold", "MapID Threshold", 50).getInt();

        config.save();
    }

    @Mod.EventHandler
    public void init (FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(instance);
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