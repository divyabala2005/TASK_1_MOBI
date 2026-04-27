import java.util.Scanner;

public class RateEstimation{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter foreign currency amount: ");
        double amount = sc.nextDouble();

        System.out.print("Enter currency code: ");
        String code = sc.next();

        System.out.print("Enter currency exchange rate: ");
        double rate = sc.nextDouble();

        System.out.print("Enter annual appreciation: ");
        double appreciation = sc.nextDouble();

        System.out.print("Enter number of years: ");
        int year = sc.nextInt();

        if(amount <= 0 || rate <= 0 || year < 1 || year > 30){
            System.out.println("Invalid input value");
            return;
        }

        double initial = amount * rate;
        double previous = initial;
        double gainTot = 0;

        double maxGain = Double.MIN_VALUE;
        int bestYear = 0;

        System.out.println("\nYEAR\tRATE\t\tINR\t\tYoY Gain");

        for(int i = 1; i <= year; i++){
            double projectedRate = rate * Math.pow(1 + appreciation / 100, i);
            double INR = amount * projectedRate;
            double gain = INR - previous;

            if(gain > maxGain){
                maxGain = gain;
                bestYear = i;
            }

            gainTot += gain;

            System.out.print("\n" +i+ "\t|\t" + projectedRate + "\t|\t" + INR + "\t|\t" + gain);

            previous = INR;
        }

        double finValue = previous;
        double CAGR = Math.pow(finValue / initial, 1.0 / year) - 1;

        System.out.println("\nTotal Gain: Rs. " + gainTot);
        System.out.println("CAGR: " + CAGR*100 + "%");
        System.out.println("Best year: " + bestYear);

    }
}