package Chapter05.resources;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterARM_5_1_1 {

    private final FileWriter writer;
    private final long id;

    private static boolean isClosed = false;
    private static long totalInstances = 0l;

    public FileWriterARM_5_1_1(final String fileName) throws IOException {
        writer = new FileWriter(fileName);
        id = ++totalInstances;
        System.out.println(String.format("[Create #]\t %05d", id));
    }

    public void writeStuff(final String message) throws IOException {
        writer.write(message);
    }

    public void finalize() throws IOException {
        writer.close();
        isClosed = true;
        System.out.println(String.format("[Finalize #]\t %05d", id));
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        while (!isClosed) {
            /*================================================================================
             * writerは閉じられない
             *  JVMは大きなメモリを抱えているので，GCを実行する必要が発生しない。
             *  GC（finalize）はいつ発生するのか？
             *  -Xms1m -Xmx1mで実験してみよう！
             *===============================================================================*/
            final FileWriterARM_5_1_1 writerExample = new FileWriterARM_5_1_1("peekaboo.txt");
            writerExample.writeStuff("peek-a-boo");
            Thread.sleep(100);
        }

         /*================================================================================
          * peek-a-booとは？
          *  「いないいないばあ」
          *  発達心理学の概念を用いて言えば、いないいないばあを喜ぶのは、個人差はあるものの、
          *  自我が芽生え自己と他者の分離が始まる生後6ヶ月以降の赤ちゃんである。
          *  いないいないばあをしている相手を他者として認識し、「いないいない」という一時的な分離から
          *  再会を予期した後に、「ばあ」と予期通りに再会が叶う事に喜びや興奮を感じているものと思われる。
          *  - Wikipedia
          *===============================================================================*/
    }
}