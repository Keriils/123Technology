package com.newmaa.othtech.common.dimensions.sky;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.WorldProvider;

import com.newmaa.othtech.common.dimensions.worldProviderAntimonia;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class skyProviderHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onSkyRendererTick(TickEvent.ClientTickEvent event) {
        WorldClient world = FMLClientHandler.instance()
            .getWorldClient();
        if (world != null) {
            WorldProvider provider = world.provider;
            if (provider instanceof worldProviderAntimonia) {
                if (provider.getSkyRenderer() == null) {
                    provider.setSkyRenderer(new skyProviderAntimonia());
                }
            }
        }
    }
}
