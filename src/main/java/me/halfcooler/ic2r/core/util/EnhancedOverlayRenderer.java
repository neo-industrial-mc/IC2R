package me.halfcooler.ic2r.core.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * GT-style face grid overlay when holding tools that use {@link RotationUtil#rotateByHit}.
 * Draws a 3×3 region grid on the looked-at face, plus an X on the region for the machine's current facing.
 */
@OnlyIn(Dist.CLIENT)
public final class EnhancedOverlayRenderer
{
	/** Slight lift along the face normal to avoid z-fighting. */
	private static final float FACE_EPS = 0.002F;
	/** Block outline expand amount (matches vanilla selection). */
	private static final double OUTLINE_EPS = 0.002D;

	// Semi-transparent black, same idea as GT's default block overlay colour.
	private static final float R = 0.0F;
	private static final float G = 0.0F;
	private static final float B = 0.0F;
	private static final float A = 0.5F;

	private EnhancedOverlayRenderer()
	{
	}

	/**
	 * @param currentFacing machine facing to mark with an X, or {@code null} to skip the mark
	 * @return {@code true} if the vanilla highlight should be cancelled (we drew our own outline)
	 */
	public static boolean render(
		Level world,
		BlockHitResult target,
		PoseStack poseStack,
		MultiBufferSource buffers,
		Direction currentFacing
	)
	{
		BlockPos pos = target.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (state.isAir())
		{
			return false;
		}

		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Direction side = target.getDirection();
		VertexConsumer lines = buffers.getBuffer(RenderType.lines());

		poseStack.pushPose();
		poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

		// Coloured block outline (replaces vanilla black selection box).
		VoxelShape shape = state.getShape(world, pos);
		if (!shape.isEmpty())
		{
			AABB bounds = shape.bounds().inflate(OUTLINE_EPS);
			LevelRenderer.renderLineBox(poseStack, lines, bounds, R, G, B, A);
		}

		// Face grid + optional facing mark, in block-local coordinates.
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();

		drawFaceGrid(lines, matrix, normal, side);
		if (currentFacing != null)
		{
			drawFacingMark(lines, matrix, normal, side, currentFacing);
		}

		poseStack.popPose();
		return true;
	}

	private static void drawFaceGrid(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Direction side)
	{
		// Four lines at 0.25 / 0.75 on the face UV plane (matches RotationUtil thresholds).
		lineOnFace(consumer, matrix, normal, side, 0.0F, 0.25F, 1.0F, 0.25F);
		lineOnFace(consumer, matrix, normal, side, 0.0F, 0.75F, 1.0F, 0.75F);
		lineOnFace(consumer, matrix, normal, side, 0.25F, 0.0F, 0.25F, 1.0F);
		lineOnFace(consumer, matrix, normal, side, 0.75F, 0.0F, 0.75F, 1.0F);
	}

	/**
	 * Draw an X on the UV region that {@link RotationUtil#rotateByHit} maps to {@code facing}.
	 * Region layout matches the 0.25 / 0.75 grid: center, four edges, four corners (opposite).
	 */
	private static void drawFacingMark(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Direction sideHit, Direction facing)
	{
		if (facing == sideHit)
		{
			// Center square
			drawX(consumer, matrix, normal, sideHit, 0.25F, 0.25F, 0.75F, 0.75F);
			return;
		}

		if (facing == sideHit.getOpposite())
		{
			// All four corners
			drawX(consumer, matrix, normal, sideHit, 0.0F, 0.0F, 0.25F, 0.25F);
			drawX(consumer, matrix, normal, sideHit, 0.75F, 0.0F, 1.0F, 0.25F);
			drawX(consumer, matrix, normal, sideHit, 0.0F, 0.75F, 0.25F, 1.0F);
			drawX(consumer, matrix, normal, sideHit, 0.75F, 0.75F, 1.0F, 1.0F);
			return;
		}

		// Edge region for an adjacent facing — UV axes match rotateByHit hit coords.
		float[] edge = edgeUvForFacing(sideHit, facing);
		if (edge != null)
		{
			drawX(consumer, matrix, normal, sideHit, edge[0], edge[1], edge[2], edge[3]);
		}
	}

	/**
	 * @return UV rect {u0, v0, u1, v1} for the edge strip that selects {@code facing}, or null
	 */
	private static float[] edgeUvForFacing(Direction sideHit, Direction facing)
	{
		// UV axes: DOWN/UP → U=x V=z; NORTH/SOUTH → U=x V=y; WEST/EAST → U=z V=y
		// Edge mid strips: low/high U or V in [0,0.25] or [0.75,1] with the other axis in (0.25,0.75)
		return switch (sideHit)
		{
			case DOWN, UP -> switch (facing)
			{
				case WEST -> new float[] { 0.0F, 0.25F, 0.25F, 0.75F };
				case EAST -> new float[] { 0.75F, 0.25F, 1.0F, 0.75F };
				case NORTH -> new float[] { 0.25F, 0.0F, 0.75F, 0.25F };
				case SOUTH -> new float[] { 0.25F, 0.75F, 0.75F, 1.0F };
				default -> null;
			};
			case NORTH, SOUTH -> switch (facing)
			{
				case WEST -> new float[] { 0.0F, 0.25F, 0.25F, 0.75F };
				case EAST -> new float[] { 0.75F, 0.25F, 1.0F, 0.75F };
				case DOWN -> new float[] { 0.25F, 0.0F, 0.75F, 0.25F };
				case UP -> new float[] { 0.25F, 0.75F, 0.75F, 1.0F };
				default -> null;
			};
			case WEST, EAST -> switch (facing)
			{
				case NORTH -> new float[] { 0.0F, 0.25F, 0.25F, 0.75F };
				case SOUTH -> new float[] { 0.75F, 0.25F, 1.0F, 0.75F };
				case DOWN -> new float[] { 0.25F, 0.0F, 0.75F, 0.25F };
				case UP -> new float[] { 0.25F, 0.75F, 0.75F, 1.0F };
				default -> null;
			};
		};
	}

	private static void drawX(
		VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, Direction side,
		float u0, float v0, float u1, float v1
	)
	{
		lineOnFace(consumer, matrix, normal, side, u0, v0, u1, v1);
		lineOnFace(consumer, matrix, normal, side, u0, v1, u1, v0);
	}

	/**
	 * Draw a line on a block face using UV in [0,1]×[0,1].
	 * U/V orientation matches the hit-position axes used by {@link RotationUtil#rotateByHit}.
	 */
	private static void lineOnFace(
		VertexConsumer consumer,
		Matrix4f matrix,
		Matrix3f normalMatrix,
		Direction side,
		float u1, float v1,
		float u2, float v2
	)
	{
		float[] p1 = uvToLocal(side, u1, v1);
		float[] p2 = uvToLocal(side, u2, v2);
		line(consumer, matrix, normalMatrix, p1[0], p1[1], p1[2], p2[0], p2[1], p2[2]);
	}

	/**
	 * Map face UV to block-local coordinates with a small outward normal offset.
	 * UV axes follow the same hit coordinates as {@link RotationUtil#rotateByHit}:
	 * <ul>
	 *   <li>DOWN/UP: U=x, V=z</li>
	 *   <li>NORTH/SOUTH: U=x, V=y</li>
	 *   <li>WEST/EAST: U=z, V=y</li>
	 * </ul>
	 */
	private static float[] uvToLocal(Direction side, float u, float v)
	{
		return switch (side)
		{
			case DOWN -> new float[] { u, -FACE_EPS, v };
			case UP -> new float[] { u, 1.0F + FACE_EPS, v };
			case NORTH -> new float[] { u, v, -FACE_EPS };
			case SOUTH -> new float[] { u, v, 1.0F + FACE_EPS };
			case WEST -> new float[] { -FACE_EPS, v, u };
			case EAST -> new float[] { 1.0F + FACE_EPS, v, u };
		};
	}

	private static void line(
		VertexConsumer consumer,
		Matrix4f matrix,
		Matrix3f normalMatrix,
		float x1, float y1, float z1,
		float x2, float y2, float z2
	)
	{
		float dx = x2 - x1;
		float dy = y2 - y1;
		float dz = z2 - z1;
		float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 1.0E-6F)
		{
			return;
		}

		dx /= len;
		dy /= len;
		dz /= len;

		consumer.vertex(matrix, x1, y1, z1).color(R, G, B, A).normal(normalMatrix, dx, dy, dz).endVertex();
		consumer.vertex(matrix, x2, y2, z2).color(R, G, B, A).normal(normalMatrix, dx, dy, dz).endVertex();
	}
}
