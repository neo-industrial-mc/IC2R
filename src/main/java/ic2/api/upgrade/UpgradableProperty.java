package ic2.api.upgrade;

public enum UpgradableProperty {
  Processing,
  Augmentable,
  RedstoneSensitive,
  Transformer,
  EnergyStorage,
  ItemConsuming,
  ItemProducing,
  FluidConsuming,
  FluidProducing,
  RemotelyAccessible,
  /** Advanced miner: accepts mining filter upgrade modules. */
  MiningFilter
}
