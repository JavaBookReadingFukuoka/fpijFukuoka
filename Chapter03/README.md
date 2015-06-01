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

比較式をラムダ式で作成するより

```java
Comparator<Hero> ascendingPower = (hero1,hero2)->hero1.powerDiff(hero2);
```

Comparator#comparing コンビニエンスメソッドを使う  

```java
import static java.util.Comparator.comparing;

public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
        Function<? super T, ? extends U> keyExtractor)
{
    Objects.requireNonNull(keyExtractor);
    return (Comparator<T> & Serializable)
        (c1, c2) -> keyExtractor.apply(c1).compareTo(keyExtractor.apply(c2));
}
```

引数の関数(Function)はラムダ式で書くか
```java
Function<Hero, Integer> byPower = hero -> hero.getPower();
List<Hero> sortedHeros = heros.stream()
        .sorted(comparing(byPower));
```

メソッド参照を使う

```java
List<Hero> sortedHeros = heros.stream()
        .sorted(comparing(Hero::getPower));
```

thenComparingで並び替え条件を追加する

```java
List<Hero> sortedHeros = heros.stream()
        .sorted(
          comparing(Hero::getPower)
          .thenComparing(Hero::getName)
        );
```

パワー順に並べて同じ場合は名前順にならべる


## 3.4 collectメソッドとCollectorsクラスの使用

↓のforEachをcollectを使ってなおします。
```java
private final List<Hero> heros = Arrays.asList(
        new Hero("あくましょうぐん", 10000),
        new Hero("ろびんますく", 100),
        new Hero("うぉーずまん", 100),
        new Hero("ばっふぁろーまん", 1000)
);

List<Hero> power1000AndOver = new ArrayList<Hero>();

heros.stream()
        .filter(hero -> hero.getPower() >= 1000)
        .forEach(hero -> power1000AndOver.add(hero));


power1000AndOver.forEach(System.out::println);
//あくましょうぐん さんの戦闘力は 10000 です。
//ばっふぁろーまん さんの戦闘力は 1000 です。
```

forEachだとArrayListのインスタンスを使うので
並列処理でスレッドセーフの問題が...

この命令型のコードを宣言型に

```java
List<Hero> power1000AndOver = heros.stream()
        .filter(hero -> hero.getPower() >= 1000)
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
```

collectの引数は左から...

- サプライヤ：コンテナの生成方法
- アキュムレータ：追加方法
- コンバイナ：他のコンテナとの結合方法

長い...  
けどコード内でArrayListの状態変更を行わない  
スレッドセーフになるよ  

でも長いのでCollectors#toListを使う

```java
public static <T>
Collector<T, ?, List<T>> toList() {
    return new CollectorImpl<>(
      (Supplier<List<T>>) ArrayList::new,
       List::add,
       (left, right) -> { left.addAll(right); return left; },
       CH_ID);
}
```

```java
List<Hero> power1000AndOver = heros.stream()
        .filter(hero -> hero.getPower()>=1000)
        .collect(Collectors.toList());

power1000AndOver.forEach(System.out::println);
```

Collectorsには他にも色々あるよ

- toSet
- toMap
- joining
- mapping
- collectingAndThen
- minBy
- maxBy
- groupingBy

groupingByでパワー別グループに分けてみた

```java
private final List<Hero> heros = Arrays.asList(
        new Hero("あくましょうぐん", 10000),
        new Hero("あしゅらまん", 200),
        new Hero("ろびんますく", 100),
        new Hero("うぉーずまん", 100),
        new Hero("うるふまん", 90),
        new Hero("ばっふぁろーまん", 1000)
);

Map<Integer,List<Hero>> powerGroup = heros.stream()
        .collect(Collectors.groupingBy(Hero::getPower));

powerGroup.entrySet().forEach(System.out::println);

// 10000=[あくましょうぐん さんの戦闘力は 10000 です。]
// 100=[ろびんますく さんの戦闘力は 100 です。, うぉーずまん さんの戦闘力は 100 です。]
// 1000=[ばっふぁろーまん さんの戦闘力は 1000 です。]
// 200=[あしゅらまん さんの戦闘力は 200 です。]
// 90=[うるふまん さんの戦闘力は 90 です。]

```


さらにmappingで名前だけ抽出

