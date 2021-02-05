package me.clicky.mapsaver.function;

import me.clicky.mapsaver.mapsaver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.storage.MapData;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static me.clicky.mapsaver.function.ColourProfile.IdMap;

public class ExportMap {

    public static void ExportPNG(MapData mapData) {

        byte[] MapImage = mapData.colors;
        String MapName = mapData.mapName;

        String connection;

        try {
            connection = Minecraft.getMinecraft().getCurrentServerData().serverIP;
        } catch (NullPointerException e) {
            connection = "singleplayer";
        }

        //Assume map shape square
        Integer MapSize = (int) Math.sqrt(MapImage.length);

        new File(mapsaver.modFolder + "/maps/" + connection).mkdirs();
        File target = new File(mapsaver.modFolder + "/maps/" + connection + "/" + MapName + ".png");
        if(!target.exists()) {
            BufferedImage image = new BufferedImage(MapSize, MapSize, BufferedImage.TYPE_INT_ARGB);
            for ( Integer x = 0; x < MapSize; x++ ) {
                for ( Integer y = 0; y < MapSize; y++ ) {
                    Integer ColourID = (int) MapImage[ x + (y * MapSize)];
                    if (ColourID < 0)
                        ColourID += 256;
                    image.setRGB(x, y, IdMap(String.valueOf(ColourID)));
                }
            }


            try {
                ImageIO.write(image, "png", target);
                System.out.println("Map png saved on ID " + MapName);
            } catch (IOException e) {
                System.out.println("Save failed on map png " + MapName);
            }
        }
    }

    public static void ExportBin (MapData mapData) {

        byte[] MapImage = mapData.colors;
        String MapName = mapData.mapName;

        File target = new File(mapsaver.modFolder + "/maps/" + MapName + ".bin");
        if(!target.exists()) {
            try {
                FileUtils.writeByteArrayToFile(target, MapImage);
                System.out.println("Map binary saved on ID " + MapName);
            } catch (IOException e) {
                System.out.println("Save failed on map binary " + MapName);
            }
        }
    }
}
