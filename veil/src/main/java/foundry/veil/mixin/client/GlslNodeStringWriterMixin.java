package foundry.veil.mixin.client;

import io.github.ocelot.glslprocessor.api.node.expression.GlslCompareNode;
import io.github.ocelot.glslprocessor.api.node.expression.GlslConditionalNode;
import io.github.ocelot.glslprocessor.api.visitor.GlslNodeStringWriter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * glsl-processor 0.2.3 writes conditional ({@code a ? b : c}) and comparison nodes without parentheses. Operation nodes
 * wrap themselves in parentheses but not their operands, so {@code (a ? b : c) * d} is written as
 * {@code (a ? b : c * d)}, silently changing the meaning of every shader Veil rewrites. This makes both nodes wrap
 * themselves in parentheses like every other expression node does.
 */
@Mixin(value = GlslNodeStringWriter.class, remap = false)
public abstract class GlslNodeStringWriterMixin {

    @Shadow
    @Final
    private StringBuilder builder;

    @Shadow
    public abstract GlslNodeStringWriter inline();

    @Shadow
    private void addIndent() {
        throw new AssertionError();
    }

    @Shadow
    public abstract void trimSemicolon();

    @Shadow
    private void accept(final CharSequence text, final boolean inline, final boolean semicolon) {
        throw new AssertionError();
    }

    /**
     * @author Veil port
     * @reason Parenthesize comparisons so they keep their precedence inside other expressions
     */
    @Overwrite
    public void visitCompare(final GlslCompareNode node) {
        final GlslNodeStringWriter inline = this.inline();
        this.addIndent();
        this.builder.append('(');
        node.getFirst().visit(inline);
        this.trimSemicolon();
        this.builder.append(' ');
        this.builder.append(node.getOperand().getDelimiter());
        this.builder.append(' ');
        node.getSecond().visit(inline);
        this.trimSemicolon();
        this.accept(")", false, false);
    }

    /**
     * @author Veil port
     * @reason Parenthesize conditionals so they keep their precedence inside other expressions
     */
    @Overwrite
    public void visitCondition(final GlslConditionalNode node) {
        final GlslNodeStringWriter inline = this.inline();
        this.addIndent();
        this.builder.append('(');
        node.getCondition().visit(inline);
        this.trimSemicolon();
        this.builder.append(" ? ");
        node.getFirst().visit(inline);
        this.trimSemicolon();
        this.builder.append(" : ");
        node.getSecond().visit(inline);
        this.trimSemicolon();
        this.accept(")", false, false);
    }
}
