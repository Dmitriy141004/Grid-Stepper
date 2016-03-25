 package util;

import java.io.*;

public class SimpleFileIO {
    public static String load(String path) throws IOException {
        String fileData = "";

        File fileObj = new File(path);
        FileReader file = null;
        BufferedReader buffer = null;

        try {
            file = new FileReader(fileObj);
            buffer = new BufferedReader(file, (int) fileObj.length());

            String line;
            while ((line = buffer.readLine()) != null) fileData += line + '\n';
        } finally {
            if (buffer != null) buffer.close();
            if (file != null) file.close();
        }

        return fileData;
    }

    public static void write(String lines, String path) throws IOException {
        FileWriter file = null;
        BufferedWriter buffer = null;

        File fileObj = new File(path);
        if (!fileObj.exists()) throw new FileNotFoundException();

        try {
            file = new FileWriter(fileObj);
            buffer = new BufferedWriter(file);

            buffer.write(lines);
        } finally {
            if (buffer != null) {
                buffer.flush();
                buffer.close();
            }
            if (file != null) file.close();
        }
    }
}