```java
Map<Integer,List<String>> powerGroupNameList = heros.stream()
        .collect(Collectors.groupingBy(Hero::getPower,
                Collectors.mapping(Hero::getName,Collectors.toList())));

powerGroupNameList.entrySet().forEach(System.out::println);

// 10000=[あくましょうぐん]
// 100=[ろびんますく, うぉーずまん]
// 1000=[ばっふぁろーまん]
// 200=[あしゅらまん]
// 90=[うるふまん]
```

ちょっと寄り道(mappingだけ試した)

```java
List<String> nameList = heros.stream()
        .collect(Collectors.mapping(Hero::getName, Collectors.toList()));

nameList.forEach(System.out::println);

// あくましょうぐん
// あしゅらまん
// ろびんますく
// うぉーずまん
// うるふまん
// ばっふぁろーまん
```

ちょっと寄り道(BinaryOperator.maxBy)で最強を探す

```java
Optional<Hero> saikyo = heros.stream()
        .reduce(BinaryOperator.maxBy(Hero::powerDiff));

saikyo.ifPresent(System.out::println);
// あくましょうぐん さんの戦闘力は 10000 です。
```

頭文字が同じで一番強いのを探す

```java
Map<String, Optional<Hero>> saikyoInGroup = heros.stream()
        .collect(Collectors.groupingBy(
                hero -> hero.getName().substring(0, 1),
                Collectors.reducing(
                        BinaryOperator.maxBy(Hero::powerDiff)
                )
        ));

saikyoInGroup.entrySet().forEach(System.out::println);

// ば=Optional[ばっふぁろーまん さんの戦闘力は 1000 です。]
// あ=Optional[あくましょうぐん さんの戦闘力は 10000 です。]
// う=Optional[うぉーずまん さんの戦闘力は 100 です。]
// ろ=Optional[ろびんますく さんの戦闘力は 100 です。]
```

## 3.5 ディレクトリの全ファイルをリスト

外部イテレータを使ってファイル一覧表示

```java
File dir = new File(".");
String[] fileNames = dir.list();
for (String fileName:fileNames){
    System.out.println("fileName="+fileName);
}
```

streamを使って関数型スタイルで

```java
try(Stream<Path> stream = Files.list(Paths.get("."))){
    stream.forEach(System.out::println);
}catch (IOException e){
    System.out.println("error!");
}
```

ファイルのみを絞り込んで表示する場合

```java
try(Stream<Path> stream = Files.list(Paths.get("."))){
    stream
            .filter(Files::isRegularFile)
            .forEach(System.out::println);
}catch (IOException e){
    System.out.println("error!");
}
```



## 3.6 ディレクトリの特定のファイルだけをリスト

匿名クラスで書いたら長い...

```bat
src\com\company\chapter3 のディレクトリ
Chap31.java
Chap36_1FileList.java              Chap36_2DirectoryStream.java       Chap36_3FileIsHidden.java
Chap37_1getSubDirectoryList.java   Chap37_2flatMap.java               Chap38_1FileWatch.java
Hero.java                          Sample.java                        Sample36.java
```


```java
File file = new File("src/com/company/chapter3");

final String[] filesNm = file.list(
        new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("Chap36");
            }
        }
);

for (String fileNm:filesNm){
    System.out.println(fileNm);
}

// -> Chap36_1FileList.java
// -> Chap36_2DirectoryStream.java
// -> Chap36_3FileIsHidden.java
```

ラムダ式でだとすっきり書ける

```java
final String[] filesNm2 = file.list(
        (dir,name)->name.startsWith("Chap36")
);
for (String fileNm:filesNm2){
    System.out.println(fileNm);
}


// -> Chap36_1FileList.java
// -> Chap36_2DirectoryStream.java
// -> Chap36_3FileIsHidden.java
```

DirecctoryStreamを使うよ

```java
Files.newDirectoryStream(
        Paths.get("src/com/company/chapter3"),
        path->path.getFileName().toString().startsWith("Chap36")
).forEach(System.out::println);

// -> src\com\company\chapter3\Chap36_1FileList.java
// -> src\com\company\chapter3\Chap36_2DirectoryStream.java
// -> src\com\company\chapter3\Chap36_3FileIsHidden.java
```

[newDirectoryStream](https://docs.oracle.com/javase/jp/7/api/java/nio/file/Files.html#newDirectoryStream%28java.nio.file.Path%29)

ファイル抽出でメソッド参照

```java
final File[] files3 = new File("src/com/company").
        listFiles(File::isFile);
for (File f : files3) {
    System.out.println(f.getName());
}
```



## 3.7 flatMapで直下のサブディレクトリをリスト

## 3.8 ファイルの変更を監視

## 3.9 まとめ
