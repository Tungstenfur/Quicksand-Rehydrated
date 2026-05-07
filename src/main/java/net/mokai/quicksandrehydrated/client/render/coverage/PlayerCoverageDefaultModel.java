//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.mokai.quicksandrehydrated.client.render.coverage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class PlayerCoverageDefaultModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("qsrehydrated", "coverage_layer_default"), "coverage_default");
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public final ModelPart headArmor;
    public final ModelPart bodyArmor;
    public final ModelPart rightArmArmor;
    public final ModelPart leftArmArmor;
    public final ModelPart rightLegArmorOuter;
    public final ModelPart leftLegArmorOuter;
    public final ModelPart rightLegArmorInner;
    public final ModelPart leftLegArmorInner;
    public final ModelPart rightFootArmor;
    public final ModelPart leftFootArmor;

    public boolean renderHelmet;
    public boolean renderChestplate;
    public boolean renderLeggings;
    public boolean renderBoots;

    public PlayerCoverageDefaultModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");

        this.headArmor = root.getChild("head_armor");
        this.bodyArmor = root.getChild("body_armor");
        this.rightArmArmor = root.getChild("right_arm_armor");
        this.leftArmArmor = root.getChild("left_arm_armor");
        this.rightLegArmorOuter = root.getChild("right_leg_armor_outer");
        this.leftLegArmorOuter = root.getChild("left_leg_armor_outer");
        this.rightLegArmorInner = root.getChild("right_leg_armor_inner");
        this.leftLegArmorInner = root.getChild("left_leg_armor_inner");
        this.rightFootArmor = root.getChild("right_foot_armor");
        this.leftFootArmor = root.getChild("left_foot_armor");
    }

    public void syncArmorTransforms() {
        this.headArmor.copyFrom(this.head);
        this.bodyArmor.copyFrom(this.body);
        this.rightArmArmor.copyFrom(this.rightArm);
        this.leftArmArmor.copyFrom(this.leftArm);
        this.rightLegArmorOuter.copyFrom(this.rightLeg);
        this.leftLegArmorOuter.copyFrom(this.leftLeg);
        this.rightLegArmorInner.copyFrom(this.rightLeg);
        this.leftLegArmorInner.copyFrom(this.leftLeg);
        this.rightFootArmor.copyFrom(this.rightLeg);
        this.leftFootArmor.copyFrom(this.leftLeg);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        // Head - first layer (skin) and second layer (hat/helmet)
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.1F)), 
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Hat - explicitly defined as second layer
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.26F)), 
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Body - first layer (skin) and second layer (jacket)
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.26F)), 
            PartPose.offset(0.0F, 0.0F, 0.0F));
        
        // Right arm - first layer (skin) and second layer (sleeve)
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F)), 
            PartPose.offset(-5.0F, 2.0F, 0.0F));
        
        // Left arm - first layer (skin) and second layer (sleeve)
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.26F)), 
            PartPose.offset(5.0F, 2.0F, 0.0F));
        
        // Right leg - first layer (skin) and second layer (pants)
        // Increased deformation for second layer to ensure it's visible
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), 
            PartPose.offset(-1.9F, 12.0F, 0.0F));
        
        // Left leg - first layer (skin) and second layer (pants)
        // Increased deformation for second layer to ensure it's visible
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), 
            PartPose.offset(1.9F, 12.0F, 0.0F));

        // ARMOR PARTS
        partdefinition.addOrReplaceChild("head_armor", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.15F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body_armor", CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm_armor", CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm_armor", CubeListBuilder.create()
            .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(5.0F, 2.0F, 0.0F));

        // outer generic armor for leggings (leggings use 0.5F typically, so 0.7F coverage)
        partdefinition.addOrReplaceChild("right_leg_armor_inner", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.7F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg_armor_inner", CubeListBuilder.create()
            .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.7F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg_armor_outer", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg_armor_outer", CubeListBuilder.create()
            .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_foot_armor", CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(-1.9F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_foot_armor", CubeListBuilder.create()
            .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1.15F)),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.syncArmorTransforms();

        // Render all body parts - first layer
        (this.renderHelmet ? this.headArmor : this.head).render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);

        if (this.renderChestplate) {
            this.bodyArmor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.rightArmArmor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftArmArmor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        } else {
            this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        if (this.renderLeggings && !this.renderBoots) {
            this.rightLegArmorInner.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftLegArmorInner.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        } else if (this.renderLeggings && this.renderBoots) {
            this.rightLegArmorOuter.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftLegArmorOuter.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        } else if (!this.renderLeggings && this.renderBoots) {
            this.rightFootArmor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftFootArmor.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        } else {
            this.rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            this.leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // Render hat (second layer for head) explicitly with full alpha
        if (!this.renderHelmet) {
            this.hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
