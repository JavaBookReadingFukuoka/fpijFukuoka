# ５章　外部リソースを扱う

「外部リソース」を使用する場合は，ガベージコレクション(GC)は開発者の責任の範囲。

* 外部リソースとは?
  * データベース接続
  * ファイル
  * ソケット
  * ネイティブリソース
  * [JDK close()メソッド一覧 - GrepCode](http://grepcode.com/search?query=close%28%29&start=0&r=repository.grepcode.com%24java%24root&entity=method&n=)

## 5.1 リソースの解放

ベンカット・スブラマニアン（著者）の経験： 

* **アプリケーションの使用率が高くなると落ちるアプリケーション**
  * 開発者はその問題を**「正常に動きます…ほとんどの場合は。」**と説明した
  * finalize()でデータベースの接続を解放していた
  * JVMは十分なメモリを持っているのでGCを走らせる必要がないと判断する
  * ファイナライザは滅多に呼ばれず外部リソースが溜まり，動作が不安定になっていた

JJUG CCC 2015 Springのケース：

* **JUGGの発表資料「ほんとうに便利だった業務で使えるJava SE8新機能」でファイル処理のリソースの解放漏れ？**
  * 業務にJava8を導入したJUGGの発表が話題に！
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

外部リソースを解放するためにfinalizeを実装したが，JVMが十分にメモリを確保しているので，finalizeが呼ばれず，アプリの動作が不安定になる問題。

メモリの確保が不十分な状態 "-Xms1m -Xmx1m" でGC(finalize)が呼ばれる様子を見てみよう。 
  * [FileWriterARM_5_1_1.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/FileWriterARM_5_1_1.java)

### 5.1.2 リソースを閉じる

JVMに任せたfinalizeは不安定なので，リソース解放は明示的にclose()を呼びだそう。 
  * [FileWriterARM_5_1_2.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/FileWriterARM_5_1_2.java)

close()は例外が発生した場合も確実に呼ばれるのか？ - **NO!**

### 5.1.3 確実にリソースを解放する

**try-finally**で確実にリソースを閉じる。
  * [FileWriterARM_5_1_3.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/FileWriterARM_5_1_3.java)

外部リソースの操作中に例外が発生しても確実にリソースを解放できるようになった。

だが**コードがsmelly（臭う）** - [code smell](http://ja.wikipedia.org/wiki/%E3%82%B3%E3%83%BC%E3%83%89%E3%81%AE%E8%87%AD%E3%81%84)

### 5.1.4 自動リソース管理（ARM）の使用

Java 7で導入された**ARM(Automatic Resource Management)**を使えばOK？

* ARMとは？
  * AutoClosableをimplementsすると，JVMによってclose()が自動で呼ばれる仕組み。開発者は明示的にcloseを書かなくても良い
    * try-with-resources構文と呼ぶ
    * [FileWriterARM_5_1_4.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/FileWriterARM_5_1_4.java)
  * ARMは非常に完結で魅力的

しかし，**ARMは開発者が忘れずにtry-with-resourcesを記述しなければいけない**。

**[「もっといい方法があるはずだ」（We can do better than that）](http://wired.jp/2014/05/08/astro-teller/)**


## 5.2 ラムダ式でリソース解放

開発者がtry-with-resources構文を使用しなくても，ラムダ式を使えば自動的にリソース解放ができるAPIを記述できる！？

### 5.2.1 リソース解放を行うクラスの準備

外部リソースを使うが，ARMを使わない(AutoClosableをimplementsしない)クラスを準備する。

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

ラムダ式はブロック（{ }）で複数行のコードも書くことができる
  * [FileWriterARM_5_2_3.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/FileWriterEAM_5_2_3.java)

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

  * [Locking_5_3.java](https://github.com/k--kato/fpijFukuoka/blob/feature/Chapter05/resources/fpij/Locker_5_3.java)


## 5.4 簡潔な例外テストの生成

### 5.4.1 try/catchで例外テスト

### 5.4.2 アノテーションを使った例外テスト

### 5.4.3 例外テストにラムダ式を使用

### 5.4.4 テストの実行

## 5.5 まとめ


