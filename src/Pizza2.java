import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * This is a template I'm using for the google hashcode problems.
 * It sets up the input and output files along with the readers and writers.
 *
 * Created by: Owomugisha Isaac
 */

public class Pizza2 {

    private static final String[] INPUT_FILENAMES = {"a_example.in", "b_small.in", "c_medium.in", "d_big.in"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_small.out", "c_medium.out", "d_big.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int file = 0; file < INPUT_FILENAMES.length; file++) {
            String FILENAME = INPUT_FILENAMES[file];
            String outputFile = OUTPUT_FILENAMES[file];

            init(FILENAME);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));

            int R = nextInt(), C = nextInt(), L = nextInt(), H = nextInt();

            pizza = new String[R + 2];
            prefixSum = new int[R + 2][C + 2][2];

            for (int i = 1; i < pizza.length - 1; i++) pizza[i] = " " + next() + " ";
            fillPrefixSum(R, C);

            ArrayList<Slice> ans = chooseRows(L, H);
//            ArrayList<Slice> ans = chooseColumns(L, H);

            writer.write(ans.size() + "");
            for (Slice slice: ans)
                writer.write("\n" + (slice.r1 - 1) + " " + (slice.c1 - 1) + " " + (slice.r2 - 1) + " " + (slice.c2 - 1));

            reader.close();
            writer.close();
        }

    }

    private static ArrayList<Slice> chooseRows(int L, int H) {

        ArrayList<Slice> slices = new ArrayList<>();
        int lastCol = 0;
        for (int i = 1; i < pizza.length - 1; i++) {
            int start = 1, end = 1;
            while (end < pizza[1].length() - 1) {
                Slice slice = new Slice(i, start, i, end);
                if (H == 12 && i == 1) System.out.println(slice + ": " + start + ", " + end);
                boolean eligible = slice.isEligible(L);
                int size = slice.getSize();
                end++;
                if (eligible && size <= H) {
                    slices.add(slice);
                    lastCol = Math.max(lastCol, slice.c2);
                    start = end;
                } else {
                    if (size >= H) start += 1;
                }
            }
        }

        // Find remaining
        for (int i = lastCol + 1; i < pizza[1].length() - 1; i++) {
            int start = 1, end = 1;
            while (end < pizza.length - 1) {
                Slice slice = new Slice(start, i, end, i);
                boolean eligible = slice.isEligible(L);
                int size = slice.getSize();
                end++;
                if (eligible && size <= H) {
                    slices.add(slice);
                    start = end;
                } else {
                    if (size >= H) start += 1;
                }
            }
        }

        if (H == 5) System.out.println(slices);

        return slices;
    }

    private static ArrayList<Slice> chooseColumns(int L, int H) {

        ArrayList<Slice> slices = new ArrayList<>();
        for (int i = 1; i < pizza[1].length() - 1; i++) {
            int start = 1, end = 1;
            while (end < pizza.length - 1) {
                Slice slice = new Slice(start, i, end, i);
                boolean eligible = slice.isEligible(L);
                int size = slice.getSize();
                end++;
                if (eligible && size <= H) {
                    slices.add(slice);
                    start = end;
                } else {
                    if (size >= H) start += 1;
                }
            }
        }

        if (H == 5) System.out.println(slices);

        return slices;
    }

    // last dimension: 0 - represents T, 1 - represents M.
    private static int[][][] prefixSum;
    private static String[] pizza;

    private static void fillPrefixSum(int R, int C) {
        for (int i = 1; i <= R; i++) {
            for (int j = 1; j <= C; j++) {
                if (pizza[i].charAt(j) == 'T') prefixSum[i][j][0] = 1;
                else prefixSum[i][j][1] = 1;

                prefixSum[i][j][0] += prefixSum[i - 1][j][0];
                prefixSum[i][j][1] += prefixSum[i - 1][j][1];

                prefixSum[i][j][0] += prefixSum[i][j - 1][0];
                prefixSum[i][j][1] += prefixSum[i][j - 1][1];

                // Subtract double counted part
                prefixSum[i][j][0] -= prefixSum[i - 1][j - 1][0];
                prefixSum[i][j][1] -= prefixSum[i - 1][j - 1][1];
            }
        }
    }

    private static int[] calcIngredients(int r1, int c1, int r2, int c2) {
        int t = prefixSum[r2][c2][0] -
                prefixSum[r2][c1 - 1][0] -
                prefixSum[r1 - 1][c2][0] +
                prefixSum[r1 - 1][c1 - 1][0];

        int m = prefixSum[r2][c2][1] -
                prefixSum[r2][c1 - 1][1] -
                prefixSum[r1 - 1][c2][1] +
                prefixSum[r1 - 1][c1 - 1][1];

        return new int[]{t, m};
    }

    private static class Slice implements Comparable<Slice> {
        int r1, c1, r2, c2;

        Slice(int r1, int c1, int r2, int c2) {
            this.r1 = r1;
            this.c1 = c1;
            this.r2 = r2;
            this.c2 = c2;
        }

        int[] getIngredientsSize() {
            return calcIngredients(r1, c1, r2, c2);
        }

        int getSize() {
            return (r2 - r1 + 1)*(c2 - c1 + 1);
        }

        boolean isEligible(int L) {
            int[] size = getIngredientsSize();
            return (size[0] >= L && size[1] >= L) && !(size[0] > L && size[1] > L);
        }

        @Override
        public int compareTo(Slice other) {
            return (other.r2 - other.r1 + 1)*(other.c2 - other.c1 + 1) - (r2 - r1 + 1)*(c2 - c1 + 1);
        }

        boolean doNotOverlap(Slice other) {
            return other.r1 > r2 || r1 > other.r2 || other.c1 > c2 || c1 > other.c2;
        }

        @Override
        public String toString() {
            return "{" + r1 + " " + c1 + " " + r2 + " " + c2 + "}: " + Arrays.toString(getIngredientsSize());
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
