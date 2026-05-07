package net.mokai.quicksandrehydrated.client.render.coverage;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.entity.coverage.CoverageEntry;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;

public final class CoverageTextureUtils {

    private CoverageTextureUtils() {
    }

    public static TextureAtlasSprite[] buildCoverageByPixel(PlayerCoverage coverage) {
        TextureAtlasSprite[] coverageByPixel = new TextureAtlasSprite[32];

        for (CoverageEntry entry : coverage.coverageEntries) {
            for (int i = entry.begin; i <= entry.end; i++) {
                try {
                    coverageByPixel[i] = CoverageAtlasHolder.singleton.get(entry.texture);
                } catch (Exception e) {
                    // Skip invalid texture references.
                }
            }
        }

        return coverageByPixel;
    }

    public static TextureAtlasSprite getDepthMask(boolean slimArms) {
        ResourceLocation mask = new ResourceLocation(
            QuicksandRehydrated.MOD_ID,
            slimArms ? "coverage_mask_thin_arms" : "coverage_mask"
        );
        return CoverageAtlasHolder.singleton.get(mask);
    }

    public static TextureAtlasSprite getArmorDepthMask(EquipmentSlot slot) {
        String slotName = slot.getName();
        ResourceLocation armorMask = new ResourceLocation(
            QuicksandRehydrated.MOD_ID,
            "coverage_mask_armor_" + slotName
        );
        if (!resourceExists(armorMask)) {
            return getDepthMask(false);
        }
        return CoverageAtlasHolder.singleton.get(armorMask);
    }

    private static boolean resourceExists(ResourceLocation spriteId) {
        ResourceLocation textureId = new ResourceLocation(
            spriteId.getNamespace(),
            "textures/coverages/" + spriteId.getPath() + ".png"
        );
        return Minecraft.getInstance().getResourceManager().getResource(textureId).isPresent();
    }

    public static TextureAtlasSprite pickTopCoverageSprite(PlayerCoverage coverage) {
        TextureAtlasSprite[] coverageByPixel = buildCoverageByPixel(coverage);
        for (int i = coverageByPixel.length - 1; i >= 0; i--) {
            if (coverageByPixel[i] != null) {
                return coverageByPixel[i];
            }
        }
        return null;
    }
}

