class Borrower{
    private String name;
    private double monthlyIncome;
    private int employmentYear;
    private int baseScore;
    private int onTimePayments;
    private int defaults;

    public Borrower(String name, double monthlyIncome, int employmentYear, int baseScore, int onTimePayments, int defaults){
        this.name = name;
        this.monthlyIncome = monthlyIncome;
        this.employmentYear = employmentYear;
        this.baseScore = baseScore;
        this.onTimePayments = onTimePayments;
        this.defaults = defaults;
    }

    public String getName(){
        return name;
    }

    public double getMonthlyIncome(){
        return monthlyIncome;
    }

    public int getEmploymentYear(){
        return employmentYear;
    }

    public int getBaseScore(){
        return baseScore;
    }

    public int getOnTimePayments(){
        return onTimePayments;
    }

    public int getDefaults(){
        return defaults;
    }
}

interface CreditScoringModel{
    int calculateScore(Borrower b);
    String getRiskGrade(int score);
}

class IncomeBased implements CreditScoringModel{
    public int calculateScore(Borrower b){
        double score = (b.getMonthlyIncome() / 1000) * 10 + (b.getEmploymentYear() * 5);

        if (score > 900) {
            score = 900;
        }
        return (int) score;
    }

    public String getRiskGrade(int score) {
        if(score >= 750){
            return "A";
        }
        else if(score >= 650){
            return "B";
        }
        else if(score >= 550){
            return "C";
        }
        else{
            return "D";
        }
    }
}
class HistoryBased implements CreditScoringModel {
    public int calculateScore(Borrower b) {
        double score = b.getBaseScore() + (b.getOnTimePayments() * 2) - (b.getDefaults() * 50);

        if (score < 300) {
            score = 300;
        }

        return (int) score;
    }

    public String getRiskGrade(int score) {
        if (score >= 750) {
            return "A";
        }
        else if (score >= 650) {
            return "B";
        }
        else if (score >= 550) {
            return "C";
        }
        else {
            return "D";
        }
    }
}

class HybridScoring implements CreditScoringModel{
    private IncomeBased incomeModel = new IncomeBased();
    private HistoryBased historyModel = new HistoryBased();

    public int calculateScore(Borrower borrower){
        double incomeScore = incomeModel.calculateScore(borrower);
        double historyScore = historyModel.calculateScore(borrower);

        return (int)((0.6 * incomeScore) + (0.4 * historyScore));
    }

    public String getRiskGrade(int score) {
        if (score >= 750) {
            return "A";
        }
        else if (score >= 650) {
            return "B";
        }
        else if (score >= 550) {
            return "C";
        }
        else {
            return "D";
        }
    }
}

class LoanOffer{
    double approvedAmount;
    double interestRate;
    double emi;

    public LoanOffer(double approvedAmount, double interestRate, double emi){
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.emi = emi;
    }

    @Override
    public String toString(){
        return "Approved: Rs. " + approvedAmount + "\tRate: " + interestRate + "%\tEMI: " + emi;

    }
}

abstract class Loan {
    String loanId;
    Borrower borrower;
    double requestedAmount;
    int tenureMonths;

    public Loan(String loanId, Borrower b,
                double requestedAmount, int tenureMonths) {
        this.loanId = loanId;
        this.borrower = b;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
    }

    abstract double getInterestRate(String grade);
    abstract double getMaxAllowed();

    public boolean isEligible(CreditScoringModel model) {
        int score = model.calculateScore(borrower);
        return score >= 550;
    }

    public LoanOffer generateOffer(CreditScoringModel model) {
        int score = model.calculateScore(borrower);
        String grade = model.getRiskGrade(score);

        System.out.println("Borrower: " + borrower.getName() +
                "  Score: " + score + " (Grade " + grade + ")");

        if (!isEligible(model)) {
            System.out.println("  REJECTED: Score below minimum threshold of 550.\n");
            return null;
        }

        double approvedAmount = Math.min(requestedAmount, getMaxAllowed());
        double rate = getInterestRate(grade);
        double r = rate / (12 * 100);
        double emi = (approvedAmount * r * Math.pow(1 + r, tenureMonths)) /
                (Math.pow(1 + r, tenureMonths) - 1);

        LoanOffer offer = new LoanOffer(approvedAmount, rate, emi);

        System.out.println("  " + offer + "\n");
        return offer;
    }
}

class MicroLoan extends Loan {
    public MicroLoan(String id, Borrower b, double amt, int months) {
        super(id, b, amt, months);
    }

    double getMaxAllowed() {
        return 50000;
    }

    double getInterestRate(String grade) {
        switch (grade) {
            case "A": return 10;
            case "B": return 12;
            case "C": return 15;
            default: return 18;
        }
    }
}

class SalaryAdvance extends Loan {
    public SalaryAdvance(String id, Borrower b, double amt, int months) {

        super(id, b, amt, months);
    }

    double getMaxAllowed() {

        return borrower.getMonthlyIncome() * 2;
    }

    double getInterestRate(String grade) {
        switch (grade) {
            case "A": return 12;
            case "B": return 14;
            case "C": return 18;
            default: return 22;
        }
    }
}

public class Main {
    public static void main(String[] args) {

        Borrower b1 = new Borrower("Neha", 45000, 5, 650, 36, 0);
        Borrower b2 = new Borrower("Ravi", 18000, 1, 520, 10, 2);

        Loan l1 = new SalaryAdvance("SA01", b1, 80000, 12);
        Loan l2 = new MicroLoan("ML01", b2, 30000, 6);

        l1.generateOffer(new HybridScoring());
        l2.generateOffer(new HistoryBased());
    }
}

