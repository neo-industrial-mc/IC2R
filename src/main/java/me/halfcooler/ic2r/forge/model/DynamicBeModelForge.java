package me.halfcooler.ic2r.forge.model;

import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.block.DynamicBeModel;
import me.halfcooler.ic2r.core.block.comp.Obscuration;
import me.halfcooler.ic2r.core.block.tileentity.Ic2rTileEntity;
import me.halfcooler.ic2r.core.block.wiring.tileentity.TileEntityFeConverter;
import me.halfcooler.ic2r.core.util.LogCategory;
import me.halfcooler.ic2r.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

final class DynamicBeModelForge extends DynamicBeModel<List<List<BakedQuad>>> implements Ic2rModel, IDynamicBakedModel
{
	private static final ModelProperty<List<List<BakedQuad>>> MESH_DATA = new ModelProperty<>();

	private final boolean feConverter;
	private final ResourceLocation euModelId;
	private final ResourceLocation feModelId;
	private final ResourceLocation particleTextureId;
	/**
	 * Per-port full-cube meshes (same texture on all faces); index = face ordinal.
	 */
	private List<List<BakedQuad>> nonePortMesh;
	private List<List<BakedQuad>> euPortMesh;
	private List<List<BakedQuad>> fePortMesh;
	/**
	 * Explicit particle sprite so break FX never falls back to missing texture.
	 */
	private TextureAtlasSprite particleSprite;

	DynamicBeModelForge(ResourceLocation id)
	{
		super(id);
		this.feConverter = TileEntityFeConverter.class.isAssignableFrom(this.block.getTeClass());
		this.euModelId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/wiring/fe_converter_eu");
		this.feModelId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/wiring/fe_converter_fe");
		this.particleTextureId = this.feConverter
			? ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/wiring/fe_converter/none")
			: null;
	}

	private static int getIdx(Direction dir)
	{
		return dir == null ? 6 : dir.ordinal();
	}

	private static List<BakedQuad> getObscuredQuads(Obscuration.ObscurationData data, Direction targetFace)
	{
		BakedModel refModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(data.state());
		RandomSource rand = RandomSource.create(42L);
		List<BakedQuad> refQuads = refModel.getQuads(data.state(), data.side(), rand, ModelData.EMPTY, null);

		if (refQuads.isEmpty())
		{
			return null;
		}

		List<BakedQuad> result = new ArrayList<>(refQuads.size());

		for (int i = 0; i < refQuads.size(); i++)
		{
			BakedQuad quad = refQuads.get(i);

			if (data.side() != targetFace)
			{
				quad = transformQuadFace(quad, data.side(), targetFace);
			}

			if (data.colorMultipliers() != null && i < data.colorMultipliers().length && data.colorMultipliers()[i] != -1)
			{
				quad = tintQuad(quad, data.colorMultipliers()[i]);
			}

			result.add(quad);
		}

		return result;
	}

	private static BakedQuad transformQuadFace(BakedQuad quad, Direction fromFace, Direction toFace)
	{
		int[] oldData = quad.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		for (int v = 0; v < 4; v++)
		{
			int off = v * stride;
			float x = Float.intBitsToFloat(oldData[off]);
			float y = Float.intBitsToFloat(oldData[off + 1]);
			float z = Float.intBitsToFloat(oldData[off + 2]);

			float[] uv = posToUV(fromFace, x, y, z);
			float[] newPos = uvToPos(toFace, uv[0], uv[1]);

			newData[off] = Float.floatToRawIntBits(newPos[0]);
			newData[off + 1] = Float.floatToRawIntBits(newPos[1]);
			newData[off + 2] = Float.floatToRawIntBits(newPos[2]);
		}

		return new BakedQuad(newData, quad.getTintIndex(), toFace, quad.getSprite(), quad.isShade());
	}

	private static float[] posToUV(Direction face, float x, float y, float z)
	{
		return switch (face)
		{
			case NORTH -> new float[] { x, y };
			case SOUTH -> new float[] { 1 - x, y };
			case WEST -> new float[] { 1 - z, y };
			case EAST -> new float[] { z, y };
			case DOWN -> new float[] { x, 1 - z };
			case UP -> new float[] { x, z };
		};
	}

	private static float[] uvToPos(Direction face, float u, float v)
	{
		return switch (face)
		{
			case NORTH -> new float[] { u, v, 0 };
			case SOUTH -> new float[] { 1 - u, v, 1 };
			case WEST -> new float[] { 0, v, 1 - u };
			case EAST -> new float[] { 1, v, u };
			case DOWN -> new float[] { u, 0, 1 - v };
			case UP -> new float[] { u, 1, v };
		};
	}

