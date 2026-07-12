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
 * Progress is only shown while {@code > 0}. Client display (visibility, text, colors)
 * is controlled by {@code ic2-client.toml} → {@code [jade.progress]}.
 */
public enum Ic2ProgressProvider implements IServerExtensionProvider<Object, CompoundTag>, IClientExtensionProvider<CompoundTag, ProgressView>
{
	INSTANCE;

	public static final ResourceLocation UID = IC2.getIdentifier("progress");
	private static final float MIN_VISIBLE = 1.0E-4F;

	private static final String KEY_CURRENT = "ic2Cur";
	private static final String KEY_MAX = "ic2Max";

	@Override
	public ResourceLocation getUid()
	{
		return UID;
	}

	@Override
	public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel world, Object target, boolean showDetails)
	{
		ProgressSnapshot snap = resolve(target);
		if (snap == null || snap.ratio <= MIN_VISIBLE)
		{
			return null;
		}

		float progress = Math.min(1.0F, snap.ratio);
		ViewGroup<CompoundTag> group = new ViewGroup<>(List.of(ProgressView.create(progress)));
		if (snap.max > 0L)
		{
			group.getExtraData().putLong(KEY_CURRENT, snap.current);
			group.getExtraData().putLong(KEY_MAX, snap.max);
		}
		return List.of(group);
	}

	@Override
	public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups)
	{
		if (!JadeConfigHelper.progressMode().isVisible(accessor.showDetails()))
		{
			return List.of();
		}

		return ClientViewGroup.map(groups, ProgressView::read, (serverGroup, clientGroup) ->
		{
			CompoundTag extra = serverGroup.getExtraData();
			long current = extra.getLong(KEY_CURRENT);
			long max = extra.getLong(KEY_MAX);

			for (ProgressView view : clientGroup.views)
			{
				view.style = JadeConfigHelper.progressStyle();
				view.text = JadeConfigHelper.formatProgressText(view.progress, current, max);
			}
		});
	}

	/**
	 * @return progress snapshot, or {@code null} when nothing to display
	 */
	static ProgressSnapshot resolve(Object target)
	{
		if (target instanceof Ic2TileEntity te && te.hasComponent(Process.class))
		{
			Process process = te.getComponent(Process.class);
			float ratio = (float) process.getProgressRatio();
			if (ratio > MIN_VISIBLE)
			{
				return new ProgressSnapshot(ratio, process.getProgress(), process.operationDuration);
			}
		}

		if (target instanceof TileEntityStandardMachine<?, ?, ?> machine)
		{
			float progress = machine.getProgress();
			if (progress > MIN_VISIBLE)
			{
				int max = Math.max(1, machine.operationLength);
				long current = Math.round((double) progress * max);
				return new ProgressSnapshot(progress, current, max);
			}
		}

		if (target instanceof IGuiValueProvider guiValues)
		{
			try
			{
				double progress = guiValues.getGuiValue("progress");
				if (progress > MIN_VISIBLE)
				{
					return new ProgressSnapshot((float) progress, 0L, 0L);
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
				return new ProgressSnapshot((float) ratio, 0L, 0L);
			}
		}

		if (target instanceof TileEntityScanner scanner && scanner.duration > 0 && scanner.progress > 0)
		{
			return new ProgressSnapshot(
				(float) scanner.progress / (float) scanner.duration,
				scanner.progress,
				scanner.duration
			);
		}

		if (target instanceof TileEntityReplicator replicator && replicator.patternUu > 0.0 && replicator.uuProcessed > 0.0)
		{
			// Scale UU buckets to milli-units so fraction labels stay readable as integers.
			long current = Math.round(replicator.uuProcessed * 1000.0);
			long max = Math.round(replicator.patternUu * 1000.0);
			return new ProgressSnapshot(
				(float) Math.min(1.0, replicator.uuProcessed / replicator.patternUu),
				current,
				Math.max(1L, max)
			);
		}

		return null;
	}

	record ProgressSnapshot(float ratio, long current, long max)
	{
	}
}
