package designing.fpij;

import java.math.BigDecimal;
import java.util.function.Function;

public class CalculateNAV {
    private final Function<String, BigDecimal> priceFinder;

    public CalculateNAV(Function<String, BigDecimal> priceFinder) {
        this.priceFinder = priceFinder;
    }
    
    public BigDecimal computeStockWorth(final String ticker, final int shares) {
        return priceFinder.apply(ticker).multiply(BigDecimal.valueOf(shares));
    }
    
    public static void main(String[] args) {
        final CalculateNAV calculateNav = new CalculateNAV(YahooFinance::getPrice);
        System.out.println(String.format("100 shares of Google worth: $%.2f", calculateNav.computeStockWorth("GOOG", 100)));
    }
}
