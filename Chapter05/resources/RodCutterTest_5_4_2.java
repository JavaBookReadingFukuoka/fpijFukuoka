package Chapter05.resources;

import org.junit.Before;
import org.junit.Test;

public class RodCutterTest_5_4_2 {

    private RodCutter rodCutter;
    private int prices;

    @Before
    public void Before() {
        rodCutter = new RodCutter();
        prices = 108;
    }

    @Test(expected = RodCutterException.class)
    public void VerboseExceptionTest() throws RodCutterException {
        rodCutter.setPrices(prices);
        rodCutter.maxProfit(0);
    }
}