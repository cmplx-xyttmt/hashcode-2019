import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class CalcScore {

    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_small.out", "c_medium.out", "d_big.out"};

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < OUTPUT_FILENAMES.length; i++) {
            init(OUTPUT_FILENAMES[i]);
            int score = 0;
            int s = nextInt();
            for (int j = 0; j < s; j++) {
                int r1 = nextInt(), c1 = nextInt(), r2 = nextInt(), c2 = nextInt();

                score += (r2 - r1 + 1)*(c2 - c1 + 1);
            }

            System.out.println("Score for " + i + ": " + score);
            reader.close();
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
}
