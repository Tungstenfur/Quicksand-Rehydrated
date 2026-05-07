package net.mokai.quicksandrehydrated.client.render.coverage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

public final class ArmorTextureResolver {

    private ArmorTextureResolver() {
    }

    public static ResourceLocation resolve(ArmorItem armorItem, EquipmentSlot slot) {
        boolean innerLayer = slot == EquipmentSlot.LEGS;
        String materialName = armorItem.getMaterial().getName();
        ResourceLocation materialId = new ResourceLocation(materialName);
        String texturePath = String.format(
            "textures/models/armor/%s_layer_%d.png",
            materialId.getPath(),
            innerLayer ? 2 : 1
        );
        return new ResourceLocation(materialId.getNamespace(), texturePath);
    }
}

