package util.io;

import java.io.*;

public class FileIO {
    public static String load(String path) {
        String fileData = "";

        File file = new File(path);
        FileReader fileReader = null;
        BufferedReader buffer = null;

        try {
            fileReader = new FileReader(file);
            buffer = new BufferedReader(fileReader);

            String line;
            while ((line = buffer.readLine()) != null)
                fileData += line + '\n';
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null)
                    fileReader.close();
                if (buffer != null)
                    buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileData;
    }

    public static void write(String lines, String path) {
        File file = new File(path);

        if (!file.exists())
            throw new RuntimeException(String.format("File \"%s\" not found", file.getAbsolutePath()));

        FileWriter fileWriter = null;
        BufferedWriter buffer = null;

        try {
            fileWriter = new FileWriter(file);
            buffer = new BufferedWriter(fileWriter);

            buffer.write(lines);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (buffer != null) {
                    buffer.flush();
                    buffer.close();
                }
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
