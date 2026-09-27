package net.minecraft.world;

/** Compile stub approximating removed InteractionResultHolder (1.21.11 Item.use returns InteractionResult). */
public record InteractionResultHolder<T>(InteractionResult result, T object) {
    public static <T> InteractionResultHolder<T> success(T obj) { return new InteractionResultHolder<>(InteractionResult.SUCCESS, obj); }
    public static <T> InteractionResultHolder<T> fail(T obj) { return new InteractionResultHolder<>(InteractionResult.FAIL, obj); }
    public static <T> InteractionResultHolder<T> pass(T obj) { return new InteractionResultHolder<>(InteractionResult.PASS, obj); }
    public static <T> InteractionResultHolder<T> consume(T obj) { return new InteractionResultHolder<>(InteractionResult.CONSUME, obj); }
    public InteractionResult getResult() { return result; }
    public T getObject() { return object; }
}
