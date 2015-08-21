# ７章　再帰の最適化

###必要な用語の解説

 * 再帰関数   
　その処理の中で自身の呼び出しを行うような関数。  

 * 末尾再帰関数   
　その関数の戻り値が自分自身をただ呼び出すのみであるような再帰関数。  
　[例(ExampleCode.java)](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/ExampleCode.java)

 * 末尾呼び出し最適化  
　通常、メソッドを呼び出す際には現在の変数の状態や処理の状態をスタックに積んでメソッドを呼び出し、処理が終わった後スタックに積んでいたものを戻す。そのため、大量の呼び出しが必要な再帰関数を呼び出すとStackOverFlowを起こす。  
　しかし、末尾再帰な関数は処理中での自身の呼び出しを通常のメソッド呼び出し(元に戻る情報が必要)ではなく、単なるジャンプ(元に戻る情報が必要ない)の連続として変換できることが知られている。この変換を末尾呼び出し最適化と呼ぶ。  
　SchemeやScalaはこの末尾呼び出し最適化を言語仕様として持っている。が、Javaにはそんな仕様はないので自分で書く。


## 7.1 末尾呼び出し最適化を使う
### 7.1.1 最適化前の再帰
　*factorialRec(),ex1(),ex2()* → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/Factorial.java)
### 7.1.2 末尾再帰に変換する
　さっきのfactorialRec()はそもそも末尾再帰にすらなってない。ので変換する。  
　関数の引数に計算中の値を追加し、それを呼び出しの際に渡すようにすれば戻り値を指定する部分で処理をしなくて良くなる。
### 7.1.3 TailCall関数型インターフェース
　p,154~156  
　この場合だと、{ t(f , n) , t(f\*n , n-1) , t(f\*n\*(n-1),n-2),・・・・}のような処理の連続したStreamを生成し、その一番最後の処理のみをfilterで取り出して処理結果を受け取るようにする。
### 7.1.4 TailCallsコンビニエンスクラス
 　p,156~157  
　TailCallだけではStreamの処理を切れないので処理の終わりの場合にのみ作成するTailCallを実装したコンビニエンスクラスを作る。
### 7.1.5 末尾再帰関数を使う
　*ex3(),ex4()* → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/Factorial.java)

*この後はあんまり本質的ではないのでそんなに説明しません。*
### 7.1.6 再帰を綺麗にする
  *ex5()* → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/Factorial.java)
### 7.1.7 算術オーバーフローを修正する
  *ex1()* → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/BigFactorial.java)


## 7.2 メモ化でスピードアップ
### 7.2.1 最適化問題
　問題説明
### 7.1.2 何の変哲もない再帰  
　例えば、n=5の場合  
　*f(4)+f(1) , f(3)+f(2) , f(2)+f(3) , f(1)+f(4)*  
　のうち最も大きいものが答えになる。

　このような問題のように問題全体を解くために、部分問題の計算方法を用いる方法を**分割統治法**という。これに、部分問題の答えを保存しておくメモ化を合わせたものを一般的に**動的計画法**と呼ぶ。

　*maxProfit(5),maxProfit(22)*  → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/RodCutterBasic.java)

### 7.2.3 計算結果のメモ化
　要するに、計算結果を保存しておくためのHashMapを用意しておき、それをメモとして用いながら処理を行えば良い。  
　今回の場合、実際の処理を行うfunctionと、メモを検索するfunctionの二種類が存在し、お互いに相手を呼び合う形になっている。少し処理の流れがわかりづらいが、基本的には

あるn = kの場合の長さを知りたい場合は、まずメモを探してその答えが存在しているかを確認し、あればそれを返す。なければ処理を行う。その中でkより小さいnの値を求める呼び出しを行う→最初へ、

という感じ。  
computeIfAbsent()が便利。

*maxProfitMemo(22),maxProfitMemo2(22,new HashMap())* → [コード](https://github.com/HM-MEA/Functional-c7/blob/master/src/main/java/recur/fpij/RodCutterBasic.java)


## 7.3 まとめ
　p,167

