package designing.fpij;

import java.awt.Color;

public abstract class Filter {

    public static final Filter NO_FILTER = new Filter(Filter.NO_FILTER) {
        @Override
        public Color apply(Color inputColor) {
            return inputColor;
        }
    };

    protected final Filter filter;

    protected Filter(Filter filter) {
        this.filter = filter;
    }

    public abstract Color apply(Color inputColor);
}
