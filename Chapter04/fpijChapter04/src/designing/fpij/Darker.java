package designing.fpij;

import java.awt.Color;

public class Darker extends Filter {

    public Darker() {
        super(NO_FILTER);
    }

    public Darker(Filter filter) {
        super(filter);
    }

    @Override
    public Color apply(Color inputColor) {
        return filter.apply(inputColor).darker();
    }

}
