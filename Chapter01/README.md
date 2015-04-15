# １章　Hello、ラムダ式

## 1.1 考え方を変える
従来型のコードは命令型で手続き的にプログラムを記述する。<br>
より良い方法として、宣言型での記述を。<br>

コレクションに特定の値が含まれているか調べる際に、コレクションの要素を特定の値が見つかるまでイテレートするのは命令型。<br>
コレクションの要素が含まれているかどうかのAPIを使えば宣言型。

ラムダ式の登場前は命令型でしか書けなかった処理も、ラムダ式の登場で宣言型に記述できるようになった。

例）
```
final BigDecimal totalOf値引価格 =
 prices.stream()
       .filter(価格 -> 価格.compareTo(BigDecimal.valueOf(20)) > 0)
       .map(価格 -> 価格.multiply(BigDecimal.valueOf(0.9)))
       .reduce(BigDecimal.ZERO, BigDecimal::add);
System.out.println("値引き後価格の合計: " + totalOf値引価格);
```


## 1.2 関数型のコードによる大きな利益



## 1.3 なぜ関数型で記述するのか



## 1.4 革命ではなく、進化



# 1.5 簡単にするためほんの少しの砂糖



# 1.6 まとめ


