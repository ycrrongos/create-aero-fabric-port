package foundry.veil.impl.client.render.pipeline;

import org.jetbrains.annotations.ApiStatus;

/**
 * Evaluates the tiny arithmetic expressions used for framebuffer sizes, e.g. <code>q.screen_width * 0.5</code>.
 */
@ApiStatus.Internal
public final class VeilSizeExpression {

    private final String source;

    public VeilSizeExpression(String source) {
        this.source = source.trim();
    }

    public static VeilSizeExpression constant(double value) {
        return new VeilSizeExpression(Double.toString(value));
    }

    public String source() {
        return this.source;
    }

    public int evaluate(int screenWidth, int screenHeight) {
        Parser parser = new Parser(this.source.toLowerCase().replace("query.", "q."), screenWidth, screenHeight);
        double value = parser.parseExpression();
        parser.skipWhitespace();
        if (parser.pos != parser.input.length()) {
            throw new IllegalArgumentException("Unexpected token in size expression '" + this.source + "' at " + parser.pos);
        }
        return Math.max(1, (int) Math.round(value));
    }

    private static final class Parser {

        private final String input;
        private final int screenWidth;
        private final int screenHeight;
        private int pos;

        private Parser(String input, int screenWidth, int screenHeight) {
            this.input = input;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
        }

        private void skipWhitespace() {
            while (this.pos < this.input.length() && Character.isWhitespace(this.input.charAt(this.pos))) {
                this.pos++;
            }
        }

        private boolean consume(char c) {
            this.skipWhitespace();
            if (this.pos < this.input.length() && this.input.charAt(this.pos) == c) {
                this.pos++;
                return true;
            }
            return false;
        }

        private double parseExpression() {
            double value = this.parseTerm();
            while (true) {
                if (this.consume('+')) {
                    value += this.parseTerm();
                } else if (this.consume('-')) {
                    value -= this.parseTerm();
                } else {
                    return value;
                }
            }
        }

        private double parseTerm() {
            double value = this.parseFactor();
            while (true) {
                if (this.consume('*')) {
                    value *= this.parseFactor();
                } else if (this.consume('/')) {
                    value /= this.parseFactor();
                } else {
                    return value;
                }
            }
        }

        private double parseFactor() {
            this.skipWhitespace();
            if (this.consume('-')) {
                return -this.parseFactor();
            }
            if (this.consume('(')) {
                double value = this.parseExpression();
                if (!this.consume(')')) {
                    throw new IllegalArgumentException("Missing ')' in size expression " + this.input);
                }
                return value;
            }
            int start = this.pos;
            while (this.pos < this.input.length() && (Character.isLetterOrDigit(this.input.charAt(this.pos)) || this.input.charAt(this.pos) == '.' || this.input.charAt(this.pos) == '_')) {
                this.pos++;
            }
            String token = this.input.substring(start, this.pos);
            return switch (token) {
                case "q.screen_width" -> this.screenWidth;
                case "q.screen_height" -> this.screenHeight;
                default -> {
                    try {
                        yield Double.parseDouble(token);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Unknown value '" + token + "' in size expression " + this.input);
                    }
                }
            };
        }
    }
}
