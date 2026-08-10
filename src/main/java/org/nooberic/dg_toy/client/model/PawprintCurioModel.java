package org.nooberic.dg_toy.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.nooberic.dg_toy.Dg_toy;

public class PawprintCurioModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Dg_toy.MODID, "pawprint_curio"),
            "main"
    );

    public PawprintCurioModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition pawprint = root.addOrReplaceChild(
                "pawprint",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, -1.5708F, 1.5708F)
        );
        PartDefinition side = pawprint.addOrReplaceChild(
                "side",
                CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-2.0F, -1.5F, 4.0F, 4.0F, 1.5F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 5).addBox(-4.0F, -1.5F, -5.0F, 8.0F, 1.5F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );
        side.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 13).addBox(-1.0F, -1.0F, -7.75F, 1.0F, 1.5F, 7.75F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -0.5F, 5.0F, 0.0F, -0.5236F, 0.0F));
        side.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 3).addBox(0.0F, -1.0F, -7.75F, 1.0F, 1.5F, 7.75F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.5F, 5.0F, 0.0F, 0.5236F, 0.0F));
        side.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(18, 22).addBox(0.0F, -1.5F, -3.0F, 1.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 0.0F, -2.0F, 0.0F, 0.5236F, 0.0F));
        side.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(18, 16).addBox(-1.0F, -1.5F, -3.0F, 1.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 0.0F, -2.0F, 0.0F, -0.5236F, 0.0F));
        pawprint.addOrReplaceChild(
                "panel",
                CubeListBuilder.create()
                        .texOffs(18, 8).addBox(-4.0F, -1.25F, -4.0F, 8.0F, 1.25F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-5.0F, -1.25F, -3.0F, 10.0F, 1.25F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 3).addBox(-4.5F, -1.25F, -1.0F, 9.0F, 1.25F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 10).addBox(-4.0F, -1.25F, 0.0F, 8.0F, 1.25F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 12).addBox(-3.5F, -1.25F, 1.0F, 7.0F, 1.25F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 14).addBox(-3.0F, -1.25F, 2.0F, 6.0F, 1.25F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 0).addBox(-2.0F, -1.25F, 3.0F, 4.0F, 1.25F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
    }
}
