import java.util.Scanner;

public class WA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();
        for (int i = 1; i <= t; i++) {
            long a = sc.nextLong();
            long b = sc.nextLong();
            long ans = a - b;
            System.out.println(ans);
        }
        sc.close();
    }
}
