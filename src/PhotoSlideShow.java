import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;

public class PhotoSlideShow {

    private static final String[] INPUT_FILENAMES = {"a_example.txt", "b_lovely_landscapes.txt",
            "c_memorable_moments.txt", "d_pet_pictures.txt", "e_shiny_selfies.txt"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_lovely_landscapes.out",
            "c_memorable_moments.out", "d_pet_pictures.out", "e_shiny_selfies.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int file = 0; file < INPUT_FILENAMES.length; file++) {
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
                if (photo.orientation == 'H') {
                    slides.add(new Slide(i, -1, photo.tags, null, slides.size()));
                    updateSlideValue(slides.size() - 1);
                }
                else {
                    verticals.add(photo);
                    if (verticals.size() == 2) {
                        Photo photo1 = verticals.get(0);
                        Photo photo2 = verticals.get(1);

                        slides.add(new Slide(photo1.index, photo2.index, photo1.tags, photo2.tags, slides.size()));
                        updateSlideValue(slides.size() - 1);
                        verticals = new ArrayList<>();
                    }
                }
            }

            Collections.shuffle(slides);
            System.out.println("Slides collected: " + slides.size());
            localSearch(((int)Math.pow(10, 7))/slides.size());
            ArrayList<Slide> ans = new ArrayList<>(slides);


            System.out.println("Score is: " + calcScore(ans));
            System.out.println("=============================");
            writer.write(ans.size() + " ");
            for (Slide slide: ans) writer.write("\n" + slide);

            reader.close();
            writer.close();
        }

    }

    private static ArrayList<Slide> slides;

    private static void localSearch(int numOfIterations) {
        System.out.println("Number of iterations: " + numOfIterations);
        System.out.println("Score at start: " + calcScore(slides));

        PriorityQueue<Slide> slidePriorityQueue = new PriorityQueue<>(slides);

        for (int i = 0; i < numOfIterations; i++) {
            Slide min = slidePriorityQueue.peek();
            int maxIncrement = 0;
            assert min != null;
            int maxIncrementIndex = min.index;

            for (int j = 0; j < slides.size(); j++) {
                if (j != min.index) {
                    int increment = calcDifference(j, min.index, slides);
                    if (increment > maxIncrement) {
                        maxIncrement = increment;
                        maxIncrementIndex = j;
                    }
                }
            }

            if (maxIncrementIndex != min.index) {
                int a = min.index;

                Slide aAbove = a > 0 ? slides.get(a - 1) : null;
                Slide aMiddle = slides.get(maxIncrementIndex);
                Slide aBelow = a < slides.size() - 1 ? slides.get(a + 1) : null;

                Slide bAbove = maxIncrementIndex > 0 ? slides.get(maxIncrementIndex - 1) : null;
                Slide bMiddle = slides.get(a);
                Slide bBelow = maxIncrementIndex < slides.size() - 1 ? slides.get(maxIncrementIndex + 1) : null;

                if (aAbove != null) {
                    slidePriorityQueue.remove(aAbove);
                    aAbove.value = calcLocalScore(getAbove(aAbove, true), aAbove, aMiddle);
                }
                slidePriorityQueue.remove(aMiddle);
                aMiddle.value = calcLocalScore(aAbove, aMiddle, aBelow);
                if (aBelow != null) {
                    slidePriorityQueue.remove(aBelow);
                    aBelow.value = calcLocalScore(aMiddle, aBelow, getAbove(aBelow, false));
                }

                if (bAbove != null) {
                    slidePriorityQueue.remove(bAbove);
                    bAbove.value = calcLocalScore(getAbove(bAbove, true), bAbove, bMiddle);
                }
                slidePriorityQueue.remove(bMiddle);
                bMiddle.value = calcLocalScore(bAbove, bMiddle, bBelow);
                if (bBelow != null) {
                    slidePriorityQueue.remove(bBelow);
                    bBelow.value = calcLocalScore(bMiddle, bBelow, getAbove(bBelow, false));
                }

                slides.set(a, aMiddle); slides.set(maxIncrementIndex, bMiddle);
                if (aAbove != null) slidePriorityQueue.add(aAbove);
                slidePriorityQueue.add(aMiddle);
                if (aBelow != null) slidePriorityQueue.add(aBelow);
                if (bAbove != null) slidePriorityQueue.add(bAbove);
                slidePriorityQueue.add(bMiddle);
                if (bBelow != null) slidePriorityQueue.add(bBelow);
            }
        }
    }

    private static void updateSlideValue(int index) {
        if (index > 0) {
            Slide slide = slides.get(index);
            Slide above = slides.get(index - 1);

            int value = calcScoreForAdjacentSlides(slide, above);
            slide.value += value;
            above.value += value;
        }
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

    private static int calcDifference(int a, int b, List<Slide> slides) {
        Slide aa = slides.get(a);
        Slide aAbove = a == 0 ? null : slides.get(a - 1);
        Slide aBelow = a == slides.size() - 1 ? null : slides.get(a + 1);

        Slide bb = slides.get(b);
        Slide bAbove = b == 0 ? null : slides.get(b - 1);
        Slide bBelow = b == slides.size() - 1 ? null : slides.get(b + 1);

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

    private static Slide getAbove(Slide slide, boolean above) {
        int index = slide.index + (above ? -1 : 1);
        if (index < 0 || index == slides.size()) return null;
        return slides.get(index);
    }

    private static class Slide implements Comparable<Slide> {
        int id1, id2;
        Set<String> tags;
        int value;
        int index;


        Slide(int id1, int id2, Set<String> tags1, Set<String> tags2, int index) {
            this.id1 = id1;
            this.id2 = id2;
            this.tags = new HashSet<>();
            this.tags.addAll(tags1);
            if (tags2 != null) this.tags.addAll(tags2);
            this.index = index;
        }

        @Override
        public String toString() {
            return id1 + (id2 >= 0 ? " " + id2 : "");
        }

        @Override
        public boolean equals(Object obj) {
            assert obj instanceof Slide;
            Slide other = (Slide) obj;

            return other.id2 == id2 && other.id1 == id1;
        }

        @Override
        public int compareTo(Slide other) {
            return -value + other.value;
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
