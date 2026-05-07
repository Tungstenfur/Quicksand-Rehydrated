package net.mokai.quicksandrehydrated.client.render.coverage;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public final class ArmorCoverageTextureCache {

    private static final int TEXTURE_SIZE = 64;
    private static final Map<LivingEntity, CacheEntry> CACHE = new WeakHashMap<>();
    private static final Map<ResourceLocation, NativeImage> ARMOR_TEXTURE_CACHE = new WeakHashMap<>();

    private ArmorCoverageTextureCache() {
    }

    public static ResourceLocation getCoverageTexture(LivingEntity entity,
                                                      ResourceLocation armorTexture,
                                                      TextureAtlasSprite[] coverageByPixel,
                                                      TextureAtlasSprite depthMask,
                                                      boolean innerLayer,
                                                      int coverageGeneration) {
        CacheEntry entry = CACHE.computeIfAbsent(entity, key -> new CacheEntry(entity));
        return entry.getOrUpdate(armorTexture, coverageByPixel, depthMask, innerLayer, coverageGeneration);
    }

    private static NativeImage loadArmorTexture(ResourceLocation armorTexture) {
        if (ARMOR_TEXTURE_CACHE.containsKey(armorTexture)) {
            return ARMOR_TEXTURE_CACHE.get(armorTexture);
        }
        try (InputStream stream = Minecraft.getInstance().getResourceManager()
            .getResource(armorTexture)
            .map(resource -> {
                try {
                    return resource.open();
                } catch (IOException e) {
                    return null;
                }
            })
            .orElse(null)) {
            if (stream == null) {
                ARMOR_TEXTURE_CACHE.put(armorTexture, null);
                return null;
            }
            NativeImage image = NativeImage.read(stream);
            ARMOR_TEXTURE_CACHE.put(armorTexture, image);
            return image;
        } catch (IOException e) {
            ARMOR_TEXTURE_CACHE.put(armorTexture, null);
            return null;
        }
    }

    private static final class CacheEntry {
        private final DynamicTexture outerTexture;
        private final DynamicTexture innerTexture;
        private final ResourceLocation outerLocation;
        private final ResourceLocation innerLocation;
        private ResourceLocation lastOuterArmorTexture;
        private ResourceLocation lastInnerArmorTexture;
        private TextureAtlasSprite lastOuterMask;
        private TextureAtlasSprite lastInnerMask;
        private int lastOuterGeneration = Integer.MIN_VALUE;
        private int lastInnerGeneration = Integer.MIN_VALUE;

        private CacheEntry(LivingEntity entity) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            this.outerTexture = new DynamicTexture(TEXTURE_SIZE, TEXTURE_SIZE, true);
            this.innerTexture = new DynamicTexture(TEXTURE_SIZE, TEXTURE_SIZE, true);
            this.outerLocation = textureManager.register(
                "qsrehydrated/armor_coverage_" + entity.getUUID() + "_outer",
                this.outerTexture
            );
            this.innerLocation = textureManager.register(
                "qsrehydrated/armor_coverage_" + entity.getUUID() + "_inner",
                this.innerTexture
            );
        }

        private ResourceLocation getOrUpdate(ResourceLocation armorTexture,
                                             TextureAtlasSprite[] coverageByPixel,
                                             TextureAtlasSprite depthMask,
                                             boolean innerLayer,
                                             int coverageGeneration) {
            if (innerLayer) {
                if (!armorTexture.equals(lastInnerArmorTexture)
                    || lastInnerGeneration != coverageGeneration
                    || lastInnerMask != depthMask) {
                    rebuild(innerTexture, armorTexture, coverageByPixel, depthMask);
                    lastInnerArmorTexture = armorTexture;
                    lastInnerGeneration = coverageGeneration;
                    lastInnerMask = depthMask;
                }
                return innerLocation;
            }

            if (!armorTexture.equals(lastOuterArmorTexture)
                || lastOuterGeneration != coverageGeneration
                || lastOuterMask != depthMask) {
                rebuild(outerTexture, armorTexture, coverageByPixel, depthMask);
                lastOuterArmorTexture = armorTexture;
                lastOuterGeneration = coverageGeneration;
                lastOuterMask = depthMask;
            }
            return outerLocation;
        }

        private void rebuild(DynamicTexture target,
                             ResourceLocation armorTexture,
                             TextureAtlasSprite[] coverageByPixel,
                             TextureAtlasSprite depthMask) {
            NativeImage armorImage = loadArmorTexture(armorTexture);
            NativeImage targetImage = target.getPixels();
            if (targetImage == null) {
                return;
            }

            int emptyColor = FastColor.ARGB32.color(0, 0, 0, 0);
            for (int y = 0; y < TEXTURE_SIZE; y++) {
                for (int x = 0; x < TEXTURE_SIZE; x++) {
                    targetImage.setPixelRGBA(x, y, emptyColor);
                }
            }

            if (armorImage == null || coverageByPixel == null || depthMask == null) {
                target.upload();
                return;
            }

            int armorWidth = Math.min(TEXTURE_SIZE, armorImage.getWidth());
            int armorHeight = Math.min(TEXTURE_SIZE, armorImage.getHeight());
            int maskWidth = Math.max(1, depthMask.contents().width());
            int maskHeight = Math.max(1, depthMask.contents().height());

            for (int y = 0; y < armorHeight; y++) {
                for (int x = 0; x < armorWidth; x++) {
                    int armorColor = armorImage.getPixelRGBA(x, y);
                    int armorAlpha = FastColor.ARGB32.alpha(armorColor);
                    if (armorAlpha == 0) {
                        continue;
                    }

                    int maskX = x * maskWidth / armorWidth;
                    int maskY = y * maskHeight / armorHeight;
                    int depthIndex = computeDepthIndex(depthMask, maskX, maskY);
                    TextureAtlasSprite coverageSprite = coverageByPixel[depthIndex];
                    if (coverageSprite == null) {
                        continue;
                    }

                    int spriteWidth = Math.max(1, coverageSprite.contents().width());
                    int spriteHeight = Math.max(1, coverageSprite.contents().height());
                    int spriteX = x * spriteWidth / armorWidth;
                    int spriteY = y * spriteHeight / armorHeight;
                    int coverageColor = coverageSprite.getPixelRGBA(0, spriteX, spriteY);
                    int coverageAlpha = FastColor.ARGB32.alpha(coverageColor);
                    if (coverageAlpha == 0) {
                        continue;
                    }

                    int outAlpha = coverageAlpha * armorAlpha / 255;
                    int outColor = FastColor.ARGB32.color(
                        outAlpha,
                        FastColor.ARGB32.red(coverageColor),
                        FastColor.ARGB32.green(coverageColor),
                        FastColor.ARGB32.blue(coverageColor)
                    );
                    targetImage.setPixelRGBA(x, y, outColor);
                }
            }

            target.upload();
        }
    }

    private static int computeDepthIndex(TextureAtlasSprite depthMask, int x, int y) {
        int depthRGBA = depthMask.getPixelRGBA(0, x, y);
        float depthFloat = (float) FastColor.ARGB32.alpha(depthRGBA) / 255.0F;
        int depthIndex = (int) (depthFloat * 31.0F);
        return Math.max(0, Math.min(31, depthIndex));
    }
}

