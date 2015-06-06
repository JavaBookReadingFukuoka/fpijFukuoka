package designing.fpij;

import java.awt.Color;

public class Brighter extends Filter {

    public Brighter() {
        super(NO_FILTER);
    }

    public Brighter(Filter filter) {
        super(filter);
    }

    @Override
    public Color apply(Color inputColor) {
        return filter.apply(inputColor).brighter();
    }

}
