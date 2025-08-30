import java.io.FileReader;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class PolynomialSolver {

    // Convert value from base → decimal
    private static long convertBase(String value, int base) {
        return Long.parseLong(value, base);
    }

    // Solve linear equations using Gaussian Elimination
    private static double[] solveEquations(double[][] A, double[] b) {
        int n = b.length;

        for (int i = 0; i < n; i++) {
            // Make diagonal element 1
            double diag = A[i][i];
            for (int j = 0; j < n; j++) {
                A[i][j] /= diag;
            }
            b[i] /= diag;

            // Eliminate column below and above
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = A[k][i];
                    for (int j = 0; j < n; j++) {
                        A[k][j] -= factor * A[i][j];
                    }
                    b[k] -= factor * b[i];
                }
            }
        }

        return b; // b now contains solution
    }

    public static void main(String[] args) throws Exception {
        // Read JSON input file
        if (args.length == 0) {
            System.out.println("Usage: java -cp .;json-simple-1.1.1.jar PolynomialSolver input.json");
            return;
        }

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(new FileReader(args[0]));

        // Extract keys
        JSONObject keys = (JSONObject) obj.get("keys");
        int n = ((Long) keys.get("n")).intValue();
        int k = ((Long) keys.get("k")).intValue();

        // Extract (x, y) points
        List<int[]> points = new ArrayList<>();
        for (Object key : obj.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt((String) key);
            JSONObject val = (JSONObject) obj.get(key);
            int base = Integer.parseInt((String) val.get("base"));
            String value = (String) val.get("value");
            long y = convertBase(value, base);
            points.add(new int[]{x, (int) y});
        }

        // Sort by x for consistency
        points.sort(Comparator.comparingInt(a -> a[0]));

        // Use first k points
        double[][] A = new double[k][k];
        double[] b = new double[k];

        for (int i = 0; i < k; i++) {
            int x = points.get(i)[0];
            int y = points.get(i)[1];

            b[i] = y;
            for (int j = 0; j < k; j++) {
                A[i][j] = Math.pow(x, j);
            }
        }

        // Solve for coefficients
        double[] coeffs = solveEquations(A, b);

        // Print polynomial
        System.out.println("Polynomial coefficients:");
        for (int i = 0; i < coeffs.length; i++) {
            System.out.printf("a%d = %.2f\n", i, coeffs[i]);
        }

        System.out.print("\nPolynomial: f(x) = ");
        for (int i = 0; i < coeffs.length; i++) {
            if (i > 0) System.out.print(" + ");
            System.out.printf("%.2f*x^%d", coeffs[i], i);
        }
        System.out.println();
    }
}
