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

public class DgCollarCurioModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Dg_toy.MODID, "dg_collar_curio"),
            "main"
    );

    public DgCollarCurioModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "choker",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -5.0F, -1.0F, 6.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 7).addBox(3.0F, -4.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(6, 7).addBox(-4.0F, -4.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, 2.75F, -2.0F)
        );

        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
    }
}
