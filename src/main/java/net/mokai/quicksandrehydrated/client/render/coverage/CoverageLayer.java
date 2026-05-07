package net.mokai.quicksandrehydrated.client.render.coverage;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;
import net.mokai.quicksandrehydrated.entity.coverage.CoverageEntry;
import net.mokai.quicksandrehydrated.entity.coverage.PlayerCoverage;
import net.mokai.quicksandrehydrated.entity.playerStruggling;
import net.mokai.quicksandrehydrated.registry.ModModelLayers;

public class CoverageLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    PlayerCoverageDefaultModel coverageModel;
    private final DynamicTexture texture;
    private final DynamicTexture splatterTexture;
    TextureManager textureManager;
    ResourceLocation resourcelocation;
    ResourceLocation splatterResourcelocation;

    private static final float EDGE_ROUGHNESS = 0.15F;
    private static final int DEPTH_SHIFT_Y = 2;

    public CoverageLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer, boolean pSlim) {
        super(pRenderer);

        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (!pSlim) {
            this.coverageModel = new PlayerCoverageDefaultModel(modelSet.bakeLayer(ModModelLayers.COVERAGE_LAYER_DEFAULT));
        } else {
            this.coverageModel = new PlayerCoverageSlimModel(modelSet.bakeLayer(ModModelLayers.COVERAGE_LAYER_SLIM));
        }

        this.texture = new DynamicTexture(64, 64, true);
        this.splatterTexture = new DynamicTexture(64, 64, true);
        this.textureManager = Minecraft.getInstance().textureManager;
        this.resourcelocation = Minecraft.getInstance().textureManager.register("coverage", this.texture);
        this.splatterResourcelocation = Minecraft.getInstance().textureManager.register("coverage_splatter", this.splatterTexture);

    }

    private void extendPixelUp(NativeImage img, TextureAtlasSprite depthMask, TextureAtlasSprite[] coverageByPixel, int x, int y, int sourceDepthIndexRaw, int color) {
        if (y < DEPTH_SHIFT_Y) {
            return;
        }
        int targetY = y - DEPTH_SHIFT_Y;
        int targetDepthIndex = computeDepthIndexRaw(depthMask, x, targetY);
        if (coverageByPixel[targetDepthIndex] != coverageByPixel[sourceDepthIndexRaw]) {
            return;
        }
        int existing = img.getPixelRGBA(x, targetY);
        if (FastColor.ARGB32.alpha(existing) == 0) {
            img.setPixelRGBA(x, targetY, color);
        }
    }

    private int computeDepthIndexRaw(TextureAtlasSprite depthMask, int x, int y) {
        int depthRGBA = depthMask.getPixelRGBA(0, x, y);
        float depthFloat = (float) FastColor.ARGB32.alpha(depthRGBA) / 255.0F;
        int depthIndex = (int) (depthFloat * 31.0F);
        return Math.max(0, Math.min(31, depthIndex));
    }

    private int computeDepthIndex(TextureAtlasSprite depthMask, int x, int y) {
        int depthRGBA = depthMask.getPixelRGBA(0, x, y);
        float depthFloat = (float) FastColor.ARGB32.alpha(depthRGBA) / 255.0F;

        int hash = (x * 73428767) ^ (y * 912783);
        float jitter = (((hash >>> 10) & 255) / 255.0F - 0.5F) * EDGE_ROUGHNESS;
        depthFloat = Math.max(0.0F, Math.min(1.0F, depthFloat + jitter));

        int depthIndex = (int) (depthFloat * 31.0F);
        return Math.max(0, Math.min(31, depthIndex));
    }

    private TextureAtlasSprite[] buildCoverageByPixel(PlayerCoverage coverage) {
        // Step one: make array with 32 values.
        // Each value points to a TextureAtlasSprite,
        // which is the texture that should be applied at that depth.
        TextureAtlasSprite[] coverageByPixel = new TextureAtlasSprite[32];

        for (CoverageEntry entry : coverage.coverageEntries) {
            // both begin and end are inclusive
            for (int i = entry.begin; i <= entry.end; i++) {

                try {
                    coverageByPixel[i] = CoverageAtlasHolder.singleton.get(entry.texture);
                } catch (Exception e) {
                    // If texture can't be loaded, skip this entry (draws empty)
                    continue;
                }

            }
        }

        return coverageByPixel;
    }

    private void updateTexture(PlayerCoverage coverage) {

        TextureAtlasSprite[] coverageByPixel = buildCoverageByPixel(coverage);

        // Step two: go through every pixel and determine its depth
        // get the texture that should be applied at that depth.
        // if there is no texture to be applied, it sets to the pixel to 0 alpha

        TextureAtlasSprite depthMask = CoverageAtlasHolder.singleton.get(new ResourceLocation(QuicksandRehydrated.MOD_ID, "coverage_mask"));
        NativeImage img = this.texture.getPixels();

        if (img == null) return;

        for (int i = 0; i < 64; ++i) {
            for (int k = 0; k < 64; ++k) {

                int depthIndex = computeDepthIndex(depthMask, i, k);
                int depthIndexRaw = computeDepthIndexRaw(depthMask, i, k);

                // get the texture that should be here
                TextureAtlasSprite coverageTexture = coverageByPixel[depthIndex];

                // do not do anything if its null
                if (coverageTexture == null)  {
                    int emptyColor = FastColor.ARGB32.color(0, 0, 0, 0);
                    img.setPixelRGBA(i, k, emptyColor);
                    continue;
                }

                // get color, and set color
                int color_rgba = coverageTexture.getPixelRGBA(0, i, k);
                img.setPixelRGBA(i, k, color_rgba);
                extendPixelUp(img, depthMask, coverageByPixel, i, k, depthIndexRaw, color_rgba);

            }
        }

        this.texture.upload();

    }

    private void updateSplatterTexture(PlayerCoverage coverage) {
        TextureAtlasSprite[] coverageByPixel = buildCoverageByPixel(coverage);
        TextureAtlasSprite depthMask = CoverageAtlasHolder.singleton.get(new ResourceLocation(QuicksandRehydrated.MOD_ID, "coverage_mask"));
        NativeImage img = this.splatterTexture.getPixels();

        if (img == null) return;

        for (int i = 0; i < 64; ++i) {
            for (int k = 0; k < 64; ++k) {
                int depthIndex = computeDepthIndex(depthMask, i, k);
                int depthIndexRaw = computeDepthIndexRaw(depthMask, i, k);
                TextureAtlasSprite coverageTexture = coverageByPixel[depthIndex];

                if (coverageTexture == null)  {
                    img.setPixelRGBA(i, k, FastColor.ARGB32.color(0, 0, 0, 0));
                    continue;
                }

                int baseColor = coverageTexture.getPixelRGBA(0, i, k);
                int baseAlpha = FastColor.ARGB32.alpha(baseColor);
                if (baseAlpha == 0) {
                    img.setPixelRGBA(i, k, FastColor.ARGB32.color(0, 0, 0, 0));
                    continue;
                }

                int hash = (i * 73428767) ^ (k * 912783) ^ (depthIndex * 12043);
                float noise = ((hash >>> 8) & 255) / 255.0F;
                float splat = Math.max(0.0F, (noise - 0.70F) / 0.30F);
                int outAlpha = (int) (baseAlpha * splat * 0.85F);

                if (outAlpha <= 0) {
                    img.setPixelRGBA(i, k, FastColor.ARGB32.color(0, 0, 0, 0));
                    continue;
                }

                int outColor = FastColor.ARGB32.color(
                    outAlpha,
                    FastColor.ARGB32.red(baseColor),
                    FastColor.ARGB32.green(baseColor),
                    FastColor.ARGB32.blue(baseColor)
                );
                img.setPixelRGBA(i, k, outColor);
                extendPixelUp(img, depthMask, coverageByPixel, i, k, depthIndexRaw, outColor);
            }
        }

        this.splatterTexture.upload();
    }

    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pAbstractPlayer, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        try {
            playerStruggling playerqs = (playerStruggling) pAbstractPlayer;
            
            // Check if player has coverage
            PlayerCoverage pC = playerqs.getCoverage();
            if (pC == null || pC.coverageEntries.isEmpty()) {
                return; // No coverage to render
            }
            
            // Update texture if needed
            if (pC.renderUpdate) {
                pC.renderUpdate = false;
                this.updateTexture(pC);
                this.updateSplatterTexture(pC);
            }
            
            // Get the appropriate model
            PlayerCoverageDefaultModel model = this.coverageModel;
            
            // Copy properties from parent model
            this.getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(pAbstractPlayer, pLimbSwing, pLimbSwingAmount, pPartialTick);
            model.setupAnim(pAbstractPlayer, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

            // Set armor visibility flags
            model.renderHelmet = !pAbstractPlayer.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
            model.renderChestplate = !pAbstractPlayer.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
            model.renderLeggings = !pAbstractPlayer.getItemBySlot(EquipmentSlot.LEGS).isEmpty();
            model.renderBoots = !pAbstractPlayer.getItemBySlot(EquipmentSlot.FEET).isEmpty();
            
            // Ensure visibility of all parts, including all second layers
            model.hat.visible = true;
            model.leftLeg.visible = true;
            model.rightLeg.visible = true;
            model.leftArm.visible = true;
            model.rightArm.visible = true;
            model.body.visible = true;
            model.head.visible = true;
            
            // Use a translucent render type for better blending
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(this.resourcelocation));
            
            // Render the coverage with full opacity
            model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, 
                LivingEntityRenderer.getOverlayCoords(pAbstractPlayer, 0.0F), 
                1.0F, 1.0F, 1.0F, 1.0F);

            VertexConsumer splatterConsumer = pBuffer.getBuffer(RenderType.entityTranslucentCull(this.splatterResourcelocation));
            model.renderToBuffer(pPoseStack, splatterConsumer, pPackedLight,
                LivingEntityRenderer.getOverlayCoords(pAbstractPlayer, 0.0F),
                1.0F, 1.0F, 1.0F, 1.0F);

            // Debug: uncomment to print information about the coverage
            /*
            if (!pC.coverageEntries.isEmpty()) {
                System.out.println("Rendering coverage for player: " + pAbstractPlayer.getName().getString());
                System.out.println("Coverage entries: " + pC.coverageEntries.size());
                for (CoverageEntry entry : pC.coverageEntries) {
                    System.out.println("Entry: begin=" + entry.begin + ", end=" + entry.end +
                                      ", texture=" + entry.texture);
                }
            }
            */
        } catch (Exception e) {
            // Silently catch any exceptions to prevent rendering crashes
            // In a production environment, you might want to log this
            // e.printStackTrace(); // Uncomment for debugging
        }
    }

}
