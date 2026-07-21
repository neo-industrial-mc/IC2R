package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public abstract class BlockEntity extends net.neoforged.neoforge.attachment.AttachmentHolder implements net.neoforged.neoforge.common.extensions.IBlockEntityExtension {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Deprecated // Neo: always use getType()
    private final BlockEntityType<?> type;
    @Nullable
    protected Level level;
    protected final BlockPos worldPosition;
    protected boolean remove;
    private BlockState blockState;
    private DataComponentMap components = DataComponentMap.EMPTY;
    @Nullable
    private CompoundTag customPersistentData;

    public BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        this.type = type;
        this.worldPosition = pos.immutable();
        this.validateBlockState(blockState);
        this.blockState = blockState;
    }

    private void validateBlockState(BlockState p_353132_) {
        if (!this.isValidBlockState(p_353132_)) {
            throw new IllegalStateException("Invalid block entity " + this.getNameForReporting() + " state at " + this.worldPosition + ", got " + p_353132_);
        }
    }

    public boolean isValidBlockState(BlockState p_353131_) {
        return this.getType().isValid(p_353131_); // Neo: use getter so correct type is checked for modded subclasses
    }

    public static BlockPos getPosFromTag(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Nullable
    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("NeoForgeData", net.minecraft.nbt.Tag.TAG_COMPOUND)) this.customPersistentData = tag.getCompound("NeoForgeData");
        if (tag.contains(ATTACHMENTS_NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) deserializeAttachments(registries, tag.getCompound(ATTACHMENTS_NBT_KEY));
    }

    public final void loadWithComponents(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
        BlockEntity.ComponentHelper.COMPONENTS_CODEC
            .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
            .resultOrPartial(p_337987_ -> LOGGER.warn("Failed to load components: {}", p_337987_))
            .ifPresent(p_337995_ -> this.components = p_337995_);
    }

    public final void loadCustomOnly(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.customPersistentData != null) tag.put("NeoForgeData", this.customPersistentData.copy());
        var attachmentsTag = serializeAttachments(registries);
        if (attachmentsTag != null) tag.put(ATTACHMENTS_NBT_KEY, attachmentsTag);
    }

    public final CompoundTag saveWithFullMetadata(HolderLookup.Provider registries) {
        CompoundTag compoundtag = this.saveWithoutMetadata(registries);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithId(HolderLookup.Provider registries) {
        CompoundTag compoundtag = this.saveWithoutMetadata(registries);
        this.saveId(compoundtag);
        return compoundtag;
    }

    public final CompoundTag saveWithoutMetadata(HolderLookup.Provider registries) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, registries);
        BlockEntity.ComponentHelper.COMPONENTS_CODEC
            .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this.components)
            .resultOrPartial(p_337988_ -> LOGGER.warn("Failed to save components: {}", p_337988_))
            .ifPresent(p_337994_ -> compoundtag.merge((CompoundTag)p_337994_));
        return compoundtag;
    }

    public final CompoundTag saveCustomOnly(HolderLookup.Provider registries) {
        CompoundTag compoundtag = new CompoundTag();
        this.saveAdditional(compoundtag, registries);
        return compoundtag;
    }

    public final CompoundTag saveCustomAndMetadata(HolderLookup.Provider registries) {
        CompoundTag compoundtag = this.saveCustomOnly(registries);
        this.saveMetadata(compoundtag);
        return compoundtag;
    }

    private void saveId(CompoundTag tag) {
        ResourceLocation resourcelocation = BlockEntityType.getKey(this.getType());
        if (resourcelocation == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            tag.putString("id", resourcelocation.toString());
        }
    }

    public static void addEntityType(CompoundTag tag, BlockEntityType<?> entityType) {
        tag.putString("id", BlockEntityType.getKey(entityType).toString());
    }

    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag compoundtag = this.saveCustomOnly(registries);
        this.removeComponentsFromTag(compoundtag);
        BlockItem.setBlockEntityData(stack, this.getType(), compoundtag);
        stack.applyComponents(this.collectComponents());
    }

    private void saveMetadata(CompoundTag tag) {
        this.saveId(tag);
        tag.putInt("x", this.worldPosition.getX());
        tag.putInt("y", this.worldPosition.getY());
        tag.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static BlockEntity loadStatic(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries) {
        String s = tag.getString("id");
        ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
        if (resourcelocation == null) {
            LOGGER.error("Block entity has invalid type: {}", s);
            return null;
        } else {
            return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(resourcelocation).map(p_155240_ -> {
                try {
                    return p_155240_.create(pos, state);
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to create block entity {}", s, throwable);
                    return null;
                }
            }).map(p_337992_ -> {
                try {
                    p_337992_.loadWithComponents(tag, registries);
                    return (BlockEntity)p_337992_;
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to load data for block entity {}", s, throwable);
                    return null;
                }
            }).orElseGet(() -> {
                LOGGER.warn("Skipping BlockEntity with id {}", s);
                return null;
            });
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }
    }

    protected static void setChanged(Level level, BlockPos pos, BlockState state) {
        level.blockEntityChanged(pos);
        if (!state.isAir()) {
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    public BlockPos getBlockPos() {
        return this.worldPosition;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return new CompoundTag();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
        this.invalidateCapabilities();
        requestModelDataUpdate();
    }

    public void clearRemoved() {
        this.remove = false;
        // Neo: invalidate capabilities on block entity placement
        invalidateCapabilities();
    }

    public boolean triggerEvent(int id, int type) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportCategory reportCategory) {
        reportCategory.setDetail("Name", this::getNameForReporting);
        if (this.level != null) {
            CrashReportCategory.populateBlockDetails(reportCategory, this.level, this.worldPosition, this.getBlockState());
            CrashReportCategory.populateBlockDetails(reportCategory, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
        }
    }

    private String getNameForReporting() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Override
    public CompoundTag getPersistentData() {
        if (this.customPersistentData == null)
            this.customPersistentData = new CompoundTag();
        return this.customPersistentData;
    }

    @Override
    @Nullable
    public final <T> T setData(net.neoforged.neoforge.attachment.AttachmentType<T> type, T data) {
        setChanged();
        return super.setData(type, data);
    }

    @Override
    @Nullable
    public final <T> T removeData(net.neoforged.neoforge.attachment.AttachmentType<T> type) {
        setChanged();
        return super.removeData(type);
    }

    @Override
    public final void syncData(net.neoforged.neoforge.attachment.AttachmentType<?> type) {
        net.neoforged.neoforge.attachment.AttachmentSync.syncBlockEntityUpdate(this, type);
    }

    @Deprecated
    public void setBlockState(BlockState blockState) {
        this.validateBlockState(blockState);
        this.blockState = blockState;
    }

    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
    }

    public final void applyComponentsFromItemStack(ItemStack stack) {
        this.applyComponents(stack.getPrototype(), stack.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap components, DataComponentPatch patch) {
        final Set<DataComponentType<?>> set = new HashSet<>();
        set.add(DataComponents.BLOCK_ENTITY_DATA);
        final DataComponentMap datacomponentmap = PatchedDataComponentMap.fromPatch(components, patch);
        this.applyImplicitComponents(new BlockEntity.DataComponentInput() {
            @Nullable
            @Override
            public <T> T get(DataComponentType<T> p_338266_) {
                set.add(p_338266_);
                return datacomponentmap.get(p_338266_);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> p_338358_, T p_338352_) {
                set.add(p_338358_);
                return datacomponentmap.getOrDefault(p_338358_, p_338352_);
            }
        });
        DataComponentPatch datacomponentpatch = patch.forget(set::contains);
        this.components = datacomponentpatch.split().added();
    }

    protected void collectImplicitComponents(DataComponentMap.Builder components) {
    }

    @Deprecated
    public void removeComponentsFromTag(CompoundTag tag) {
    }

    public final DataComponentMap collectComponents() {
        DataComponentMap.Builder datacomponentmap$builder = DataComponentMap.builder();
        datacomponentmap$builder.addAll(this.components);
        this.collectImplicitComponents(datacomponentmap$builder);
        return datacomponentmap$builder.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap components) {
        this.components = components;
    }

    @Nullable
    public static Component parseCustomNameSafe(String customName, HolderLookup.Provider registries) {
        try {
            return Component.Serializer.fromJson(customName, registries);
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse custom name from string '{}', discarding", customName, exception);
            return null;
        }
    }

    static class ComponentHelper {
        public static final Codec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).codec();

        private ComponentHelper() {
        }
    }

    protected interface DataComponentInput {
        @Nullable
        <T> T get(DataComponentType<T> component);

        <T> T getOrDefault(DataComponentType<? extends T> component, T defaultValue);

        // Neo: Utility for modded component types, to remove the need to invoke '.value()'
        @Nullable
        default <T> T get(java.util.function.Supplier<? extends DataComponentType<T>> componentType) {
            return get(componentType.get());
        }

        default <T> T getOrDefault(java.util.function.Supplier<? extends DataComponentType<T>> componentType, T value) {
            return getOrDefault(componentType.get(), value);
        }
    }
}
