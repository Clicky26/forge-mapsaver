package me.clicky.mapsaver.mixin;

import me.clicky.mapsaver.function.ExportMap;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItemRenderer.class)
public class MixinMapRender {
    @Inject(method = "renderMap", at = @At(value = "HEAD"), cancellable = true)
    public void renderMap(MapData mapData,boolean noOverlayRendering, CallbackInfo ci) {
        ExportMap.ExportPNG(mapData);
        //ExportMap.ExportBin(mapData);
    }
}
