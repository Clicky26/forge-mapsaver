package me.clicky.mapsaver.mixin;

import me.clicky.mapsaver.EventHandler;
import me.clicky.mapsaver.function.ChatUtil;
import me.clicky.mapsaver.function.ExportMap;
import me.clicky.mapsaver.mapsaver;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.datatransfer.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static me.clicky.mapsaver.function.ColourProfile.IdMap;

@Mixin(MapItemRenderer.class)
public class MixinMapRender implements ClipboardOwner {
    @Inject(method = "renderMap", at = @At(value = "HEAD"), cancellable = true)
    public void renderMap(MapData mapData,boolean noOverlayRendering, CallbackInfo ci) {
        if (mapsaver.ExportAll) ExportMap.ExportPNG(mapData);

        if (EventHandler.waiting) {
            boolean MoreNeeded = false;
            for (List WantedMap : EventHandler.WantedMapsWithLocs) {
                if ((int) WantedMap.get(3) == 0) MoreNeeded = true;
                if (Integer.parseInt(mapData.mapName.replace("map_", "")) == (int) WantedMap.get(2) && (int) WantedMap.get(3) == 0) {
                    WantedMap.set(3, 1);
                    BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                    for (Integer x = 0; x < 128; x++) {
                        for (Integer y = 0; y < 128; y++) {
                            Integer ColourID = (int) mapData.colors[x + (y * 128)];
                            if (ColourID < 0)
                                ColourID += 256;
                            image.setRGB(x, y, IdMap(String.valueOf(ColourID)));
                        }
                    }
                    EventHandler.g2d.drawImage(image, (int) WantedMap.get(0) * 128, (int) WantedMap.get(1) * 128, null);
                }
            }
            if (!MoreNeeded) {
                EventHandler.waiting = false;
                File target = new File(mapsaver.modFolder + "/maps/" + EventHandler.name + ".png");
                try {
                    EventHandler.g2d.dispose();
                    ImageIO.write(EventHandler.concatImage, "png", target);
                    ChatUtil.client_message_simple("Map exported to " + ChatUtil.b + EventHandler.name + ".png");

                    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                    c.setContents(new TransferableImage(EventHandler.concatImage), this);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void lostOwnership( Clipboard clip, Transferable trans ) {
        System.out.println( "Lost Clipboard Ownership" );
    }


    private class TransferableImage implements Transferable {
        Image i;

        public TransferableImage( Image i ) {
            this.i = i;
        }

        public Object getTransferData( DataFlavor flavor )
                throws UnsupportedFlavorException, IOException {
            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for ( int i = 0; i < flavors.length; i++ ) {
                if ( flavor.equals( flavors[ i ] ) ) {
                    return true;
                }
            }

            return false;
        }

    }
}