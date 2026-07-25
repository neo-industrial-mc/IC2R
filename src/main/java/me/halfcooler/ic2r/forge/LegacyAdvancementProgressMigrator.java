package me.halfcooler.ic2r.forge;

import com.google.gson.*;
import me.halfcooler.ic2r.core.IC2R;
import me.halfcooler.ic2r.core.RemapService;
import me.halfcooler.ic2r.core.util.LogCategory;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Migrates player advancement progress saved under pre-rebrand IDs.
 * <p>
 * Registry entries are remapped via {@link RemapService} ({@code ic2:*} → {@code ic2r:*}).
 * Advancement progress is stored separately in {@code <world>/advancements/<uuid>.json} and is
 * <em>not</em> covered by registry aliases. When Minecraft loads progress for an unknown
 * advancement ID it logs {@code Ignored advancement '…' - it doesn't exist anymore?} and drops
 * the entry permanently on the next save — which is exactly the reported 1.20.1 → 1.21.1
 * “mod progress wiped, vanilla intact” symptom for pre-20.1.40 / pre-rebrand saves.
 * <p>
 * Mappings:
 * <ul>
 *   <li>{@code ic2:ic2/…} → {@code ic2r:ic2r/…} (tree advancements; path prefix rebrand)</li>
 *   <li>{@code ic2:recipes/…} → {@code ic2r:recipes/…} (recipe-unlock advancements)</li>
 *   <li>Criterion keys {@code ic2:…} → {@code ic2r:…}</li>
 *   <li>{@code build_coal_diamond}: criterion {@code *:industrial_diamond} → {@code impossible}
 *       (industrial diamond removed; advancement is now code-granted)</li>
 * </ul>
 * Runs once per player file on server start, before any player joins (file rewrite).
 */
public final class LegacyAdvancementProgressMigrator
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private static final String DATA_VERSION_KEY = "DataVersion";

	/**
	 * Path segment used under both namespaces for the main IC2R tree.
	 */
	public static final String TREE_PATH_PREFIX_LEGACY = "ic2/";
	public static final String TREE_PATH_PREFIX_CURRENT = "ic2r/";

	/**
	 * Advancement path (no namespace) for coal→diamond challenge.
	 */
	public static final String COAL_DIAMOND_PATH = "ic2r/build_generator/build_compressor/build_coal_diamond";

	public static final String CRITERION_INDUSTRIAL_DIAMOND_LEGACY = "ic2:industrial_diamond";
	public static final String CRITERION_INDUSTRIAL_DIAMOND = "ic2r:industrial_diamond";
	public static final String CRITERION_IMPOSSIBLE = "impossible";

	private LegacyAdvancementProgressMigrator()
	{
	}

	/**
	 * Remap a single advancement id from a progress file.
	 * Non-IC2R namespaces are returned unchanged.
	 */
	public static ResourceLocation remapAdvancementId(ResourceLocation id)
	{
		Objects.requireNonNull(id, "id");
		if (!RemapService.LEGACY_NAMESPACE.equals(id.getNamespace()))
		{
			return id;
		}

		String path = id.getPath();
		if (path.startsWith(TREE_PATH_PREFIX_LEGACY))
		{
			path = TREE_PATH_PREFIX_CURRENT + path.substring(TREE_PATH_PREFIX_LEGACY.length());
		}
		return ResourceLocation.fromNamespaceAndPath(RemapService.CURRENT_NAMESPACE, path);
	}

	/**
	 * Remap a criterion key stored under an advancement progress entry.
	 *
	 * @param advancementPath path of the <em>already remapped</em> advancement id
	 * @param criterionKey    original criterion name from the save
	 */
	public static String remapCriterionKey(String advancementPath, String criterionKey)
	{
		Objects.requireNonNull(advancementPath, "advancementPath");
		Objects.requireNonNull(criterionKey, "criterionKey");

		if (COAL_DIAMOND_PATH.equals(advancementPath)
			&& (CRITERION_INDUSTRIAL_DIAMOND.equals(criterionKey)
			|| CRITERION_INDUSTRIAL_DIAMOND_LEGACY.equals(criterionKey)))
		{
			return CRITERION_IMPOSSIBLE;
		}

		if (criterionKey.startsWith(RemapService.LEGACY_NAMESPACE + ":"))
		{
			return RemapService.CURRENT_NAMESPACE + ":"
				+ criterionKey.substring(RemapService.LEGACY_NAMESPACE.length() + 1);
		}
		return criterionKey;
	}

	/**
	 * Migrate one player advancement progress JSON object in memory.
	 *
	 * @return empty if nothing changed; otherwise the rewritten root object
	 */
	public static Optional<JsonObject> migrateProgressJson(JsonObject root)
	{
		Objects.requireNonNull(root, "root");

		JsonElement dataVersion = root.get(DATA_VERSION_KEY);
		Map<String, JsonObject> migrated = new LinkedHashMap<>();
		boolean changed = false;

		for (Map.Entry<String, JsonElement> entry : root.entrySet())
		{
			String key = entry.getKey();
			if (DATA_VERSION_KEY.equals(key))
			{
				continue;
			}
			if (!entry.getValue().isJsonObject())
			{
				continue;
			}

			ResourceLocation originalId = ResourceLocation.tryParse(key);
			if (originalId == null)
			{
				migrated.put(key, entry.getValue().getAsJsonObject());
				continue;
			}

			ResourceLocation remappedId = remapAdvancementId(originalId);
			if (!remappedId.equals(originalId))
			{
				changed = true;
			}

			JsonObject progressIn = entry.getValue().getAsJsonObject();
			JsonObject progressOut = remapProgressObject(remappedId.getPath(), progressIn);
			if (progressOut != progressIn)
			{
				changed = true;
			}

			String outKey = remappedId.toString();
			JsonObject existing = migrated.get(outKey);
			if (existing == null)
			{
				migrated.put(outKey, progressOut);
			} else
			{
				JsonObject merged = mergeProgress(existing, progressOut);
				if (merged != existing)
				{
					changed = true;
				}
				migrated.put(outKey, merged);
			}
		}

		if (!changed)
		{
			return Optional.empty();
		}

		JsonObject out = new JsonObject();
		for (Map.Entry<String, JsonObject> e : migrated.entrySet())
		{
			out.add(e.getKey(), e.getValue());
		}
		if (dataVersion != null)
		{
			out.add(DATA_VERSION_KEY, dataVersion);
		}
		return Optional.of(out);
	}

	/**
	 * Remap criterion keys inside one progress object. Returns the same instance when unchanged.
	 */
	static JsonObject remapProgressObject(String advancementPath, JsonObject progress)
	{
		if (!progress.has("criteria") || !progress.get("criteria").isJsonObject())
		{
			return progress;
		}

		JsonObject criteriaIn = progress.getAsJsonObject("criteria");
		JsonObject criteriaOut = new JsonObject();
		boolean criteriaChanged = false;

		for (Map.Entry<String, JsonElement> c : criteriaIn.entrySet())
		{
			String remapped = remapCriterionKey(advancementPath, c.getKey());
			if (!remapped.equals(c.getKey()))
			{
				criteriaChanged = true;
			}
			if (!criteriaOut.has(remapped))
			{
				criteriaOut.add(remapped, c.getValue());
			}
		}

		if (!criteriaChanged)
		{
			return progress;
		}

		JsonObject out = progress.deepCopy();
		out.add("criteria", criteriaOut);
		return out;
	}

	/**
	 * Merge two progress objects for the same advancement (union of completed criteria).
	 */
	static JsonObject mergeProgress(JsonObject a, JsonObject b)
	{
		JsonObject out = a.deepCopy();
		if (!b.has("criteria") || !b.get("criteria").isJsonObject())
		{
			return out;
		}
		JsonObject criteria = out.has("criteria") && out.get("criteria").isJsonObject()
			? out.getAsJsonObject("criteria")
			: new JsonObject();
		boolean changed = false;
		for (Map.Entry<String, JsonElement> e : b.getAsJsonObject("criteria").entrySet())
		{
			if (!criteria.has(e.getKey()))
			{
				criteria.add(e.getKey(), e.getValue());
				changed = true;
			}
		}
		if (!changed && out.has("criteria"))
		{
			return a;
		}
		out.add("criteria", criteria);
		if (b.has("done") && b.get("done").isJsonPrimitive() && b.getAsJsonPrimitive("done").isBoolean())
		{
			boolean doneA = out.has("done") && out.get("done").isJsonPrimitive() && out.getAsJsonPrimitive("done").getAsBoolean();
			boolean doneB = b.getAsJsonPrimitive("done").getAsBoolean();
			out.add("done", new JsonPrimitive(doneA || doneB));
		}
		return out;
	}

	/**
	 * Rewrite every {@code *.json} under the player advancements directory if migration applies.
	 *
	 * @return number of files rewritten
	 */
	public static int migrateDirectory(Path advancementsDir)
	{
		Objects.requireNonNull(advancementsDir, "advancementsDir");
		if (!Files.isDirectory(advancementsDir))
		{
			return 0;
		}

		int rewritten = 0;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(advancementsDir, "*.json"))
		{
			for (Path file : stream)
			{
				if (migrateFile(file))
				{
					rewritten++;
				}
			}
		} catch (IOException ex)
		{
			IC2R.log.warn(LogCategory.Resource, ex, "Failed to scan player advancements dir %s", advancementsDir);
		}

		if (rewritten > 0)
		{
			IC2R.log.info(
				LogCategory.Resource,
				"Migrated legacy advancement progress in %d player file(s) under %s",
				rewritten,
				advancementsDir
			);
		}
		return rewritten;
	}

	/**
	 * @return true if the file was rewritten
	 */
	public static boolean migrateFile(Path file)
	{
		Objects.requireNonNull(file, "file");
		if (!Files.isRegularFile(file))
		{
			return false;
		}

		JsonObject root;
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8))
		{
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject())
			{
				return false;
			}
			root = parsed.getAsJsonObject();
		} catch (Exception ex)
		{
			IC2R.log.warn(LogCategory.Resource, ex, "Could not parse player advancements %s", file);
			return false;
		}

		Optional<JsonObject> migrated = migrateProgressJson(root);
		if (migrated.isEmpty())
		{
			return false;
		}

		try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8))
		{
			GSON.toJson(migrated.get(), writer);
		} catch (IOException ex)
		{
			IC2R.log.warn(LogCategory.Resource, ex, "Could not write migrated advancements %s", file);
			return false;
		}
		return true;
	}
}
