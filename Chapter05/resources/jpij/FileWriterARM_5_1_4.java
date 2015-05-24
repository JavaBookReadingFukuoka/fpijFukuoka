package Chapter05.resources.jpij;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterARM_5_1_4 implements AutoCloseable {
    private final FileWriter writer;
    private final long id;

    private static long totalInstances = 0l;

    public FileWriterARM_5_1_4(final String fileName) throws IOException {
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
        try (final FileWriterARM_5_1_4 writerExample = new FileWriterARM_5_1_4("peekaboo.txt")) {
            writerExample.writeStuff("peek-a-boo");
        }

         /*================================================================================
          * try-with-resourcesを使うと，自動でclose()が呼ばれた！
          *  しかし，全ての開発者がARM(Automatic Resource Management)を忘れず書けるか？
          *===============================================================================*/
    }
}