import java.io.IOException;

public class FileWriterExample {
    private final FileWriter writer;

    public FileWriterExample(final String fileName) throws IOException {
        writer = new FileWriterExample(fileName);
    }
    public void writeStuff(final String message) throws IOException {
        writer.write(message);
    }
    public void finalize() throws IOException {
        writer.close();
    }
    //...

     public static void main(final String[] args) throws IOException {
         final FileWriterExample wirterExample = new FileWriterExample("peekaboo.txt");
         wirterExample.writeStuff("peek-a-boo");

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