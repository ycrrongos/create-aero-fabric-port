package net.neoforged.neoforge.client.model.generators;
public class ConfiguredModel {
  public ConfiguredModel(Object... a) {}
  public static Builder builder() { return new Builder(); }
  public static class Builder {
    public Builder modelFile(Object f) { return this; }
    public Builder rotationX(int v) { return this; }
    public Builder rotationY(int v) { return this; }
    public ConfiguredModel[] build() { return new ConfiguredModel[0]; }
  }
}
