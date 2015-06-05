# ４章　ラムダ式で設計する

インタフェース、クラス階層、匿名内部クラスを簡潔なコードに置き換えることができる。

* 機能からロジックを簡単に分離し拡張をより簡単にするためにラムダ式を使用する例
* ラムダ式を使ったわずか数行のコードで責任の委譲を行い、Decoratorパターンを実装する例
* 冗長なインタフェースを流暢で直感的なインタフェースに変身させる例

以上が４章の内容。


## 4.1 ラムダ式を使った関心の分離

[高階関数](http://ja.wikipedia.org/wiki/高階関数)を使うことでクラス階層を必要とせずコードの再利用ができる。


### 間違った設計

証券(BOND)と株式(STOCK)を表すことができる資産(Asset)クラスがある。
その価値を合計するメソッド(totalAssetValues)を資産ユーティリティ(AssetUtil)クラスに定義した。

その後、証券(BOND)だけの価値を合計するメソッド(totalBondValues)と株式(STOCK)だけの価値を合計するメソッド(totalStockValues)が必要となりそれぞれ追加した。

結果としてできたクラスはこんな感じ。

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

本物のJava使いならこう書くはず。（でしょ？ｗ）

```java
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets, AssetSelector selector) {}
}
```

```java
interface AssetSelector {
     boolean test(Asset asset);
}
```

```java
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

AssetSelector インタフェースを使えば「何を合計するか」を分離することができる。いわゆる Strategy パターン。


### 正しい設計

三つのメソッドをインタフェースを使って一つにし、「何を合計するか」を分離したところまでは良かったが、ここはラムダ式の出番。
AssetSelector は定義済みの java.util.function.Predicate インタフェースを再利用し、次のように書く。

```java
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets, Predicate<Asset> assetSelector) {/* 省略 */}
}
```

```java
System.out.println("Total of all assets: " 
                   + AssetUtil.totalAssetValues(assets, asset -> true));
System.out.println("Total of bonds: " 
                   + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.BOND));
System.out.println("Total of stocks: " 
                   + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.STOCK));
```

### まとめ

* ラムダ式を使って新たなクラスを作ることなくメソッドレベルでの関心の分離を行うことができる。
* Strategy のようなパターンを Java で実装する場合、通常インタフェースとクラスを使用するがラムダ式は一歩進んだ設計手法を与えてくれる。
* このパターンは高階関数を使う開発者が選択ロジックを提供する必要がある。
* しかし、選択ロジックは変数に格納しておき任意の時点で再利用できる。


## 4.2 ラムダ式を使った委譲

* 責任の一部を他のクラスに委譲するのではなく、ラムダ式やメソッド参照に委譲できる。


### 関数型インタフェースを使って委譲する

株価をリクエストし、その価格と保有株数から合計資産額を計算する。
株価をリクエストする箇所を委譲し、テスト用の Web サービスのスタブを使ってテストする例と本物の Web サービスと統合する例を示す。

```java
public class CalculateNAV {
    private Function<String, BigDecimal> priceFinder;  // 委譲先

    public CalculateNAV(Function<String, BigDecimal> priceFinder) {
        this.priceFinder = priceFinder;
    }

    public BigDecimal computeStockWorth(final String ticker, final int shares) {
        return priceFinder.apply(ticker).multiply(BigDecimal.valueOf(shares));  // 委譲している箇所
    }
}
```


### ラムダ式を使ってテストスタブを注入

`ticker -> new BigDecimal("6.01")` が注入される実装。

```java
@Test
public void testComputeStockWorth() {
    CalculateNAV instance = new CalculateNAV(ticker -> new BigDecimal("6.01"));  // 注入箇所
    BigDecimal expected = new BigDecimal("6010.00");
    BigDecimal actual = instance.computeStockWorth("GOOG", 1000);
    assertEquals(expected, actual);
}
```


### メソッド参照を使って本物の実装を注入

`YahooFinance::getPrice` が注入される実装。

```java
final CalculateNAV calculateNav = new CalculateNAV(YahooFinance::getPrice);  // 注入箇所
System.out.println(String.format("100 shares of Google worth: $%.2f", 
        calculateNav.computeStockWorth("GOOG", 100)));
