import java.io.*;
import java.util.StringTokenizer;

public class Template {

    private static final String[] INPUT_FILENAMES = {"a_example.in", "b_small.in", "c_medium.in", "d_big.in"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_small.out", "c_medium.out", "d_big.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int file = 0; file < INPUT_FILENAMES.length; file++) {
            String FILENAME = INPUT_FILENAMES[file];
            String outputFile = OUTPUT_FILENAMES[file];

            init(FILENAME);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));


            reader.close();
            writer.close();
        }

    }

    //Input Reader
    private static BufferedReader reader;
    private static StringTokenizer tokenizer;

    private static void init(String filename) throws IOException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
        tokenizer = new StringTokenizer("");
    }

    private static String next() throws IOException {
        String read;
        while (!tokenizer.hasMoreTokens()) {
            read = reader.readLine();
            if (read == null || read.equals(""))
                return "-1";
            tokenizer = new StringTokenizer(read);
        }

        return tokenizer.nextToken();
    }

    private static int nextInt() throws IOException {
        return Integer.parseInt(next());
    }

//    private static long nextLong() throws IOException {
//        return Long.parseLong(next());
//    }
//
//    //    Get a whole line.
//    private static String line() throws IOException {
//        return reader.readLine();
//    }
//
//    private static double nextDouble() throws IOException {
//        return Double.parseDouble(next());
//    }
}