import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

public class PhotoSlideShow {

    private static final String[] INPUT_FILENAMES = {"a_example.txt", "b_lovely_landscapes.txt",
            "c_memorable_moments.txt", "d_pet_pictures.txt", "e_shiny_selfies.txt"};
    private static final String[] OUTPUT_FILENAMES = {"a_example.out", "b_lovely_landscapes.out",
            "c_memorable_moments.out", "d_pet_pictures.out", "e_shiny_selfies.out"};

    public static void main(String[] args) throws IOException {
        BufferedWriter writer;
        for (int file = 3; file < INPUT_FILENAMES.length; file++) {
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
            slideTags = new HashMap<>();
            ArrayList<Photo> verticals = new ArrayList<>();
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                if (photo.orientation == 'H') {
                    slides.add(new Slide(i, -1, photo.tags, null, slides.size()));
                    updateSlideValue(slides.size() - 1);
                    updateSlideTags(slides.get(slides.size() - 1));
                } else verticals.add(photo);
            }

            verticals.sort(Photo::compareTo);
            for (int i = 0; i < verticals.size(); i++) {
                Photo photo1 = verticals.get(i);
                int j = i + 1;
                i++;
                Photo photo2 = verticals.get(j);

                slides.add(new Slide(photo1.index, photo2.index, photo1.tags, photo2.tags, slides.size()));
                updateSlideValue(slides.size() - 1);
                updateSlideTags(slides.get(slides.size() - 1));
            }

            int numOfSlidesToConsider = 0;
            for (String tag : slideTags.keySet()) numOfSlidesToConsider += slideTags.get(tag).size();

            for (Slide slide1 : slides) {
                slide1.updateValue();
            }

            System.out.println("Slides collected: " + slides.size());
            System.out.println("Total number of tags: " + slideTags.size());
            System.out.println("Slides to consider for all tags: " + numOfSlidesToConsider);
            int avgNumOfSlidesPerTag = numOfSlidesToConsider / slideTags.size();
            System.out.println("Average number of slides per tag: " + (1.0 * numOfSlidesToConsider) / slideTags.size());
//            ArrayList<Slide> ans = localSearch((int) (Math.pow(10, 5)) / avgNumOfSlidesPerTag);
//            ArrayList<Slide> ans = chooseSharing((int) (Math.pow(10, 5)) / avgNumOfSlidesPerTag);
//            ArrayList<Slide> ans = chooseSharingWhileOrderingTags((int) (Math.pow(10, 5)) / avgNumOfSlidesPerTag);
            ArrayList<Slide> ans = greedy(slides);


            System.out.println("Score is: " + calcScore(ans));
            System.out.println("=============================");
            writer.write(ans.size() + " ");
            for (Slide slide : ans) writer.write("\n" + slide);

            reader.close();
            writer.close();
        }

    }

    private static ArrayList<Slide> slides;
    private static HashMap<String, ArrayList<Integer>> slideTags;

    private static ArrayList<Slide> greedy(ArrayList<Slide> slides) {
        // TODO: Use photos before turning them into slides

        ArrayList<Slide> slidesToTake = new ArrayList<>();
        HashSet<Integer> takenSlides = new HashSet<>();

        int currSlide = 0;
        while (true) {
            takenSlides.add(currSlide);
            slidesToTake.add(slides.get(currSlide));
            if (slidesToTake.size() == slides.size()) break;
            Set<Integer> slidesToConsider = new HashSet<>();
            int bestSlide = currSlide;
            int bestScore = -1;
            for (String tag : slides.get(currSlide).tags) {
                if (slidesToConsider.size() == 0)
                    slidesToConsider.addAll(slideTags.get(tag));
                else slidesToConsider.retainAll(slideTags.get(tag));
                slidesToConsider.removeAll(takenSlides);
                if (slidesToConsider.size() < 1000) break;
            }

            if (slidesToTake.size() % 1000 == 0)
                System.out.println("Size of slides to consider: " + slidesToConsider.size()
                        + " Current size of sol: " + slidesToTake.size()
                        + " Current score: " + calcScore(slidesToTake));
            int counter = 0;
            for (int j : slidesToConsider) {
                if (!takenSlides.contains(j)) {
                    int score = calcScoreForAdjacentSlides(slides.get(currSlide), slides.get(j));
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlide = j;
                    }
                }
                counter++;
//                if (counter > 1000 && bestSlide != currSlide) break;
            }
            if (bestSlide != currSlide) currSlide = bestSlide;
            else {
                for (int i = 0; i < slides.size(); i++) {
                    if (!takenSlides.contains(i)) {
                        currSlide = i;
                        break;
                    }
                }
            }
        }

        return slidesToTake;
    }

    private static ArrayList<Slide> chooseSharing(int numOfIterations) {
        Set<Integer> taken = new HashSet<>();
        ArrayList<Slide> slidesToTake = new ArrayList<>();
        for (String tag : slideTags.keySet()) {
            for (int index : slideTags.get(tag)) {
                if (!taken.contains(index)) {
                    slidesToTake.add(slides.get(index));
                    taken.add(index);
                }
            }
        }

        slides = slidesToTake;
        return localSearch(numOfIterations);
    }

    private static ArrayList<Slide> chooseSharingWhileOrderingTags(int numOfIterations) {
        Set<Integer> taken = new HashSet<>();
        ArrayList<Slide> slidesToTake = new ArrayList<>();
        Set<String> tagsConsidered = new HashSet<>();
        LinkedList<String> tagsQueue = new LinkedList<>();

        String tag = "";
        Set<String> tagsNotConsidered = new HashSet<>(slideTags.keySet());
        while (true) {
            tagsNotConsidered.removeAll(tagsConsidered);
            System.out.println("Current tag size: " + tagsNotConsidered.size() + "; Current score: " + calcScore(slidesToTake));
            if (tagsNotConsidered.isEmpty()) break;
            tag = tagsNotConsidered.stream().findFirst().orElse(tag);
            tagsQueue.addLast(tag);
            while (!tagsQueue.isEmpty()) {
                tag = tagsQueue.removeFirst();
                tagsConsidered.add(tag);
                ArrayList<Integer> get = slideTags.get(tag);
                for (int i = 0; i < get.size(); i++) {
                    int index = get.get(i);
                    if (!taken.contains(index)) {
                        slidesToTake.add(slides.get(index));
                        taken.add(index);
                        if (i == get.size() - 1) {
                            for (String tg : slides.get(index).tags) {
                                if (!tagsConsidered.contains(tg) && slideTags.get(tg).size() > 1) {
                                    tagsQueue.addLast(tg);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        slides = slidesToTake;
        return localSearch(numOfIterations);
    }

    private static ArrayList<Slide> localSearch(int numOfIterations) {
        System.out.println("Number of iterations: " + numOfIterations);
        System.out.println("Score at start: " + calcScore(slides));
        ArrayList<Slide> changingSlides = new ArrayList<>(slides);
//        Collections.shuffle(changingSlides);
//        changingSlides.sort(Slide::compareTo);
        for (int i = 0; i < slides.size(); i++) {
            slides.get(i).value = 0;
            updateSlideValue(i);
        }
//        slides.sort(Slide::compareTo);
        slides.sort(Collections.reverseOrder(Slide::compareTo));
        System.out.println("Before values");
        for (int i = 0; i < Math.min(slides.size(), 100); i++) {
            System.out.print(slides.get(i).value + ", ");
        }
        System.out.println();
//        changingSlides.sort(Collections.reverseOrder(Slide::compareTo));
        HashMap<Integer, Integer> trackChangedPositions = new HashMap<>();
        for (int i = 0; i < changingSlides.size(); i++) trackChangedPositions.put(changingSlides.get(i).index, i);

        for (int i = 0; i < Math.min(numOfIterations, changingSlides.size()); i++) {
            Slide min = slides.get(i);
            int maxIncrement = 0;
            assert min != null;
            int minIndex = trackChangedPositions.get(min.index);
            int maxIncrementIndex = minIndex;

            for (String tag : min.tags) {
                ArrayList<Integer> consideringSlides = slideTags.get(tag);
                for (Integer consideringSlide : consideringSlides) {
                    if (consideringSlide == 0) consideringSlide += 1;
                    else if (consideringSlide == slides.size() - 1) consideringSlide -= 1;
                    else consideringSlide += (consideringSlide % 2 == 0 ? -1 : 1);
                    Slide slide = slides.get(consideringSlide);
                    int currIndex = trackChangedPositions.get(slide.index);
                    if (currIndex != min.index) {
                        int increment = calcDifference(currIndex, minIndex, changingSlides);
                        if (increment > maxIncrement) {
                            maxIncrement = increment;
                            maxIncrementIndex = currIndex;
                        }
                    }
                }
            }

            if (maxIncrementIndex != min.index) {

                Slide aMiddle = changingSlides.get(maxIncrementIndex);

                Slide bMiddle = changingSlides.get(minIndex);

                changingSlides.set(minIndex, aMiddle);
                changingSlides.set(maxIncrementIndex, bMiddle);
                trackChangedPositions.put(aMiddle.index, minIndex);
                trackChangedPositions.put(bMiddle.index, maxIncrementIndex);
            }

            if (numOfIterations / 10 != 0 && i % (numOfIterations / 10) == 0)
                System.out.println("Iteration number " + (i + 1) + " and score: " + calcScore(changingSlides));
        }

        return changingSlides;
    }

    private static void updateSlideTags(Slide slide) {
        for (String tag : slide.tags) {
            if (slideTags.containsKey(tag)) slideTags.get(tag).add(slide.index);
            else {
                slideTags.put(tag, new ArrayList<>());
                slideTags.get(tag).add(slide.index);
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

    private static class Photo implements Comparable<Photo> {
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

        @Override
        public int compareTo(Photo other) {
            return numOfTags - other.numOfTags;
        }
    }

    private static int calcScoreForAdjacentSlides(Slide slide1, Slide slide2) {
        Set<String> both = new HashSet<>(slide1.tags);
        both.retainAll(slide2.tags);
        int inBoth = both.size();

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

        void updateValue() {
            for (String tag : tags) value += slideTags.get(tag).size();
        }

        @Override
        public int compareTo(Slide other) {
            return value - other.value;
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
