package Chapter05.resources;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

class TestHelper {

    public static <X extends Throwable> Throwable assertThrows(
            final Class<X> exceptionClass, final Runnable block) {

        try {
            block.run();
        } catch (Throwable ex) {
            if (exceptionClass.isInstance(ex)) {
                return ex;
            }
        }

        fail("Failed to throw expected exception");
        return null;
    }
}

public class RodCutterTest_5_4_3 {

    private RodCutter rodCutter;
    private int prices;

    @Before
    public void Before() {
        rodCutter = new RodCutter();
        prices = 108;
    }

    @Test
    public void ConciseExceptionTest() {
        rodCutter.setPrices(prices);
        TestHelper.assertThrows(RodCutterException.class, () -> rodCutter.maxProfit(0));
    }
}