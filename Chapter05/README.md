# ５章　外部リソースを扱う

* リソースとは？
  * 「資源」を意味する。原義は"[to rise](http://www.oxforddictionaries.com/definition/english/resource)"（昇る，増大する）
  * 資源は「有限」であるという問題
  * 計算資源（CPUやメモリ）は有限なので枯渇しないように「解放」しなげればならない

* 外部リソースとは?
  * データベース接続
  * ファイル
  * ソケット
  * ネイティブリソース
  * [JDK close()メソッド一覧 - GrepCode](http://grepcode.com/search?query=close%28%29&start=0&r=repository.grepcode.com%24java%24root&entity=method&n=)

「外部リソース」を使用する場合は，ガベージコレクション(GC)は開発者の責任の範囲。

## 5.1 リソースの解放

**【問題】もしリソースを開放しなければ，何がどうなるのか？**

ベンカット・スブラマニアン（著者）の経験： 

* **アプリケーションの使用率が高くなると落ちるアプリケーション**
  * 開発者はその問題を**「正常に動きます…ほとんどの場合は。」**と説明した
  * finalize()でデータベースの接続を解放していた
  * JVMは十分なメモリを持っているのでGCを走らせる必要がないと判断する
  * ファイナライザは滅多に呼ばれず外部リソースが溜まり，動作が不安定になっていた

JJUG CCC 2015 Springのケース：

* **JJUGの発表資料「ほんとうに便利だった業務で使えるJava SE8新機能」でファイル処理のリソースの解放漏れ？**
  * 業務にJava8を導入したJJUGの発表が話題に！
    * [ほんとうに便利だった業務で使えるJava SE8新機能（JJUG CCC 2015 Spring）, p.33 - SlideShare](http://www.slideshare.net/yuukifukuda378/ss-46878413)
  * 発表翌日（2015/04/12）に，「33ページのコードにはリソース解放漏れがあります」と指摘が発生
    * [だから、あれほどFiles#lines(Path)を使うときはtry-with-resourcesでちゃんと包めといったのに… #jjug #ccc_f2 - mike-neckのブログ](http://mike-neck.hatenadiary.com/entry/2015/04/12/210000)
  * 登壇者は「33Pで説明したいことの本質ではなかったため、意図して省きました。セッションでは39Pで説明しました」とコメントした

    ```java
    Files.lines(Paths.get("sample.txt"))
             .forEach(string -> System.out.println(string));
    ```

僕のケース：

* **バッチで「ORA-01000:最大オープン・カーソル数を超えました」が発生**
  * 要件はn秒間隔でOracleのDBにアクセスして，データを監視するバッチ
  * コードレビューを担当
    * 僕:「帰社前に起動して，翌朝例外がなければ良いんじゃない」
    * コーダー:「何もありませんでした」
    * 僕:「OK，コンピュータ」
  * 結合テストで「ORA-01000:最大オープン・カーソル数を超えましたが発生」が発生していると，プロパーから指摘
  * なぜ気づかなかったのか？
    * try-catchのcatchで例外を潰していた…

### 5.1.1 問題を覗いてみる

外部リソースを参照すると計算資源（CPUやメモリ）を消費する。しかし計算資源は有限であるという問題がある。
有限資源を管理するために，GCに任せてリソースを解放するfinalizeを実装したが，JVMは十分にメモリを確保している（供給の過剰）ため，
GCがfinalizeを呼ばず，アプリの動作が不安定になるまで資源を消費し続ける問題。

メモリの確保が不十分な状態 "-Xms1m -Xmx1m" をお膳立てして，GC(finalize)が呼ばれる様子を見てみよう。 
  * [FileWriterARM_5_1_1.java](./resources/FileWriterARM_5_1_1.java)

### 5.1.4 自動リソース管理（ARM）の使用

Java 7で導入された**ARM(Automatic Resource Management)**を使えばOK？

* ARMとは？
  * AutoClosableをimplementsすると，JVMによって自動でclose()が呼ばれる仕組み。開発者は明示的にcloseを書かなくても良い
  * try-with-resources構文と呼ぶ
  * ARMは非常に完結で魅力的
* [FileWriterARM_5_1_4.java](./resources/FileWriterARM_5_1_4.java)

しかし，**ARMは開発者が忘れずにtry-with-resourcesを記述しなければいけない**。

**[「もっといい方法があるはずだ」（We can do better than that）](http://wired.jp/2014/05/08/astro-teller/)**


## 5.2 ラムダ式でリソース解放

開発者がtry-with-resources構文を使用しなくても，ラムダ式を使えば自動的にリソース解放ができるAPIを記述できる！？

### 5.2.2 高階関数の使用

Execute Around Methodパターンを使って，確実に外部リソースを解放する。
 * Execute Around Methodパターンとは？
  * ラムダ式でインスタンスを受け取ることを強制させ，メソッド呼び出しにはラムダ式の使用を開発者に強制させる
  * [Kent Beckの『Smalltalkベストプラクティス・パターン』に書かれている](http://d.hatena.ne.jp/t-wada/20040910/p1)

```java
FileWriterEAM.use("eam.txt", writerEAM -> writerEAM.writeStuff("sweet"));
```

```java
@FunctionalInterface
public interface UseInstance<T, X extends Throwable> {
    void accept(T instance) throws X;
}
```

```java
public static void use(final String fileName, final UseInstance<FileWriterEAM_5_2_3, IOException> block) throws IOException {
    final FileWriterEAM_5_2_3 writerEAM = new FileWriterEAM_5_2_3(fileName);
    try {
        block.accept(writerEAM);
    } finally {
        writerEAM.close();
    }
}
```

### 5.2.3 インスタンス解放に使用

ラムダ式はブロック{ }で複数行のコードも書くことができる
  * [FileWriterARM_5_2_3.java](./resources/FileWriterEAM_5_2_3.java)

```java
use("eam.txt", writerEAM -> {
    writerEAM.writeStuff("how");
    writerEAM.writeStuff("sweet");
});
```
**長いラムダは罪悪**そのものである。ラムダは完結でわかりやすくシンプルにメンテナンスができること。長い場合は他のメソッドに移して，メソッド参照を使うべき

## 5.3 ロックの管理

排他制御を実現したい。Java 1.0の頃からsyncronizedがある。しかしsyncronizedには弱点がある。（詳しくは『Java並行処理プログラミング』『Programming Concurrency on the JVM』を参照）

* syncronizedの弱点
  * タイムアウトの設定が難しい
  * デッドロックやライブロック発生確率が上昇する
* Java 5でLockインタフェースが導入された
  * 所定時間ロックが取得できない場合にタイムアウトを発生させることができる

ロックしたら確実にアンロックする必要がある。（Aしたら確実にBする必要がある）Execute Around Methodパターンが使えそう！

  * [Locking_5_3.java](./resources/Locker_5_3.java)


## 5.4 簡潔な例外テストの生成

Java 5でアノテーションが導入されると，JUnitは即座にそれを採用した。
アノテーションによるテストは簡潔すぎてわかりづらい。ラムダ式で変える。

### 5.4.4 テストの実行

* try/catchを使ったテストはとても冗長。
* アノテーションを使ったテストは意図しないメソッドで例外が発生してもテストに合格する可能性がある。
* ラムダ式を使うと簡潔で，意図したメソッドでの例外でテストが検証される。


どのテストコードが一番良い？

```java
  @Test public void VerboseExceptionTest() {
    rodCutter.setPrices(prices);
    try {
      rodCutter.maxProfit(0);
      fail("Expected exception for zero length");
    } catch(RodCutterException ex) {
      assertTrue("expected", true);
    }
  }
```

```java
  @Test(expected = RodCutterException.class) 
  public void TerseExceptionTest() {
    rodCutter.setPrices(prices);
    rodCutter.maxProfit(0);
  }
```

```java
  @Test 
  public void ConciseExceptionTest() {
    rodCutter.setPrices(prices);
    assertThrows(RodCutterException.class, () -> rodCutter.maxProfit(0));
  }
```

## 5.5 まとめ

アプリケーションで外部リソースを使用する場合は，GCはユーザの責任。Java 8のラムダ式でExecute Around Methodパターンを使うと，
リソースの解放をラップすることができる。

**NO MORE メモリリーク**
