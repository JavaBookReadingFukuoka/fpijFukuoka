package Chapter05.resources.jpij;

import java.io.FileWriter;
import java.io.IOException;

public class FileWriterEAM_5_2_3 {
    private final FileWriter writer;
    private final long id;

    private static long totalInstances = 0l;

    public FileWriterEAM_5_2_3(final String fileName) throws IOException {
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

    @FunctionalInterface
    public interface UseInstance<T, X extends Throwable> {
        void accept(T instance) throws X;
    }

    public static void use(final String fileName,
                           final UseInstance<FileWriterEAM_5_2_3, IOException> block) throws IOException {
        final FileWriterEAM_5_2_3 writerEAM = new FileWriterEAM_5_2_3(fileName);
        try {
            block.accept(writerEAM);
        } finally {
            writerEAM.close();
        }
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        FileWriterEAM_5_2_3.use("eam.txt", writerEAM -> writerEAM.writeStuff("sweet"));

        FileWriterEAM_5_2_3.use("eam2.txt", writerEAM -> {
            writerEAM.writeStuff("how");
            writerEAM.writeStuff("sweet");
        });

         /*================================================================================
          * ラムダ式を使うとtryすら必要がない！
          *  APIのユーザがtry-with-resourcesを書かなくても，自動でリソースが解放される
          *===============================================================================*/
    }
}