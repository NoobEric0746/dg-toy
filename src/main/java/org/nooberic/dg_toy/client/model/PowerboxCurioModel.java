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

public class PowerboxCurioModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Dg_toy.MODID, "powerbox_curio"),
            "main"
    );

    public PowerboxCurioModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Curios renders the whole model root; keep the standard humanoid parts empty
        // so only the powerbox part below is drawn.
        PartDefinition head = partDefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        // Keep the standard left-leg pose so the renderer's leftLeg.translateAndRotate
        // anchors the model at the left hip (matching the vanilla humanoid layout).
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        partDefinition.addOrReplaceChild(
            "bone",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -2.0F, -8.0F, 9.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18)
                .addBox(5.0F, -2.0F, -7.0F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(30, 18)
                .addBox(-5.0F, -2.0F, -7.0F, 1.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 1.5708F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
    }
}
