# ４章　ラムダ式で設計する

## 4.1 ラムダ式を使った関心の分離

* ラムダ式を使って新たなクラスを作ることなくメソッドレベルでの関心の分離を行うことができる。
* StrategyのようなパターンをJavaで実装する場合は通常インタフェースとクラスを使用するが、ラムダ式は一歩進んだ設計手法を与えてくれる。

### 間違った設計

証券(BOND)と株式(STOCK)を表すことができる資産(Asset)クラスがある。
その価値を合計するメソッド(totalAssetValues)を資産ユーティリティ(AssetUtil)クラスに定義した。

その後、証券(BOND)だけの価値を合計するメソッド(totalBondValues)と株式(STOCK)だけの価値を合計するメソッド(totalStockValues)が必要となりそれぞれ定義した。

結果としてできたクラス定義はこんな感じ。

```java
public final class AssetUtil {
    public int totalAssetValues(List<Asset> assets) {/* 省略 */}
    public int totalBondValues(List<Asset> assets) {/* 省略 */}
    public int totalStockValues(List<Asset> assets) {/* 省略 */}
}
```

使い方は、

```java
System.out.println("Total of all assets: " + AssetUtil.totalAssetValues(assets));
System.out.println("Total of bonds: " + AssetUtil.totalBondValues(assets));
System.out.println("Total of stocks: " + AssetUtil.totalStockValues(assets));
```

本物のJava使いならこう書くはず。

```java
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets, AssetSelector selector) {}
}

interface AssetSelector {
     boolean test(Asset asset);
}

System.out.println("Total of all assets: " + AssetUtil.totalAssetValues(assets, new AssetSelector() {
    @Override public boolean test(Asset asset) {
        return true;
    }
}));
System.out.println("Total of bonds: " + AssetUtil.totalAssetValues(assets, new AssetSelector() {
    @Override public boolean test(Asset asset) {
        return asset.getType() == Asset.AssetType.BOND;
    }
}));
System.out.println("Total of stocks: " + AssetUtil.totalAssetValues(assets, new AssetSelector() {
    @Override public boolean test(Asset asset) {
        return asset.getType() == Asset.AssetType.STOCK;
    }
}));
```

AssetSelectorインタフェースを使えば「何を合計するか」を分離することができる。いわゆるStrategyパターンだ。


### 正しい設計

三つのメソッドをインタフェースを使って一つにし、「何を合計するか」を分離したところまでは良かったのですが、明らかにラムダ式の出番。AssetSelectorは定義済みのjava.util.function.Predicateインタフェースを再利用し、次のように書くこと。

```java
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets, Predicate<Asset> assetSelector) {/* 省略 */}
}

System.out.println("Total of all assets: " + AssetUtil.totalAssetValues(assets, asset -> true));
System.out.println("Total of bonds: " + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.BOND));
System.out.println("Total of stocks: " + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.STOCK));
```

### その他

* このパターンは高階関数を使う開発者が選択ロジックを提供する必要がある。
* しかし、選択ロジックは変数に格納しておき任意の時点で再利用できる。


## 4.2 ラムダ式を使った委譲

* 責任の一部を他のクラスに委譲するのではなく、ラムダ式やメソッド参照に委譲できる。

### 関数型インタフェースを使って委譲部分を作成

```java
public class CalculateNAV {
    private Function<String, BigDecimal> priceFinder;  // 委譲先

    public CalculateNAV(Function<String, BigDecimal> priceFinder) {
        this.priceFinder = priceFinder;
    }
    
    public BigDecimal computeStockWorth(final String ticker, final int shares) {
        return priceFinder.apply(ticker).multiply(BigDecimal.valueOf(shares));
    }
}
```

### ラムダ式を使ってテストスタブを実装

```java
    @Test
    public void testComputeStockWorth() {
        CalculateNAV instance = new CalculateNAV(ticker -> new BigDecimal("6.01"));
        BigDecimal expected = new BigDecimal("6010.00");
        BigDecimal actual = instance.computeStockWorth("GOOG", 1000);
        assertEquals(expected, actual);
    }
```


### メソッド参照を使って実装

```java
    final CalculateNAV calculateNav = new CalculateNAV(YahooFinance::getPrice);
    System.out.println(String.format("100 shares of Google worth: $%.2f", 
            calculateNav.computeStockWorth("GOOG", 100)));
```

```java
public class YahooFinance {
    public static BigDecimal getPrice(final String ticker) {/* 省略 */}
}
```


### その他

* 



## 4.3 ラムダ式を使ったデコレーション

GoF の Decorator パターンを使うと次のように読みづらいデコレーションコードになるが、

```java
setFilter(new Darker(new Brighter());
```

Java 8 であれば次のようなスマートな記述ができる、ということ。

```
setFilter(Color::brighter, Color::darker);
```

### 疑問

r に注目。

```
brighter           200 → 255
darker             200 → 140
brighter & darker  200 → 255 → 200？  178くらいじゃないかなぁ。。。
```

どうやら Function::compose() の解説が誤っている。
Function::andThen() を使うのが正しい。


### キモになる部分

```java
    private Function<Color, Color> filter;

    public void setFilters(final Function<Color, Color>... filters) {
        filter = Stream.of(filters)
                .reduce((filter, next) -> filter.compose(next))
                //.orElse(color -> color);
                .orElseGet(Function::identity);
    }
```

`Function<Color, Color>... filters` は任意の数だけ Function型の引数を受け取ることができる。
Stream API の reduce と Function::compose() を使って一つの Function 型オブジェクトに集約する。

例えば、

```java
setFilters(A, B, C, D, E);
```

というコードは

```java
(input) -> E.apply(D.apply(C.apply(B.apply(A.apply(input)))));
```

というデコレーターを生成する。


### compose の解説

解説では、

```java
    Function<String, String> target = (String t) -> t.concat("->target");
    Function<String, String> next = (String t) -> t.concat("->next");

    Function<String, String> wrapper = target.compose(next);
    System.out.println(wrapper.apply("input"));
```

の出力結果は、

```
input->target->next
```

のはずだか実際には、

```
input->next->target
```

`Function::andThen()` を使うことで解説どおりの動作になる。


```java
public final class Camera {

    private Function<Color, Color> filter;

    public Camera() {
        setFilters();
    }

    public void setFilters(final Function<Color, Color>... filters) {
        filter = Stream.of(filters)
                .reduce((filter, next) -> filter.compose(next))
                //.orElse(color -> color);
                .orElseGet(Function::identity);
    }

    public Color capture(final Color inputColor) {
        final Color processedColor = filter.apply(inputColor);
        return processedColor;
    }

    public static void main(String[] args) {
        final Camera camera = new Camera();
        final Consumer<String> printCaptured
                = (filterInfo)
                -> System.out.println(String.format(
                                "with %s: %s",
                                filterInfo,
                                camera.capture(new Color(200, 100, 200))));

        printCaptured.accept("no filter");

        camera.setFilters(Color::brighter);
        printCaptured.accept("brighter filter");

        camera.setFilters(Color::darker);
        printCaptured.accept("darker filter");

        camera.setFilters(Color::brighter, Color::darker);
        printCaptured.accept("brighter & darker filter");
    }

}
```


### その他

* インタフェースに default メソッドが追加になった。
* インタフェースで static メソッドを使えるようになった。


## 4.4 defaultメソッドを覗く

ルール 1. サブタイプは自動的にスーパータイプの default メソッドを継承する。

```java
public interface Super {
    default void 




## 4.5 ラムダ式を使った流暢なインタフェース


## 4.6 例外処理


## 4.7 まとめ

