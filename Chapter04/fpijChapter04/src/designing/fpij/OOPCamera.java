package designing.fpij;

import java.awt.Color;
import java.util.function.Consumer;

public class OOPCamera {

    private Filter filter;

    public OOPCamera() {
    }
    
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    public Color capture(final Color inputColor) {
        final Color processedColor = filter.apply(inputColor);
        return processedColor;
    }
    
    public static void main(String[] args) {
        OOPCamera camera = new OOPCamera();
        final Consumer<String> printCaptured
                = (filterInfo)
                -> System.out.println(String.format(
                                "with %s: %s",
                                filterInfo,
                                camera.capture(new Color(200, 100, 200))));
        
        camera.setFilter(new Darker(new Brighter()));
        printCaptured.accept("brighter & darker filter");
    }
}
