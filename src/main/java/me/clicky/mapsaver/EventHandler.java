package me.clicky.mapsaver;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = mapsaver.MOD_ID)
public class EventHandler {

    public static boolean clicked = false;
    public static List<List> WantedMapsWithLocs = new ArrayList<>();
    public static boolean waiting = false;
    public static String name = "";

    public static BufferedImage concatImage;
    public static Graphics2D g2d;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onEvent(InputEvent.MouseInputEvent event) {

        if (Minecraft.getMinecraft().currentScreen != null) return;

        if (Mouse.isButtonDown(2)) {
            clicked = false;
            return;
        }

        if (!clicked) {
            clicked = true;

            //Get the thing I'm looking at
            final RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;

            //Is the thing an entity? We need an entity
            if (result.entityHit == null) return;

            //Does the entity have an item in it? If not then it's empty so won't contain a map
            if (result.entityHit.serializeNBT().hasKey("Item")) {

                //Grab the NBT for the item in the frame
                NBTTagCompound ItemData = result.entityHit.serializeNBT().getCompoundTag("Item");

                //For some reason mapIDs are stored as damage in their NBT??? wtf
                if (ItemData.hasKey("Damage")) {

                    //waiting = ItemData.getInteger("Damage");
                    waiting = true;

                    //Set output name
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    try {
                        name = ItemData.getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    } catch (Exception e) {}

                    //Replace illegal filename characters
                    name = name.replaceAll("[\\\\,\\/,:,?,\",<,>,|]", "");
                    name = ItemData.getInteger("Damage") + "_" + name + "_" + timestamp.getTime();

                    //Get facing vector
                    Vec3d FacingDirection = result.entityHit.getForward();

                    double tlx;
                    double tly;
                    double tlz;

                    double brx;
                    double bry;
                    double brz;

                    String IgnoreAxis;
                    boolean NeedsHFlip = false;

                    //Generate match-checking areas
                    if (FacingDirection.x < -0.9 || FacingDirection.x > 0.9) {
                        tlx = result.entityHit.posX;
                        tly = result.entityHit.posY + mapsaver.MergeRadius;
                        tlz = result.entityHit.posZ - mapsaver.MergeRadius;
                        brx = result.entityHit.posX;
                        bry = result.entityHit.posY - mapsaver.MergeRadius;
                        brz = result.entityHit.posZ + mapsaver.MergeRadius;

                        IgnoreAxis = "X";

                        if (FacingDirection.x > 0.9) NeedsHFlip = true;
                    } else {
                        tlx = result.entityHit.posX - mapsaver.MergeRadius;
                        tly = result.entityHit.posY + mapsaver.MergeRadius;
                        tlz = result.entityHit.posZ;
                        brx = result.entityHit.posX + mapsaver.MergeRadius;
                        bry = result.entityHit.posY - mapsaver.MergeRadius;
                        brz = result.entityHit.posZ;

                        IgnoreAxis = "Z";

                        if (FacingDirection.z < -0.9) NeedsHFlip = true;
                    }

                    //Find item frames within checking area
                    AxisAlignedBB scanAbove = new AxisAlignedBB(tlx, tly, tlz, brx, bry, brz);
                    List<EntityItemFrame> EntitiesInPosition = Minecraft.getMinecraft().player.world.getEntitiesWithinAABB(EntityItemFrame.class, scanAbove);

                    //If there's nothing in this area then abandon. This shouldn't happen so dunno why I added it :/
                    if (EntitiesInPosition.isEmpty()) return;

                    List<List> WantedMaps = new ArrayList<>();

                    //Loop through item frames found in area
                    for (EntityItemFrame EntityIter : EntitiesInPosition) {

                        //Make sure there's actually an item in the frame
                        if (EntityIter.serializeNBT().hasKey("Item")) {

                            //Get details on item in the frame
                            NBTTagCompound SubItemData = EntityIter.serializeNBT().getCompoundTag("Item");

                            //Make sure the item has a damage value. This is the map ID
                            if (SubItemData.hasKey("Damage")) {

                                //Make sure that the ID of the found map is within a range of the target map
                                if (Math.abs(SubItemData.getInteger("Damage") - ItemData.getInteger("Damage")) < mapsaver.IdThreshold) {

                                    //Sets the values in the array to the locations of the item frame
                                    //Ignore the axis that the maps are lying on
                                    List<? extends Number> ToAppend;
                                    Integer LocationY = (int) EntityIter.serverPosY / 4096;
                                    Integer LocationX;
                                    Integer LocationZ;
                                    if (IgnoreAxis == "Z") {
                                        LocationX = (int) EntityIter.serverPosX / 4096;
                                        ToAppend = Arrays.asList(LocationX, LocationY, SubItemData.getInteger("Damage"));
                                    } else {
                                        LocationZ = (int) EntityIter.serverPosZ / 4096;
                                        ToAppend = Arrays.asList(LocationZ, LocationY, SubItemData.getInteger("Damage"));
                                    }

                                    //Add the map to the list of wanted maps if it doesn't already exist
                                    if (!WantedMaps.contains(ToAppend)) WantedMaps.add(ToAppend);
                                }
                            }
                        }
                    }

                    //Set very max values to compare to. The locations should always be lower than 30mil
                    Integer LowestX = 29999999;
                    Integer LowestY = 29999999;

                    //Loop through each map in the array to find the lowest X and Y values. This will be the bottom left corner of the final map
                    for (List MapElement : WantedMaps) {
                        if ((int) MapElement.get(0) < LowestX) {
                            LowestX = (int) MapElement.get(0);
                        }
                        if ((int) MapElement.get(1) < LowestY) {
                            LowestY = (int) MapElement.get(1);
                        }
                    }

                    //Init a new 2D array for the maps. This will contain the centered grid of maps
                    List<List> CenteredPositions = new ArrayList<>();
                    Integer height = 0;
                    Integer width = 0;

                    //Loop through the current array of maps. At this point they will be int he format [actualX, actualY, mapID]
                    for (List MapElement : WantedMaps) {

                        //Init a temporary 1D array to save the map data to and eventually append to the final array
                        List<Integer> ToAppend = Arrays.asList(0, 0, 0, 0);

                        //Center the map positions so they start with a corner of 0,0:

                        //a few temp variables to make code easier to read
                        Integer MapPieceX = (int) MapElement.get(0);
                        Integer MapPieceY = (int) MapElement.get(1);

                        ToAppend.set(0, MapPieceX - LowestX);
                        ToAppend.set(1, MapPieceY - LowestY);

                        //Add the mapID because that's useful ofc
                        ToAppend.set(2, (int) MapElement.get(2));

                        //Get the total width and height of the map array
                        if (ToAppend.get(0) > width) width = ToAppend.get(0);
                        if (ToAppend.get(1) > height) height = ToAppend.get(1);

                        //Add the temporary 1D array to the final array
                        CenteredPositions.add(ToAppend);
                    }

                    //Loop through centeredPositions array and flip elements based on facing direction
                    for (List MapElement : CenteredPositions) {

                        //Flip the y's because PNG coordinates are dumb
                        MapElement.set(1, height - (int) MapElement.get(1));

                        //Flip horizontally if it needs it
                        if (NeedsHFlip) MapElement.set(0, width - (int) MapElement.get(0));
                    }

                    //Create a new buffered image with the size of the output image
                    concatImage = new BufferedImage((width + 1) * 128, (height + 1) * 128, BufferedImage.TYPE_INT_RGB);
                    g2d = concatImage.createGraphics();

                    //Set the public array to the one we just made so the maprender mixin can grab data from it
                    WantedMapsWithLocs = CenteredPositions;
                }
            }

        }



    }
}
