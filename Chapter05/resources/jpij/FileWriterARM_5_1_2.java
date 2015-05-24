package Chapter05.resources.jpij;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterARM_5_1_2 {
    private final FileWriter writer;
    private final long id;

    private static long totalInstances = 0l;

    public FileWriterARM_5_1_2(final String fileName) throws IOException {
        writer = new FileWriter(fileName);
        id = ++totalInstances;
        System.out.println(String.format("[Create #]\t %05d", id));
    }

    public void writeStuff(final String message) throws IOException {
        writer.write(message);
    }

    public void close() throws IOException {
        writer.close();
        System.out.println(String.format("[Close #]\t %05d", id));
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        final FileWriterARM_5_1_2 writerExample = new FileWriterARM_5_1_2("peekaboo.txt");
        writerExample.writeStuff("peek-a-boo");

         /*================================================================================
          * 明示的に開放する
          *  例外発生時にもcloseは呼ばれますか？ - NO!
          *===============================================================================*/
        writerExample.close();
    }
}