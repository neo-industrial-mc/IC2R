package ic2.core.item.armor.jetpack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LayerJetpackOverride extends RenderLayer<LivingEntity, HumanoidModel<LivingEntity>> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          "ic2", "textures/models/armor/ic2_jet_pack_layer_1.png");

  public LayerJetpackOverride(
      RenderLayerParent<LivingEntity, HumanoidModel<LivingEntity>> renderer) {
    super(renderer);
  }

  /**
   * Attaches one layer instance to every renderer backed by a {@link HumanoidModel}. Each layer
   * must be constructed with the renderer it is added to: the layer animates {@code
   * getParentModel()}, and a model borrowed from another renderer may cast the entity (e.g. {@code
   * ArmorStandModel} casts to {@code ArmorStand}).
   */
  public static void register(EntityRenderersEvent.AddLayers event) {
    for (PlayerSkin.Model skin : event.getSkins()) {
      if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> renderer
          && renderer.getModel() instanceof HumanoidModel) {
        addTo(renderer);
      }
    }

    for (EntityType<?> type : event.getEntityTypes()) {
      if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?> renderer
          && renderer.getModel() instanceof HumanoidModel) {
        addTo(renderer);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static void addTo(LivingEntityRenderer renderer) {
    renderer.addLayer(new LayerJetpackOverride(renderer));
  }

  public void render(
      @NotNull PoseStack poseStack,
      @NotNull MultiBufferSource bufferSource,
      int packedLight,
      LivingEntity entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch) {
    ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
    if (JetpackHandler.hasJetpackAttached(chestStack)) {
      HumanoidModel<LivingEntity> model = this.getParentModel();
      boolean[] saved = saveVisibility(model);
      try {
        model.setAllVisible(false);
        model.body.visible = true;
        model.rightArm.visible = true;
        model.leftArm.visible = true;
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        VertexConsumer consumer =
            ItemRenderer.getArmorFoilBuffer(
                bufferSource, RenderType.armorCutoutNoCull(TEXTURE), chestStack.hasFoil());
        model.renderToBuffer(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F));
      } finally {
        restoreVisibility(model, saved);
      }
    }
  }

  private boolean[] saveVisibility(HumanoidModel<LivingEntity> model) {
    boolean[] saved = {
      model.head.visible,
      model.hat.visible,
      model.body.visible,
      model.rightArm.visible,
      model.leftArm.visible,
      model.rightLeg.visible,
      model.leftLeg.visible,
      true,
      true,
      true,
      true,
      true
    };
    if (model instanceof PlayerModel<?> playerModel) {
      saved[7] = playerModel.jacket.visible;
      saved[8] = playerModel.rightSleeve.visible;
      saved[9] = playerModel.leftSleeve.visible;
      saved[10] = playerModel.rightPants.visible;
      saved[11] = playerModel.leftPants.visible;
    }
    return saved;
  }

  private void restoreVisibility(HumanoidModel<LivingEntity> model, boolean[] saved) {
    // Covers parts hidden by setAllVisible(false) that are not reachable below, e.g.
    // PlayerModel's private cloak and ear.
    model.setAllVisible(true);
    model.head.visible = saved[0];
    model.hat.visible = saved[1];
    model.body.visible = saved[2];
    model.rightArm.visible = saved[3];
    model.leftArm.visible = saved[4];
    model.rightLeg.visible = saved[5];
    model.leftLeg.visible = saved[6];
    if (model instanceof PlayerModel<?> playerModel) {
      playerModel.jacket.visible = saved[7];
      playerModel.rightSleeve.visible = saved[8];
      playerModel.leftSleeve.visible = saved[9];
      playerModel.rightPants.visible = saved[10];
      playerModel.leftPants.visible = saved[11];
    }
  }
}
