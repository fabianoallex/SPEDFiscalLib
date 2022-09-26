import java.io.FileWriter;
import java.io.PrintWriter;

public interface Writer {
    void write(String string);
}

class SPEDFileWriter implements Writer {
    private final PrintWriter printWriter;

    public SPEDFileWriter(FileWriter fileWriter) {
        this.printWriter = new PrintWriter(fileWriter);
    }

    @Override
    public void write(String string) {
        this.printWriter.println(string);
    }
}

record SPEDStringBuilder(StringBuilder stringBuilder) implements Writer {
    @Override
    public void write(String string) {
        this.stringBuilder.append(string);
        this.stringBuilder.append("\n\r");
    }
}