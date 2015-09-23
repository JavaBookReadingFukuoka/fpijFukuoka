import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Stocks100 {
    static class Tickers {
        public static final List<String> symbols = Arrays.asList(
                "AMD", "HPQ", "IBM", "TXN", "VMW", "XRX", "AAPL", "ADBE",
                "AMZN", "CRAY", "CSCO", "SNE", "GOOG", "INTC", "INTU",
                "MSFT", "ORCL", "TIBX", "VRSN", "YHOO");
    }
    static class YahooFinances {
        public static BigDecimal getPrice(final String ticker) {
            try {
                final URL url = new URL("http://ichart.finance.yahoo.com/table.csv?s=" + ticker);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                final String data = reader.lines().skip(1).findFirst().get();
                final String[] dataItems = data.split(",");
                return new BigDecimal(dataItems[dataItems.length - 1]);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public static void main(String ... args) {
        final BigDecimal HUNDRED = new BigDecimal("100");
        System.out.println("Stocks priced over $100 are" +
                Tickers.symbols
                       .stream()
                       .filter(symbol -> YahooFinances.getPrice(symbol).compareTo(HUNDRED) > 0)
                       .sorted()
                       .collect(Collectors.joining(", ")));
    }
}
