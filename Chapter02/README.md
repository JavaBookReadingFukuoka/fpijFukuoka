# ２章　コレクションの使用

## 2.1 リストをイテレート
古典的なforループから、ラムダ式、メソッド参照まで。<br>
[Gistにコード例記述](https://gist.github.com/tksy/b1ff68427228ea303e37)

```Java
friends.forEach(name -> System.out.println(name));
friends.forEach(System.out::println);
```

[foreach()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#forEach-java.util.function.Consumer-)の引数は[Consumerインタフェース](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Consumer.html)。引数を一つ受け取って戻り値を返さない。<br>
なお、foreach()は並列実行時に順序を保証しないが、[forEachOrdered()](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#forEachOrdered-java.util.function.Consumer-)というメソッドもある模様。

## 2.2 リストの変換
コレクションの変換処理を、古典的な方法に変わってmap()メソッドを使用。<br>
[Gistにコード例記述](https://gist.github.com/tksy/7db0545f6387a748eb5f)

```Java
friends.stream()
       .map(name -> name.length())
       .forEach(name -> System.out.print(name + " "));
```

[map()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#map-java.util.function.Function-)を通過するとラムダ式に渡した変換が行われると考えると良い。<br>
mapの中の引数は[Functionインタフェース](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Function.html)。引数を一つ受け取って戻り値を返す。<br>
なお、上記はname.length()の戻りがintなので、無駄なint→Integerのオートボクシングが行われている。<br>
そこを解決するには[mapToInt()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#mapToInt-java.util.function.ToIntFunction-)を使うと良い。


## 2.3 要素の検索
コレクションからの要素の検索を、filter()メソッドで実装。<br>
[Gistにコード例記述](https://gist.github.com/tksy/0f485932249ac9c12228)

```Java
friends.stream()
       .filter(name -> name.startsWith("N"))
       .forEach(name -> System.out.print(name + " "));
```

[filter()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#filter-java.util.function.Predicate-)内のラムダ式の検査を満たす要素だけが通過できると考えると良い。<br>
filterの中の引数は[Predicateインタフェース](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Predicate.html)。引数を一つ受け取ってbooleanの戻り値を返す。


## 2.4 ラムダ式の再利用
ラムダ式は関数型インタフェースに保存可能。<br>
[Gistにコード例記述](https://gist.github.com/tksy/0e633b6d85d543621301)

いくつか例
```Java
final Consumer<String> sysoutConsumer = name -> System.out.println(name);
final Supplier<String> nothingSupplier = () -> "Nothing";
final Predicate<String> lengthGreaterThan5 = name -> name.length() > 5;
final Function<String, String> toUpperCaseFunction = name -> name.toUpperCase();
```


## 2.5 静的スコープとクロージャ
ラムダ式を返す関数を利用する。<br>
レキシカルスコープ（静的スコープ・lexical scope）とクロージャの話。<br>
[Gistにコード例記述](https://gist.github.com/tksy/184c5f75570e9d99093d)

```Java
Function<String, Predicate<String>> startsWithLetter = letter -> name -> name.startsWith(letter);
System.out.println("N始まりは、"
    + friends.stream()
             .filter(startsWithLetter.apply("N"))
             .count());
```

Functionインタフェースの[抽象メソッドapply()](http://docs.oracle.com/javase/jp/8/docs/api/java/util/function/Function.html#apply-T-)。これを使う。<br>
挙動が似てる気がしますが、JavaScriptのクロージャなどと同様に考えてよいのかしら？


## 2.6 要素を１つ選択
[findFirst()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#findFirst--)、[Optional](http://docs.oracle.com/javase/jp/8/docs/api/java/util/Optional.html)を利用する。<br>
[Gistにコード例記述](https://gist.github.com/tksy/4e97276c6e1cfd55eba7)

```Java
Optional<String> foundName = stream.findFirst();
System.out.println(foundName.orElse("無かった時"));
```

類似っぽいメソッドに[findAny()](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#findAny--)がある。<br>
説明によると、こちらは結果が決定的にならない模様。並列向けらしい。<br>何でも良いから結果があったら返してよ！って場合に使うものだと思われ。


## 2.7 コレクションを単一の値に集約（reduce）
[sum()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/IntStream.html#sum--)、[reduce()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#reduce-java.util.function.BinaryOperator-)を利用する。<br>
[Gistにコード例記述](https://gist.github.com/tksy/9344c87af83c4dc8beb9)

```Java
Optional<String> longName = friends.stream()
                .reduce((name1, name2) -> name1.length() >= name2.length() ? name1 : name2);
```

reduceにはメソッドが３つある。<br>
１つ目はOptionalを返す。引数はBinaryOperator。上記のように引数２つをもとに結果１つを返す。<br>
２つ目はOptional無し値を返す。引数は初期値＋BinaryOperator。初期値があればOptionalで無くてよいわけで。<br>
３つ目は型変換可能だが複雑。Optional無し値を返す。引数は初期値＋BiFunction＋BinaryOperator。<br>
たいていの場合、無理してこれを使うよりmapメソッドを間に挟んだ方が良さそう。

さらに言うと、そもそもreduceを使わずに次のcollectで良いじゃんという話もあるようで…


## 2.8 要素の結合
[String.join()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/lang/String.html#join-java.lang.CharSequence-java.lang.CharSequence...-)、[collect()メソッド](http://docs.oracle.com/javase/jp/8/docs/api/java/util/stream/Stream.html#collect-java.util.stream.Collector-)を利用する。<br>
[Gistにコード例記述](https://gist.github.com/tksy/5621f25fd7bb9dd750b9)

```Java
String joined1 = String.join(", ", friends);
String joined2 = friends.stream().collect(Collectors.joining(", "));
```

文字列結合のための辛いループ処理から解放されたよ！<br>
内部的には[StringJoiner](http://docs.oracle.com/javase/jp/8/docs/api/java/util/StringJoiner.html)。


## 2.9 まとめ
ラムダ式を使い、内部イテレータを使うようにすることで、より保守しやすいコードやさらに使いやすいドメインロジックやアプリケーションにつながり、基本部分のコーディング量を削減できるよ！！
