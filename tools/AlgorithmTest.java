import java.util.*;

/** Extra checks: javac -cp out -d out tools/AlgorithmTest.java && java -cp out AlgorithmTest */
public class AlgorithmTest {
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Random random = new Random(250245);
        for (int run = 0; run < 200; run++) {
            String a = dna(random, random.nextInt(7));
            String b = dna(random, random.nextInt(7));
            check(Strains.editDistance(a, b) == edit(a, b, a.length(), b.length()), "edit distance");
            int best = 0;
            for (int mask = 0; mask < (1 << a.length()); mask++) {
                StringBuilder sub = new StringBuilder();
                for (int i = 0; i < a.length(); i++) if ((mask & (1 << i)) != 0) sub.append(a.charAt(i));
                int matched = 0;
                for (int i = 0; i < b.length() && matched < sub.length(); i++)
                    if (b.charAt(i) == sub.charAt(matched)) matched++;
                if (matched == sub.length()) best = Math.max(best, matched);
            }
            check(Strains.lcsLength(a, b) == best, "LCS enumeration");
        }
        System.out.println("200 short strain pairs: edit distance and LCS passed independent checks.");

        for (int run = 0; run < 300; run++) {
            String text = dna(random, random.nextInt(30));
            String pattern = dna(random, 1 + random.nextInt(8));
            int[] pi = Marker.prefixFunction(pattern);
            for (int end = 0; end < pattern.length(); end++) {
                int border = 0;
                for (int length = 1; length <= end; length++)
                    if (same(pattern, 0, pattern, end - length + 1, length)) border = length;
                check(pi[end] == border, "prefix function");
            }
            List<Integer> hits = new ArrayList<>();
            for (int i = 0; i + pattern.length() <= text.length(); i++)
                if (same(text, i, pattern, 0, pattern.length())) hits.add(i);
            int[] found = Marker.findAll(text, pattern);
            check(found.length == hits.size(), "KMP count");
            for (int i = 0; i < found.length; i++) check(found[i] == hits.get(i), "KMP position");

            String b = dna(random, random.nextInt(30));
            int k = 1 + random.nextInt(8);
            int expected = 0;
            for (int i = 0; i + k <= text.length(); i++) {
                for (int j = 0; j + k <= b.length(); j++) {
                    if (same(text, i, b, j, k)) { expected++; break; }
                }
            }
            check(Marker.sharedWords(text, b, k) == expected, "rolling hash");
        }
        check(Arrays.equals(Marker.findAll("AAAAA", "AAA"), new int[]{0, 1, 2}), "overlapping hits");
        check(Marker.sharedWords("AAAAA", "AAAA", 2) == 4, "count positions, not words");
        // These different DNA words have the same hash (431928) with BASE=31 and MOD=1000000007.
        check(Marker.sharedWords("CAGACATAAAAA", "ATCAGACTCCGG", 12) == 0, "hash collision must not match");
        System.out.println("300 string cases, overlapping hits, repeated words and a real hash collision passed.");

        for (int run = 0; run < 150; run++) {
            int n = 1 + random.nextInt(10);
            int[] a = new int[n - 1], b = new int[n - 1], costs = new int[n];
            for (int i = 1; i < n; i++) { a[i - 1] = i; b[i - 1] = random.nextInt(i); }
            for (int i = 0; i < n; i++) costs[i] = 1 + random.nextInt(30);
            Input.Tree tree = new Input.Tree(n, a, b, costs);
            int[][] nb = tree.neighbours();
            testCover(tree, nb, costs);
            int[] ones = new int[n];
            Arrays.fill(ones, 1);
            testCover(tree, nb, ones);
            int diameter = 0;
            for (int start = 0; start < n; start++) {
                int[] distance = new int[n];
                Arrays.fill(distance, -1);
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                distance[start] = 0;
                queue.add(start);
                while (!queue.isEmpty()) {
                    int v = queue.remove();
                    for (int next : nb[v]) if (distance[next] == -1) {
                        distance[next] = distance[v] + 1;
                        diameter = Math.max(diameter, distance[next]);
                        queue.add(next);
                    }
                }
            }
            check(Outbreak.longestChain(n, nb) == diameter, "tree diameter");
        }
        System.out.println("150 small trees: both covers passed exhaustive subset checks; diameter passed BFS checks.");
    }

    static void testCover(Input.Tree tree, int[][] nb, int[] costs) {
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << tree.n()); mask++) {
            boolean valid = true;
            for (int i = 0; i < tree.links(); i++)
                if ((mask & (1 << tree.a()[i])) == 0 && (mask & (1 << tree.b()[i])) == 0) valid = false;
            if (!valid) continue;
            long total = 0;
            for (int v = 0; v < tree.n(); v++) if ((mask & (1 << v)) != 0) total += costs[v];
            best = Math.min(best, total);
        }
        boolean[] tested = Outbreak.cheapestTests(tree.n(), nb, costs);
        for (int i = 0; i < tree.links(); i++)
            check(tested[tree.a()[i]] || tested[tree.b()[i]], "every link covered");
        long total = 0;
        for (int v = 0; v < tree.n(); v++) if (tested[v]) total += costs[v];
        check(total == best, "minimum cover cost");
    }

    static boolean same(String a, int i, String b, int j, int length) {
        for (int offset = 0; offset < length; offset++)
            if (a.charAt(i + offset) != b.charAt(j + offset)) return false;
        return true;
    }

    static String dna(Random random, int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) s.append("ACGT".charAt(random.nextInt(4)));
        return s.toString();
    }

    static int edit(String a, String b, int i, int j) {
        if (i == 0) return j;
        if (j == 0) return i;
        int change = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        return Math.min(edit(a, b, i - 1, j - 1) + change,
                Math.min(edit(a, b, i - 1, j) + 1, edit(a, b, i, j - 1) + 1));
    }
}
