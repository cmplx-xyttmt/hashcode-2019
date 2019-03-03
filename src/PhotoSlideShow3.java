import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class PhotoSlideShow3 {

    private static final String[] INPUT_FILENAMES = {"a_example.txt", "b_lovely_landscapes.txt",
            "c_memorable_moments.txt", "d_pet_pictures.txt", "e_shiny_selfies.txt"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_lovely_landscapes.out",
            "c_memorable_moments.out", "d_pet_pictures.out", "e_shiny_selfies.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int file = 2; file < INPUT_FILENAMES.length; file++) {
            String FILENAME = INPUT_FILENAMES[file];
            String outputFile = OUTPUT_FILENAMES[file];

            init(FILENAME);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));

            int N = nextInt();
            System.out.println("Test file: " + INPUT_FILENAMES[file]);
            ArrayList<Photo> photos = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                Photo photo = new Photo(next().charAt(0), nextInt(), i);
                for (int j = 0; j < photo.numOfTags; j++) {
                    photo.tags.add(next());
                }
                photos.add(photo);
            }

            slides = new ArrayList<>();
            ArrayList<Photo> verticals = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                if (photo.orientation == 'H') slides.add(new Slide(i, -1, photo.tags, null));
                else {
                    verticals.add(photo);
                    if (verticals.size() == 2) {
                        Photo photo1 = verticals.get(0);
                        Photo photo2 = verticals.get(1);

                        slides.add(new Slide(photo1.index, photo2.index, photo1.tags, photo2.tags));
                        verticals = new ArrayList<>();
                    }
                }
            }

            slides.sort(Collections.reverseOrder());
            System.out.println("Slides collected: " + slides.size());
            ArrayList<Slide> ans = new ArrayList<>();

            for (int i = 0; i < slides.size(); i += 5000) {
                int end = Math.min(i + 5000 - 1, slides.size() - 1);

                ans.addAll(collectSlides(i, end));
            }

            System.out.println("Score is: " + calcScore(ans));
            System.out.println("=============================");
            writer.write(ans.size() + " ");
            for (Slide slide: ans) writer.write("\n" + slide);

            reader.close();
            writer.close();
        }

    }

    private static ArrayList<Slide> slides;

    private static ArrayList<Slide> collectSlides(int start, int end) {
        int slidesToTake = end - start + 1;
//        System.out.println(slidesToTake);
        Integer[][] adjacentScore = new Integer[slidesToTake][slidesToTake];

        for (int i = 0; i < adjacentScore.length; i++) {
            Arrays.fill(adjacentScore[i], 0);
            for (int j = i + 1; j < adjacentScore.length; j++) {
                int score = calcScoreForAdjacentSlides(slides.get(i), slides.get(j));
                adjacentScore[i][j] = score;
                adjacentScore[j][i] = score;
            }
            Arrays.sort(adjacentScore[i], Collections.reverseOrder());
        }

        int[][] dataMatrix = new int[slidesToTake][slidesToTake];
        for (int i = 0; i < adjacentScore.length; i++) {
            Integer[] integers = adjacentScore[i];
            dataMatrix[i] = Arrays.stream(integers).mapToInt(Integer::intValue).toArray();
        }
        HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);

        int[][] assignment = ha.findOptimalAssignment();

        Set<Integer> explored = new HashSet<>();

        Map<Integer, Integer> adjacency = new HashMap<>();
        for (int[] ints : assignment) {
            int a = ints[0], b = ints[1];
            if (a != b) {
                adjacency.put(a, b);
            }
        }

        ArrayList<Slide> ans = new ArrayList<>();
        for (Integer integer: adjacency.keySet()) {
            int begin = integer;
            while (!explored.contains(begin)) {
                explored.add(begin);
                ans.add(slides.get(begin + start));
                begin = adjacency.get(begin);
            }
        }

        return ans;
    }

    private static int calcScore(ArrayList<Slide> slides) {
        int score = 0;
        for (int i = 1; i < slides.size(); i++) {
            score += calcScoreForAdjacentSlides(slides.get(i - 1), slides.get(i));
        }

        return score;
    }

    private static class Photo {
        char orientation;
        int numOfTags;
        int index;
        Set<String> tags;

        Photo(char orientation, int numOfTags, int index) {
            this.orientation = orientation;
            this.numOfTags = numOfTags;
            this.index = index;
            tags = new HashSet<>(numOfTags);
        }

        @Override
        public String toString() {
            return orientation + " " + numOfTags + " " + tags;
        }
    }

    private static int calcScoreForAdjacentSlides(Slide slide1, Slide slide2) {
        Set<String> both = new HashSet<>(slide1.tags);
        both.retainAll(slide2.tags);
        int inBoth =  both.size();

        both = new HashSet<>(slide1.tags);
        both.removeAll(slide2.tags);
        int in1 = both.size();

        both = new HashSet<>(slide2.tags);
        both.removeAll(slide1.tags);
        int in2 = both.size();

        return Math.min(inBoth, Math.min(in1, in2));
    }

    private static class Slide implements Comparable<Slide> {
        int id1, id2;
        Set<String> tags;

        Slide(int id1, int id2, Set<String> tags1, Set<String> tags2) {
            this.id1 = id1;
            this.id2 = id2;
            this.tags = new HashSet<>();
            this.tags.addAll(tags1);
            if (tags2 != null) this.tags.addAll(tags2);
        }

        @Override
        public String toString() {
            return id1 + (id2 >= 0 ? " " + id2 : "");
        }

        @Override
        public int compareTo(Slide other) {
            return tags.size() - other.tags.size();
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
