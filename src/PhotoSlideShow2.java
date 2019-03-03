import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

public class PhotoSlideShow2 {

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

            System.out.println("Slides collected: " + slides.size());
            ArrayList<Slide> ans = new ArrayList<>();

            for (int i = 0; i < slides.size(); i += 1000) {
                int end = Math.min(i + 1000 - 1, slides.size() - 1);

                ans.addAll(collectSlides(i, end));
            }

            ans = simulatedAnnealing(10, 10000, 0.9995, ans);

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
        System.out.println("Start: " + start + ", End: " + end);
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

        LinkedList<Integer> toExplore = new LinkedList<>();
        Set<Integer> explored = new HashSet<>();
        ArrayList<Slide> ans = new ArrayList<>();

        toExplore.addLast(0);
        while (!toExplore.isEmpty()) {
            int i = toExplore.removeFirst();
            explored.add(i);
            int j = 1;
            while (explored.contains(j) && j < adjacentScore.length) {
                j++;
            }
            ans.add(slides.get(i + start));
            if (j < adjacentScore.length) toExplore.addLast(j);
            else break;
        }

        ans = simulatedAnnealing(10, 1000, 0.9995, ans);

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

    private static int calcDifference(int a, int b, ChosenSlides chosenSlides) {
        Slide aa = chosenSlides.slides.get(a);
        Slide aAbove = a == 0 ? null : chosenSlides.slides.get(a - 1);
        Slide aBelow = a == chosenSlides.slides.size() - 1 ? null : chosenSlides.slides.get(a + 1);

        Slide bb = chosenSlides.slides.get(b);
        Slide bAbove = b == 0 ? null : chosenSlides.slides.get(b - 1);
        Slide bBelow = b == chosenSlides.slides.size() - 1 ? null : chosenSlides.slides.get(b + 1);

        int add = calcLocalScore(aAbove, bb, aBelow) + calcLocalScore(bAbove, aa, bBelow);
        int sub = calcLocalScore(aAbove, aa, aBelow) + calcLocalScore(bAbove, bb, bBelow);

        return add - sub;
    }

    private static int calcLocalScore(Slide above, Slide middle, Slide below) {
        int num = 0;
        if (above != null) num += calcScoreForAdjacentSlides(above, middle);
        if (below != null) num += calcScoreForAdjacentSlides(below, middle);

        return num;
    }

    private static ArrayList<Slide> simulatedAnnealing(double startingTemperature, int numberOfIterations, double coolingRate, ArrayList<Slide> slides) {
        System.out.println("Starting SA with temperature: " + startingTemperature + ", # of iterations: "
                + numberOfIterations + " and cooling rate: " + coolingRate);

        double t = startingTemperature;
        ChosenSlides chosenSlides = new ChosenSlides(slides);
        int bestScore = calcScore(chosenSlides.slides);

        System.out.println("Initial score: " + bestScore);

        ArrayList<Slide> bestSlides = new ArrayList<>(chosenSlides.slides);

        for (int i = 0; i < numberOfIterations; i++) {
            if (t > 0.1) {
                int[] indices = chosenSlides.swapSlides();
                int currentScore = chosenSlides.score + calcDifference(indices[0], indices[1], chosenSlides);
                chosenSlides.score = currentScore;
                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    bestSlides = new ArrayList<>(chosenSlides.slides);
                }
                else if (Math.exp((bestScore - currentScore) / t) < Math.random()) chosenSlides.revertSwap();

                t *= coolingRate;
            } else continue;

            if (i % (numberOfIterations/10) == 0) System.out.println("Iteration #" + i + "; Current best score: " + bestScore);
        }

        return bestSlides;
    }

    private static class ChosenSlides {
        ArrayList<Slide> slides;
        ArrayList<Slide> previousSlides;
        int score, prevScore;

        ChosenSlides(ArrayList<Slide> slides) {
            this.slides = new ArrayList<>(slides);
            previousSlides = new ArrayList<>(this.slides);
            score = calcScore(this.slides);
            prevScore = score;
        }

        int[] swapSlides() {
            int a = generateRandomIndex(), b = generateRandomIndex();

            previousSlides = new ArrayList<>(slides);
            prevScore = score;

            Slide x = slides.get(a), y = slides.get(b);

            slides.set(a, y);
            slides.set(b, x);

            return new int[]{a, b};
        }

        void revertSwap() {
            slides = previousSlides;
            score = prevScore;
        }

        int generateRandomIndex() {
            return (int) (Math.random() * slides.size());
        }
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
