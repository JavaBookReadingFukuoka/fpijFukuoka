# ８章　ラムダ式で合成

## 8.1 関数合成の利用

まず基本の話。
以下の４つの関数があるとする。

> f : 入力された数値を１０倍する<br>
> g : 入力された数値を３０増やす<br>
> h : 入力された数値を１／２にする<br>
> i : 入力された数値を２０減らす

数値 x=5 にこれを順番に適用すると
> f(5) = 5 * 10 = 50<br>
> g(50) = 50 + 30 = 80<br>
> h(80) = 80 / 2 = 40<br>
> i(40) = 40 - 20 = 20

というわけで、i(h(g(f(5)))) = 20<br>
ところで
> i(h(g(f(x)))) = (((x * 10) + 30) / 2) - 20 = x * 5 - 5

なので、i(h(g(f(x))))の算出結果にx = 5を適用しても
> x * 5 - 5 = 5 * 5 - 5 = 25 - 5 = 20

当然同じ結果が得られる。

関数合成ってこういう理解でも良いかしら？ まあ、恐い事は無いです。

さて、P170の図8-1をどう説明したものかと考えた結果、コードを書きました。<br>
[IntValueObject.java](./IntValueObject.java)<br>
[ObjectWithFunction.java](./ObjectWithFunction.java)<br>

で、図8-1が分かっていれば、例8-3のコードはスムーズに飲み込めるかと。<br>
[Stocks100.java](./Stocks100.java)


## 8.2 MapReduceの使用
MapResuceパターンは単純でマルチコアプロセッサを有効活用できますよ。

### 8.2.1 計算の準備
本編とは関係なく、この後の説明のためのStockInfoクラス、StockUtilクラスの作成。

### 8.2.2 命令型スタイルからの脱出
まずは命令型スタイルで書いてみる。

例8-8はループが３回登場。

```Java
final List<StockInfo> stocks = new ArrayList<>();
for (String symbol : Tickers.symbol) {
    stocks.add(StockUtil.getPrice(symbol));
}

final List<StockInfo> stocksPriceUnder500 = new ArrayList<>();
final Predicate<StockInfo> isPriceLessThan500 = StockUtil.isPriceLessThan(500);
for (StockInfo stock : stocks) {
    if (isPriceLessThan500.test(stock))
        stocksPriceUnder500.add(stock);
}

StockInfo highPriced = new StockInfo("", BigDecimal.ZERO);
for (StockInfo stock : stocks) {
    highPriced = StockUtil.pickHigh(highPriced, stock);
}

System.out.println("High priced under $500 is " + highPriced);
```

例8-9ではループを１回にまとめてみる。

```Java
StockInfo highPriced = new StockInfo("", BigDecimal.ZERO);
final Predicate<StockInfo> isPriceLessThan500 = StockUtil.isPriceLessThan(500);
for (String symbol : Tickers.symbol) {
    StockInfo stockInfo = StockUtil.getPrice(symbol);
    if (isPriceLessThan500.test(stockInfo))
        highPriced = StockUtil.pickHigh(highPriced, stockInfo);
}
System.out.println("High priced under $500 is " + highPriced);
```

・・・が、例8-9のコードはコード量とループ数は減ったものの、再利用が出来ない。

### 8.2.3 そして関数型へ

例8-10で関数型のコードとして書く。
例8-11とまとめると・・・

```Java
final StockInfo highPriced =
    Tickers.symbol.stream()
        .map(StockUtil::getPrice)
        .filter(StockUtil.isPriceLessThan(500))
        .reduce(StockUtil::pickHigh)
        .get();
System.out.println("High priced under $500 is " + highPriced);
```

図8-2はmap-filter-reduceの図示。並列化が可能に見える事に着目。


## 8.3 並列化への飛躍

準備が整っていれば、parallelStream()を使うだけで容易に並列化できる。

```Java
    Tickers.symbol.stream()         // 並列化無し
    Tickers.symbol.parallelStream() // 並列化
```

ただし、並列化は必ずしも最適ではない。並列化が良い場合があるというだけ。


## 8.4 まとめ
綺麗に設計しておけば、ラムダ式で簡単に並列化できて実行速度向上にもつながるよ！！
