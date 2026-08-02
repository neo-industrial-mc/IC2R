package ic2.core.crop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.api.crops.CropCard;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Renders crops which use the generic crop-stick block instead of a dedicated crop block. */
public class CropStickRenderer implements BlockEntityRenderer<TileEntityCrop> {
  public CropStickRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(
      TileEntityCrop cropTile,
      float partialTick,
      PoseStack poseStack,
      MultiBufferSource buffers,
      int light,
      int overlay) {
    CropCard crop = cropTile.getCrop();
    if (crop == null) {
      return;
    }

    List<ResourceLocation> textures = crop.getTexturesLocation();
    if (textures.isEmpty()) {
      return;
    }

    int textureIndex = Math.max(0, Math.min(cropTile.getCurrentAge() - 1, textures.size() - 1));
    ResourceLocation texture = asTextureFile(textures.get(textureIndex));
    VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
    PoseStack.Pose pose = poseStack.last();

    quad(vertices, pose, 0.0F, 0.001F, 0.5F, 1.0F, 1.001F, 0.5F, light);
    quad(vertices, pose, 0.5F, 0.001F, 0.0F, 0.5F, 1.001F, 1.0F, light);
  }

  private static ResourceLocation asTextureFile(ResourceLocation texture) {
    String path = texture.getPath();
    // CropCard's API retains IC2's old "blocks/" texture convention.
    if (path.startsWith("blocks/")) {
      path = "block/" + path.substring("blocks/".length());
    }
    return ResourceLocation.fromNamespaceAndPath(
        texture.getNamespace(), "textures/" + path + ".png");
  }

  private static void quad(
      VertexConsumer vertices,
      PoseStack.Pose pose,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      int light) {
    vertex(vertices, pose, x0, y1, z0, 0.0F, 0.0F, light);
    vertex(vertices, pose, x0, y0, z0, 0.0F, 1.0F, light);
    vertex(vertices, pose, x1, y0, z1, 1.0F, 1.0F, light);
    vertex(vertices, pose, x1, y1, z1, 1.0F, 0.0F, light);
  }

  private static void vertex(
      VertexConsumer vertices,
      PoseStack.Pose pose,
      float x,
      float y,
      float z,
      float u,
      float v,
      int light) {
    vertices
        .addVertex(pose.pose(), x, y, z)
        .setColor(255, 255, 255, 255)
        .setUv(u, v)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(light)
        .setNormal(pose, 0.0F, 1.0F, 0.0F);
  }
}