	private static BakedQuad tintQuad(BakedQuad quad, int color)
	{
		int[] oldData = quad.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		float r = ((color >>> 16) & 0xFF) / 255f;
		float g = ((color >>> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		for (int v = 0; v < 4; v++)
		{
			int off = v * stride + 3;
			int packedColor = oldData[off];
			int a = (packedColor >>> 24) & 0xFF;
			int red = (int) ((packedColor & 0xFF) * r);
			int green = (int) (((packedColor >>> 8) & 0xFF) * g);
			int blue = (int) (((packedColor >>> 16) & 0xFF) * b);
			newData[off] = (a << 24) | (blue << 16) | (green << 8) | red;
		}

		return new BakedQuad(newData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}

	@Override
	public @NotNull Collection<ResourceLocation> getDependencies()
	{
		if (!this.feConverter)
		{
			return super.getDependencies();
		}

		List<ResourceLocation> deps = new ArrayList<>(super.getDependencies());
		deps.add(this.euModelId);
		deps.add(this.feModelId);
		return deps;
	}

	@Override
	public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> resolver)
	{
		super.resolveParents(resolver);
		if (this.feConverter)
		{
			for (ResourceLocation id : List.of(this.euModelId, this.feModelId))
			{
				UnbakedModel model = resolver.apply(id);
				if (model == null)
				{
					IC2R.log.warn(LogCategory.Resource, "Missing model %s", id);
				} else
				{
					model.resolveParents(resolver);
				}
			}
		}
	}

	@Override
	public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> modelGetter, @NotNull IGeometryBakingContext context)
	{
		this.resolveParents(modelGetter);
	}

	@Override
	public @NotNull BakedModel bake(
		@NotNull IGeometryBakingContext owner,
		@NotNull ModelBaker bakery,
		@NotNull Function<Material, TextureAtlasSprite> spriteGetter,
		@NotNull ModelState modelTransform,
		@NotNull ItemOverrides overrides
	)
	{
		BakedModel result = super.bake(bakery, spriteGetter, modelTransform);
		if (this.feConverter)
		{
			BakedModel euModel = bakery.bake(this.euModelId, modelTransform, spriteGetter);
			BakedModel feModel = bakery.bake(this.feModelId, modelTransform, spriteGetter);
			if (euModel == null || feModel == null)
			{
				throw new IllegalStateException("missing fe_converter port models");
			}
			this.nonePortMesh = this.generateMesh(this.baseModel, 0, false);
			this.euPortMesh = this.generateMesh(euModel, 0, false);
			this.fePortMesh = this.generateMesh(feModel, 0, false);
			this.particleSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, this.particleTextureId));
		} else if (this.baseModel != null)
		{
			this.particleSprite = this.baseModel.getParticleIcon();
		}

		return result;
	}

	@Override
	public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData)
	{
		BlockEntity be = world.getBlockEntity(pos);
		List<List<BakedQuad>> mesh;

		if (this.feConverter && be instanceof TileEntityFeConverter converter
			&& this.nonePortMesh != null && this.euPortMesh != null && this.fePortMesh != null)
		{
			mesh = this.composeFeConverterMesh(converter.getFaceModesPacked());
		} else
		{
			boolean active = this.block.canActive() && be instanceof Ic2rTileEntity te && te.getActive();
			mesh = this.getMesh(state, active);
		}

		if (be instanceof Ic2rTileEntity te)
		{
			Obscuration component = te.getComponent(Obscuration.class);
			if (component != null)
			{
				Obscuration.ObscurationData[] data = component.getRenderState();
				if (data != null)
				{
					mesh = new ArrayList<>(mesh);

					for (int face = 0; face < 6; face++)
					{
						if (data[face] != null)
						{
							List<BakedQuad> obscured = getObscuredQuads(data[face], Util.ALL_DIRS[face]);
							if (obscured != null)
							{
								mesh.set(face, obscured);
							}
						}
					}
				}
			}
		}

		tileData = tileData.derive().with(MESH_DATA, mesh).build();
		assert tileData.get(MESH_DATA) == mesh;
		return tileData;
	}

	private List<List<BakedQuad>> composeFeConverterMesh(int packed)
	{
		List<List<BakedQuad>> mesh = new ArrayList<>(7);
		for (int face = 0; face < 6; face++)
		{
			int mode = (packed >> (face * 2)) & 0x3;
			List<List<BakedQuad>> src = switch (mode)
			{
				case 1 -> this.euPortMesh;
				case 2 -> this.fePortMesh;
				default -> this.nonePortMesh;
			};
			mesh.add(src.get(face));
		}
		mesh.add(this.nonePortMesh.get(6));
		return mesh;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random)
	{
		return this.getQuads(state, side, random, ModelData.EMPTY, null);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType)
	{
		List<List<BakedQuad>> mesh = extraData.get(MESH_DATA);
		if (mesh == null)
		{
			if (this.feConverter && this.nonePortMesh != null)
			{
				return this.nonePortMesh.get(getIdx(side));
			}
			if (state != null && this.baseModel != null)
			{
				mesh = this.getMesh(state, false);
			} else
			{
				return Collections.emptyList();
			}
		}

		List<BakedQuad> quads = mesh.get(getIdx(side));
		return quads != null ? quads : Collections.emptyList();
	}

	protected List<List<BakedQuad>> generateMesh(BakedModel baseModel, int rot, boolean rotX)
	{
		RandomSource rand = RandomSource.create(42L);
		List<List<BakedQuad>> mesh = new ArrayList<>(7);

		for (int i = 0; i < 7; i++) mesh.add(Collections.emptyList());

		for (int i = 0; i < 7; i++)
		{
			Direction face = i < 6 ? Util.ALL_DIRS[i] : null;
			List<BakedQuad> quads = baseModel.getQuads(null, face, rand, ModelData.EMPTY, null);
			int writeIdx = i;
			if (rot != 0)
			{
				if (face != null)
				{
					writeIdx = rotateFace(face, rot, rotX).ordinal();
				}

				if (!quads.isEmpty())
				{
					List<BakedQuad> newQuads = new ArrayList<>(quads.size());

					for (BakedQuad quad : quads)
					{
						newQuads.add(rotateQuad(quad, rot, rotX));
					}

					quads = newQuads;
				}
			}

			mesh.set(writeIdx, quads);
		}

		return mesh;
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
	{
		return this.getParticleIcon();
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon()
	{
		if (this.particleSprite != null)
		{
			return this.particleSprite;
		}
		return Objects.requireNonNullElseGet(this.baseModel, () -> Minecraft.getInstance().getModelManager().getMissingModel()).getParticleIcon();
	}
}
