# ２章　コレクションの使用

## 2.1 リストをイテレート
古典的なforループから、ラムダ式、メソッド参照まで。<br>
[Gistにコード例記述](https://gist.github.com/tksy/b1ff68427228ea303e37)

foreach()の引数はConsumerインタフェース。引数を一つ受け取って戻り値を返さない。

## 2.2 リストの変換
コレクションの変換処理を、古典的な方法に変わってmap()メソッドを使用。<br>
[Gistにコード例記述](https://gist.github.com/tksy/7db0545f6387a748eb5f)

map()を通過するとラムダ式に渡した変換が行われると考えると良い。<br>
mapの中の引数はFunctionインタフェース。引数を一つ受け取って戻り値を返す。

## 2.3 要素の検索
コレクションからの要素の検索を、filter()メソッドで実装。
[Gistにコード例記述](https://gist.github.com/tksy/0f485932249ac9c12228)

filter()内のラムダ式の検査を満たす要素だけが通過できると考えると良い。<br>
filterの中の引数はPredicateインタフェース。引数を一つ受け取ってbooleanの戻り値を返す。

## 2.4 ラムダ式の再利用
ラムダ式は関数型インタフェースに保存可能。
[Gistにコード例記述](https://gist.github.com/tksy/0e633b6d85d543621301)

いくつか例
```Java
final Consumer<String> sysoutConsumer = name -> System.out.println(name);
final Supplier<String> nothingSupplier = () -> "Nothing";
final Predicate<String> lengthGreaterThan5 = name -> name.length() > 5;
final Function<String, String> toUpperCaseFunction = name -> name.toUpperCase();
```

## 2.5 静的スコープとクロージャ



## 2.6 要素を１つ選択



## 2.7 コレクションを単一の値に集約（reduce）



## 2.8 要素の結合



## 2.9 まとめ



