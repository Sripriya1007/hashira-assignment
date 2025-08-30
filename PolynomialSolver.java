import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class PolynomialSolver {

    // Convert value from base → BigInteger
    private static BigInteger convertBase(String value, int base) {
        return new BigInteger(value, base);
    }

    // Solve linear equations using Gaussian Elimination (BigDecimal version)
    private static BigDecimal[] solveEquations(BigDecimal[][] A, BigDecimal[] b) {
        int n = b.length;
        MathContext mc = new MathContext(50); // high precision

        for (int i = 0; i < n; i++) {
            // Make diagonal element 1
            BigDecimal diag = A[i][i];
            for (int j = 0; j < n; j++) {
                A[i][j] = A[i][j].divide(diag, mc);
            }
            b[i] = b[i].divide(diag, mc);

            // Eliminate column below and above
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    BigDecimal factor = A[k][i];
                    for (int j = 0; j < n; j++) {
                        A[k][j] = A[k][j].subtract(factor.multiply(A[i][j], mc), mc);
                    }
                    b[k] = b[k].subtract(factor.multiply(b[i], mc), mc);
                }
            }
        }

        return b; // b now contains solution
    }

    public static void main(String[] args) throws Exception {
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
        List<Map.Entry<Integer, BigInteger>> points = new ArrayList<>();
        for (Object key : obj.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt((String) key);
            JSONObject val = (JSONObject) obj.get(key);
            int base = Integer.parseInt((String) val.get("base"));
            String value = (String) val.get("value");
            BigInteger y = convertBase(value, base);
            points.add(new AbstractMap.SimpleEntry<>(x, y));
        }

        // Sort by x
        points.sort(Comparator.comparingInt(Map.Entry::getKey));

        // Build system using first k points
        BigDecimal[][] A = new BigDecimal[k][k];
        BigDecimal[] b = new BigDecimal[k];

        for (int i = 0; i < k; i++) {
            int x = points.get(i).getKey();
            BigInteger y = points.get(i).getValue();

            b[i] = new BigDecimal(y);
            for (int j = 0; j < k; j++) {
                A[i][j] = new BigDecimal(BigInteger.valueOf(x).pow(j));
            }
        }

        // Solve for coefficients
        BigDecimal[] coeffs = solveEquations(A, b);

        // Print polynomial
        System.out.println("Polynomial coefficients:");
        for (int i = 0; i < coeffs.length; i++) {
            System.out.printf("a%d = %s%n", i, coeffs[i].toPlainString());
        }

        System.out.print("\nPolynomial: f(x) = ");
        for (int i = 0; i < coeffs.length; i++) {
            if (i > 0) System.out.print(" + ");
            System.out.printf("(%s)*x^%d", coeffs[i].toPlainString(), i);
        }
        System.out.println();
    }
}