```

```java
public class YahooFinance {
    public static BigDecimal getPrice(final String ticker) {/* 省略 */}
}
```


### その他

* BufferedReader に追加された Stream を返す lines メソッドが紹介されている。
* Function::apply() メソッドではチェック例外を throw できないため RuntimeException にラッピングして再スローしている。



## 4.3 ラムダ式を使ったデコレーション

もしもカメラのフィルターを表す実装を GoF の Decorator パターンを使って書いたら。

```java
setFilters(new Darker(new Brighter());
```

Decorator パターンはとても強力だが、とても面倒なクラスとインタフェースの階層が発生する。
Java 8 であれば次のようなスマートな記述ができる。

```
setFilters(Color::brighter, Color::darker);
```

メソッド参照を関数型インタフェースの機能を使って委譲をチェーン化できます。


### 疑問

ちょいと話が逸れます。サンプルコードの実行結果 r に注目。

```
brighter           200 → 255
darker             200 → 140
brighter & darker  200 → 255 → 200？  178くらいじゃないかなぁ。。。
```

どうやら Function::compose() の解説が誤っている。計算の順序が逆。
ここは Function::andThen() を使うのが正しいと思われる。


### キモになる部分

```java
private Function<Color, Color> filter;

public void setFilters(final Function<Color, Color>... filters) {
    filter = Stream.of(filters)
            .reduce((filter, next) -> filter.andThen(next))
            //.orElse(color -> color);
            .orElseGet(Function::identity);
}
```

* `Function<Color, Color>... filters` は任意の数だけ Function型の引数を受け取ることができる。
* Stream API の reduce と Function::andThen() を使って一つの Function 型オブジェクトに集約する。

下記コードは、その下のラムダ式と同じ機能のフィルターを設定する。

```java
setFilters(A, B, C, D, E);
```

```java
Color apply(Color input) {
    return E.apply(D.apply(C.apply(B.apply(A.apply(input))))));
}

```


### compose はきっと andThen の誤り

解説どおりだと、

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

になるはずだか実際には、

```
input->next->target
```

`Function::andThen()` が正しいよね？
[API 仕様](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Function.html#compose-java.util.function.Function-)


### その他

* `.orElse(color -> color)` と `.orElseGet(Function::identity)` は等価。
  [API 仕様](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Function.html#identity--)
* インタフェースに default メソッドが追加になった。 compose はその一つ。
* ちなみにインタフェースで static メソッドを使えるようになった。


## 4.4 defaultメソッドを覗く

次回報告します。


## 4.5 ラムダ式を使った流暢なインタフェース

次のような mailer が何度も出てくるうえ、mailer オブジェクトの生存期間が分からないようなうるさいコードを、

```java
Mailer mailer = new Mailer();
mailer.from("build@agiledeveloper.com");
mailer.to("venkats@agiledeveloper.com");
mailer.subject("build notification");
mailer.body("... your code sucks ...");
mailer.send();
```

こんな流暢なコードにしましょう、という話し。

```java
FluentMailer.send(mailer ->
    mailer.from("build@agiledeveloper.com")
          .to("venkats@agiledeveloper.com")
          .subject("build notification")
          .body("... your code sucks ..."));
```

どこが流暢かというと、

* mailer オブジェクトを new する必要がなく、mailer オブジェクトの生存期間、スコープが明確。
* メソッドチェーン（またはカスケードメソッドパターン）で mailer が何度も出てこなくなった。


### メソッドチェーン

各メソッドが this を返すことで読み出しをチェーン化する。

```java
public class MailBuilder {
    public MailBuilder from(final String address) {/* ... */ return this;}
    public MailBuilder to(final String address) {/* ... */ return this;}
    public MailBuilder subject(final String line) {/* ... */ return this;}
    public MailBuilder body(final String message) {/* ... */ return this;}
    public void send() { System.out.println("sending..."); }
}

new MailBuilder()
    .from("build@agiledeveloper.com")
    .to("venkats@agiledeveloper.com")
    .subject("build notification")
    .body("... your code sucks ...")
    .send();
```

ただし、new キーワードが API の可読性と流暢さを低減させている。
new からの参照を保持することや、その参照からチェーンを続けることを阻止しない。


### 流暢な設計

```java
public class FluentMailer {
    private FluentMailer() {}

    public FluentMailer from(final String address) {/* ... */ return this;}
    public FluentMailer to(final String address) {/* ... */ return this;}
    public FluentMailer subject(final String line) {/* ... */ return this;}
    public FluentMailer body(final String message) {/* ... */ return this;}

    public static void send(final Consumer<FluentMailer> block) {
        final FluentMailer mailer = new FluentMailer();
        block.accept(mailer);
        System.out.println("sending...");
    }
}
```

```java
FluentMailer.send(mailer ->
    mailer.from("build@agiledeveloper.com")
          .to("venkats@agiledeveloper.com")
          .subject("build notification")
          .body("... your code sucks ..."));
```

### その他

* サンプルコードの mailer のようにスコープを取得して、スコープ上で作業をして返す。このようなパターンをローンパターンと呼ぶ。
* メーラーの設定、データベース接続のパラメータ設定など、インスタンスの連続した状態を管理下におきつつ構築する必要のある場合に有用。


## 4.6 例外処理

関数インタフェースの実装でチェック例外を処理する方法は二つ。
* 例外を内部で処理するか。
* 内部で例外をキャッチして、非チェック例外として投げるか。


## 4.7 まとめ

* ラムダ式は強力かつ軽量なデザインツールである。
* 関数型インタフェースによる委譲
* ラムダ式を使ってオブジェクトの生存期間を明確にする。
* ただし、例外処理は注意深く行うこと。