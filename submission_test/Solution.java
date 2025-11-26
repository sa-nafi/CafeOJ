import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Solution for the A + B sum problem, handling large input integers
 * and a large number of test cases efficiently using BufferedReader and PrintWriter.
 */
public class Solution {

    public static void main(String[] args) throws IOException {
        // Use BufferedReader for fast input reading
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        // Use PrintWriter for fast output writing
        PrintWriter pw = new PrintWriter(System.out);

        try {
            // 1. Read the number of test cases T
            // T is small enough (<= 10^5) to fit in a standard int.
            String line = br.readLine();
            if (line == null) return;
            int T = Integer.parseInt(line.trim());

            // Process each test case
            for (int t = 0; t < T; t++) {
                // 2. Read the line containing A and B
                line = br.readLine();
                if (line == null) break;
                
                // Split the line into two parts based on the space delimiter
                String[] parts = line.split(" ");

                // 3. Parse A and B as long
                // A and B can be up to 10^18, which requires the long type.
                long A = Long.parseLong(parts[0]);
                long B = Long.parseLong(parts[1]);

                // 4. Calculate the sum A + B
                long sum = A + B;

                // 5. Output the result
                pw.println(sum);
            }
        } catch (NumberFormatException e) {
            // Handle potential errors in input parsing
        } finally {
            // Ensure all buffered output is written to the console
            pw.flush();
            pw.close();
        }
    }
}
