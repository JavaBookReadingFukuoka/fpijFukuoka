package Chapter05.resources;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RodCutterTest_5_4_1 {

    private RodCutter rodCutter;
    private int prices;

    @Before
    public void Before() {
        rodCutter = new RodCutter();
        prices = 108;
    }

    @Test
    public void VerboseExceptionTest() {
        rodCutter.setPrices(prices);
        try {
            rodCutter.maxProfit(0);
            fail("Expected exception for zero length");
        } catch (RodCutterException ex) {
            assertTrue("expected", true);
        }
    }
}

class RodCutterException extends RuntimeException {
}

class RodCutter {

    private int prices;

    public void setPrices(int prices) {
        this.prices = prices;
    }

    public int maxProfit(int prices) {
        if (prices == 0) throw new RodCutterException();
        return 0;
    }
}