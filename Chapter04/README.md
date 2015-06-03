# ４章　ラムダ式で設計する
test

## 4.1 ラムダ式を使った関心の分離

### 間違った設計

証券(BOND)と株式(STOCK)を表すことができる資産(Asset)クラスがある。
その価値を合計するメソッド(totalAssetValues)を資産ユーティリティ(AssetUtil)クラスに定義した。

その後、証券(BOND)だけの価値を合計するメソッド(totalBondValues)と株式(STOCK)だけの価値を合計するメソッド(totalStockValues)が必要となりそれぞれ定義した。

結果としてできたクラス定義はこんな感じ。

```
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets) {/* 省略 */}
     public int totalBondValues(List<Asset> assets) {/* 省略 */}
     public int totalStockValues(List<Asset> assets) {/* 省略 */}
}
```

使い方は、

```
System.out.println("Total of all assets: " + AssetUtil.totalAssetValues(assets));
System.out.println("Total of bonds: " + AssetUtil.totalBondValues(assets));
System.out.println("Total of stocks: " + AssetUtil.totalStockValues(assets));
```

きつい匂いがしてくる。本物のJava使いならこう書くはずだろう？

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

三つのメソッドをインタフェースを使って一つにし、「何を合計するか」を分離したところまでは良かったのですが、明らかにラムダ式の出番です。AssetSelectorは定義済みのjava.util.function.Predicateインタフェースを再利用し、次のように書きましょう。

```
public final class AssetUtil {
     public int totalAssetValues(List<Asset> assets, Predicate<Asset> assetSelector) {/* 省略 */}
}

System.out.println("Total of all assets: " + AssetUtil.totalAssetValues(assets, asset -> true));
System.out.println("Total of bonds: " + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.BOND));
System.out.println("Total of stocks: " + AssetUtil.totalAssetValues(assets, asset -> asset.getType() == Asset.AssetType.STOCK));
```

### まとめ

* ラムダ式を使って新たなクラスを作ることなくメソッドから関心の分離を行った。
* StrategyのようなパターンをJavaで実装する場合は通常インタフェースとクラスを使用するが、ラムダ式は一歩進んだ設計手法を与えてくれる。
* このパターンは高階関数を使う開発者が選択ロジックを提供する必要がある。
* しかし、選択ロジックは変数に格納しておき任意の時点で再利用できる。
* これはメソッドレベルでの関心の分離の一例。


## 4.2 ラムダ式を使った委譲


## 4.3 ラムダ式を塚使ったデコレーション


## 4.4 defaultメソッドを覗く


## 4.5 ラムダ式を使った流暢なインタフェース


## 4.6 例外処理


## 4.7 まとめ

