import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.StringTokenizer;

public class Pizza {

    private static final String[] INPUT_FILENAMES = {"a_example.in", "b_small.in", "c_medium.in", "d_big.in"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_small.out", "c_medium.out", "d_big.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int i1 = 0; i1 < INPUT_FILENAMES.length; i1++) {
            String FILENAME = INPUT_FILENAMES[i1];
            String outputFile = OUTPUT_FILENAMES[i1];
            init(FILENAME);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));
            int R = nextInt(), C = nextInt(), L = nextInt(), H = nextInt();
            prefixSum = new int[R][C][2];
            pizza = new String[R];

            for (int i = 0; i < pizza.length; i++) pizza[i] = next();

            fillPrefixSum(R, C);
//            writer.write("File: " + FILENAME + " L & H: " + L + " " + H + " Rows & Columns: " + R + " " + C);
            chooseAns(1, L, H, writer);

            reader.close();
            writer.close();
        }

    }

    private static void chooseAns(int runs, int L, int H, BufferedWriter writer) throws IOException {
        int maxSize = 0;
        ArrayList<Slice> ans = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            ArrayList<Slice> candAns = solve1(L, H);
            if (candAns.size() > maxSize) {
                maxSize = candAns.size();
                ans = candAns;
            }
        }

        writer.write(ans.size() + "");
        for (Slice slice: ans) writer.write("\n" + slice.r1 + " " + slice.c1 + " " + slice.r2 + " " + slice.c2);
    }

    // last dimension: 0 - represents T, 1 - represents M.
    private static int[][][] prefixSum;
    private static String[] pizza;

    private static void fillPrefixSum(int R, int C) {
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++) {
                if (pizza[i].charAt(j) == 'T') prefixSum[i][j][0] = 1;
                else prefixSum[i][j][1] = 1;

                if (i > 0) {
                    prefixSum[i][j][0] += prefixSum[i - 1][j][0];
                    prefixSum[i][j][1] += prefixSum[i - 1][j][1];
                }

                if (j > 0) {
                    prefixSum[i][j][0] += prefixSum[i][j - 1][0];
                    prefixSum[i][j][1] += prefixSum[i][j - 1][1];
                }

                // Subtract double counted part
                if (i > 0 && j > 0) {
                    prefixSum[i][j][0] -= prefixSum[i - 1][j - 1][0];
                    prefixSum[i][j][1] -= prefixSum[i - 1][j - 1][1];
                }
            }
        }
    }

    private static int[] calcIngredients(int r1, int c1, int r2, int c2) {
        int t = prefixSum[r2][c2][0] -
                prefixSum[r2][c1 > 0 ? c1 - 1 : 0][0] -
                prefixSum[r1 > 0 ? r1 - 1 : 0][c2][0] +
                prefixSum[r1 > 0 ? r1 - 1 : 0][c1 > 0 ? c1 - 1 : 0][0];

        int m = prefixSum[r2][c2][1] -
                prefixSum[r2][c1 > 0 ? c1 - 1 : 0][1] -
                prefixSum[r1 > 0 ? r1 - 1 : 0][c2][1] +
                prefixSum[r1 > 0 ? r1 - 1 : 0][c1 > 0 ? c1 - 1 : 0][1];

        return new int[]{t, m};
    }

    private static ArrayList<Slice> solve1(int L, int H) {
        ArrayList<Slice> slices = new ArrayList<>();
        Random random = new Random();
        long maxIterations = Long.MAX_VALUE;
        if (H == 14) maxIterations = 10000;
        long maxCount = 0;

        for (int r1 = 0; r1 < prefixSum.length; r1++) {
            long count = 0;
            for (int c1 = 0; c1 < prefixSum[0].length; c1++) {
                int rowAdd = 0;
                int colAdd = 0;

                while ((rowAdd + 1)*(colAdd + 1) <= H
                        && (r1 + rowAdd) < prefixSum.length
                        && (c1 + colAdd) < prefixSum[0].length) {
                    Slice slice = new Slice(r1, c1, r1 + rowAdd, c1 + colAdd);

                    if (slice.isEligible(L)) slices.add(slice);

                    int ar = 0, ac = 0;
                    while (ac == 0 && ar == 0) {
                        ar = H < 15 ? (r1 + rowAdd) < prefixSum.length - 1 ? 1 : 0 : random.nextInt(2);
                        ac = H < 15 ? (r1 + rowAdd) < prefixSum.length - 1 ? 0 : 1 : random.nextInt(2);
//                        if (H >= 13) System.out.println(ar + " " + ac);
                    }
                    rowAdd += ar;
                    colAdd += ac;
                    count++;
                    if (count >= maxIterations) break;
                }
            }
            maxCount = Math.max(maxCount, count);
        }

        slices.sort(Slice::compareTo); // Better performance
//        slices.sort(Collections.reverseOrder(Slice::compareTo)); // Worse performance
//        Collections.shuffle(slices);

//        System.out.println("New data set: " + L + " " + H);
//        for (int i = 0; i < Math.min(10, slices.size()); i++) {
//            Slice slice = slices.get(i);
//            System.out.println(i + " " + (slice.r2 - slice.r1 + 1)*(slice.c2 - slice.c1 + 1));
//        }
        boolean[] toRemove = new boolean[slices.size()];
        for (int i = 0; i < slices.size(); i++) {
            for (int j = i + 1; j < slices.size(); j++) {
                if (toRemove[i]) break;
                if (!slices.get(i).doNotOverlap(slices.get(j))) toRemove[j] = true;
            }
        }

        ArrayList<Slice> ans = new ArrayList<>();
        for (int i = 0; i < slices.size(); i++) {
            if (!toRemove[i]) ans.add(slices.get(i));
        }

        System.out.println("Max iterations performed for H = " + H + ": " + maxCount);
        System.out.println("Slices size: " + slices.size());
        System.out.println("Chosen slices: " + ans.size());
        return ans;
    }

    private static class Slice implements Comparable<Slice> {
        int r1, c1, r2, c2;

        Slice(int r1, int c1, int r2, int c2) {
            this.r1 = r1;
            this.c1 = c1;
            this.r2 = r2;
            this.c2 = c2;
        }

        int[] getSize() {
            return calcIngredients(r1, c1, r2, c2);
        }

        boolean isEligible(int L) {
            int[] size = getSize();
            return size[0] >= L && size[1] >= L;
        }

        @Override
        public int compareTo(Slice other) {
            return (other.r2 - other.r1 + 1)*(other.c2 - other.c1 + 1) - (r2 - r1 + 1)*(c2 - c1 + 1);
        }

        boolean doNotOverlap(Slice other) {
            return other.r1 > r2 || r1 > other.r2 || other.c1 > c2 || c1 > other.c2;
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
