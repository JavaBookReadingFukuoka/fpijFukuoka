# ３章　文字列、コンパレータ、フィルタ

## 3.1 文字列のイテレーション

charsはCharSequenceインタフェースの
デフォルトメソッドでStringは実装してる

```java
public interface CharSequence {
  public default IntStream chars() { ... }
}

public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
      ...
}
```

なのでこんな風にかけるよ。IntStreamなので数字がでるよ。

```java
"abc".chars().forEach(c -> System.out.print(c));
// 97
// 98
// 99
```

```java
// ※ 参考 内部イテレータ
public interface IntStream extends BaseStream<Integer, IntStream> {
   void forEach(IntConsumer action);
}
```

```java
// 参考 メソッド参照
"abc".chars().forEach(System.out::println);
// 97
// 98
// 99
```

System.outは既にターゲットが指定されているため
メソッド参照に渡された引数はターゲットでなく
メソッドの引数として使用するよ

```java
public final class System {
    public final static PrintStream out = null;
}
```

例：文字コード１個右で表示
```java
class OneRightChar {
  private static void println(int iChar){
    System.out.println((char)(iChar+1));
  }
}

"abc".chars().forEach(OneRightChar::println);
// b
// c
// d
```

mapToObjで先に文字コード１個右で表示を準備することもできるよ
```java
"abc".chars()
        .mapToObj(c -> Character.valueOf((char) (c + 1)))
        .forEach(System.out::println);
// b
// c
// d
```

charsの結果をfilterで小文字のみ対象にした例

```java
"aBcD".chars()
        .filter(c->Character.isLowerCase(c))
        .forEach(c->OneRightChar.println(c));
// b
// d
```

さっきの例をメソッド参照にした
Character::isLowerCaseはstaticメソッドだから
引数に使用されるよ！
```java
"aBcD".chars()
        .filter(Character::isLowerCase)
        .forEach(OneRightChar::println);
// b
// d
```

メソッド参照へ渡された引数は

- インスタンスメソッドはターゲット
- staticなメソッドは引数

に使われるみたい。

両方あって曖昧な場合は
決定できないのでラムダ式を使うべし！


## 3.2 Comparatorインタフェースを実装

### 3.2.1 コンパレータを使ったソート

List#sort()はリスト自体が書き換わってしまうけど、  
List#stream().sorted()なら元のリストを壊さずに  
ソート結果が手に入るよ

並び替えの対象になるJavaBean

```java
public class Hero {
    private final String name;
    private final int power;
    public Hero(final String name, final int power) {
        this.name = name;
        this.power = power;
    }
    public int powerDiff(final Hero other) {
        return power - other.power;
    }
    public String toString() {
        return String.format("%s さんの戦闘力は %d です。", name, power);
    }
}
```

```java
final List<Hero> heros = Arrays.asList(
        new Hero("あくましょうぐん", 10000),
        new Hero("うぉーずまん", 100),
        new Hero("ばっふぁろーまん", 1000)
);
heros.forEach(System.out::println);
//あくましょうぐん さんの戦闘力は 10000 です。
//うぉーずまん さんの戦闘力は 100 です。
//ばっふぁろーまん さんの戦闘力は 1000 です。
```

元のリストは壊さずに戦闘力順リストを出力！

```java
List<Hero> sortedHeros = heros
        .stream()
        .sorted(
                (hero1,hero2)->hero1.powerDiff(hero2)
        )
        .collect(Collectors.toList());
sortedHeros.forEach(System.out::println);
//うぉーずまん さんの戦闘力は 100 です。
//ばっふぁろーまん さんの戦闘力は 1000 です。
//あくましょうぐん さんの戦闘力は 10000 です。
```

メソッド参照だとみじかいよ

```java
List<Hero> sortedHeros = heros
        .stream()
        .sorted(Hero::powerDiff)
        .collect(Collectors.toList());
```

ラムダだと逆にも書けるよ（メソッド参照はできないけど）

```java
List<Hero> sortedHeros = heros
        .stream()
        .sorted(
                (hero1, hero2) -> hero2.powerDiff(hero1)
        )
        .collect(Collectors.toList());
sortedHeros.forEach(System.out::println);
//あくましょうぐん さんの戦闘力は 10000 です。
//ばっふぁろーまん さんの戦闘力は 1000 です。
//うぉーずまん さんの戦闘力は 100 です。
```

昇順と降順二つ書いた時になるべくコードの重複をなくす

```java
// 昇順
Comparator<Hero> ascendingPower = (hero1,hero2)->hero1.powerDiff(hero2);

// 降順
Comparator<Hero> descendingPower = ascendingPower.reversed();
```

一番つよいのは？

```java
heros.stream()
        .max(Hero::powerDiff)
        .ifPresent(System.out::println);
//あくましょうぐん さんの戦闘力は 10000 です。
```

min()やmax()はリストに値がないこともあるためOptionalを返します。  
ifPresentは値がある時のみ実行します。

## 3.3 複数のプロパティによる流暢な比較

## 3.4 collectメソッドとCollectorsクラスの使用

## 3.5 ディレクトリの全ファイルをリスト

## 3.6 ディレクトリの特定のファイルだけをリスト

## 3.7 flatMapで直下のサブディレクトリをリスト

## 3.8 ファイルの変更を監視

## 3.9 まとめ
