import java.io.FileWriter;
import java.io.IOException;

public class FileWriterExample {
    private final FileWriter writer;
    private static boolean isClosed = false;
    private static long totalBytes = 0l;

    public FileWriterExample(final String fileName) throws IOException {
        writer = new FileWriter(fileName);
    }

    public void writeStuff(final String message) throws IOException {
        writer.write(message);
        totalBytes += message.getBytes().length;
        System.out.println(String.format("%d \t[B]", totalBytes));
    }

    public void finalize() throws IOException {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writer.close();
        isClosed = true;
    }

    public void close() throws IOException {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writer.close();
        isClosed = true;
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        while (!isClosed) {
            /*================================================================================
             * writerは閉じられない
             *  JVMは大きなメモリを抱えているので，GCを実行する必要が発生しない。
             *  GC（finalize）はいつ発生するのか？
             *  -Xms1m -Xmx1mで実験してみよう！
             *===============================================================================*/
            final FileWriterExample writerExample = new FileWriterExample("peekaboo.txt");
            writerExample.writeStuff("peek-a-boo");
            Thread.sleep(100);
        }

         /*================================================================================
          * 明示的に開放する
          *  例外発生時にもcloseは呼ばれますか？ - NO!
          *===============================================================================*/
        //writerExample.close();

         /*================================================================================
          * peek-a-booとは？
          *  「いないいないばあ」
          *  発達心理学の概念を用いて言えば、いないいないばあを喜ぶのは、個人差はあるものの、
          *  自我が芽生え自己と他者の分離が始まる生後6ヶ月以降の赤ちゃんである。
          *  いないいないばあをしている相手を他者として認識し、「いないいない」という一時的な分離から
          *  再会を予期した後に、「ばあ」と予期通りに再会が叶う事に喜びや興奮を感じているものと思われる。
          *  - Wikipedia
          *================================================================================*/
    }
}