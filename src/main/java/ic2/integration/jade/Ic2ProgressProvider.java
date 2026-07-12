package ic2.integration.jade;

import ic2.core.IC2;
import ic2.core.block.comp.Process;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.dynamic.IGuiValueProvider;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

/**
 * Unified processing progress for recipe-style machines.
 * <p>
 * Resolution order:
 * <ol>
 *   <li>{@link Process} component ratio</li>
 *   <li>{@link TileEntityStandardMachine#getProgress()}</li>
 *   <li>{@link IGuiValueProvider} key {@code "progress"}</li>
 *   <li>{@link CustomGauge.IGaugeRatioProvider}</li>
 *   <li>Scanner / Replicator specialty fields</li>
 * </ol>
 * Progress is only shown while {@code > 0}.
 */
public enum Ic2ProgressProvider implements IServerExtensionProvider<Object, CompoundTag>, IClientExtensionProvider<CompoundTag, ProgressView>
{
	INSTANCE;

	public static final ResourceLocation UID = IC2.getIdentifier("progress");
	private static final float MIN_VISIBLE = 1.0E-4F;

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, Object target, boolean showDetails)
	{
		float progress = resolveProgress(target);
		if (progress <= MIN_VISIBLE)
		{
			return null;
		}

		progress = Math.min(1.0F, progress);
		return List.of(new ViewGroup<>(List.of(ProgressView.create(progress))));
	}

	@Override
	public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups)
	{
		return ClientViewGroup.map(groups, ProgressView::read, (serverGroup, clientGroup) ->
		{
			for (ProgressView view : clientGroup.views)
			{
				int percent = Math.round(Math.min(1.0F, Math.max(0.0F, view.progress)) * 100.0F);
				view.text = Component.translatable("ic2.jade.progress", percent);
			}
		});
	}

	/**
	 * @return progress in {@code [0, 1]}, or {@code <= 0} when nothing to display
	 */
	static float resolveProgress(Object target)
	{
		if (target instanceof Ic2TileEntity te && te.hasComponent(Process.class))
		{
			float ratio = (float) te.getComponent(Process.class).getProgressRatio();
			if (ratio > MIN_VISIBLE)
			{
				return ratio;
			}
		}

		if (target instanceof TileEntityStandardMachine<?, ?, ?> machine)
		{
			float progress = machine.getProgress();
			if (progress > MIN_VISIBLE)
			{
				return progress;
			}
		}

		if (target instanceof IGuiValueProvider guiValues)
		{
			try
			{
				double progress = guiValues.getGuiValue("progress");
				if (progress > MIN_VISIBLE)
				{
					return (float) progress;
				}
			} catch (IllegalArgumentException | UnsupportedOperationException ignored)
			{
				// Provider does not expose "progress".
			}
		}

		if (target instanceof CustomGauge.IGaugeRatioProvider gauge)
		{
			double ratio = gauge.getRatio();
			if (ratio > MIN_VISIBLE)
			{
				return (float) ratio;
			}
		}

		if (target instanceof TileEntityScanner scanner && scanner.duration > 0 && scanner.progress > 0)
		{
			return (float) scanner.progress / (float) scanner.duration;
		}

		if (target instanceof TileEntityReplicator replicator && replicator.patternUu > 0.0 && replicator.uuProcessed > 0.0)
		{
			return (float) Math.min(1.0, replicator.uuProcessed / replicator.patternUu);
		}

		return 0.0F;
	}
}
