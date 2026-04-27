import java.util.ArrayList;
import java.util.List;

abstract class ReinsuranceTreaty{
    private String treatyId;
    private String cedingCompany;
    private String reinsurer;
    private String effectiveDate;
    protected double premiumPaid;

    public ReinsuranceTreaty(String treatyId, String cedingCompany, String reinsurer, String effectiveDate, double premiumPaid){
        this.treatyId = treatyId;
        this.cedingCompany = cedingCompany;
        this.reinsurer = reinsurer;
        this.effectiveDate = effectiveDate;
        this.premiumPaid = premiumPaid;
    }

    public String getTreatyId(){
        return treatyId;
    }

    public abstract double calculateReinsuredLoss(double originalLoss);
    public abstract double calculateRetainedLoss(double originalLoss);
    public abstract double calculateReinsurancePremium(double originalPremium);
    public abstract String getTreatyType();
}

class ProportionalTreaty extends ReinsuranceTreaty{
    private double cessionRate;

    public ProportionalTreaty(String id, String ced, String rein, String date, double premium, double rate){
        super(id, ced, rein, date, premium);
        this.cessionRate = rate;
    }

    @Override
    public double calculateReinsuredLoss(double loss){
        return loss * cessionRate;
    }
    public double calculateRetainedLoss(double loss){
        return loss *(1 - cessionRate);
    }
    public double calculateReinsurancePremium(double premium){
        return premium * cessionRate;
    }
    public String getTreatyType(){
        return "Proportional";
    }

}

class ExcessOfLossTreaty extends ReinsuranceTreaty{
    private double retention;
    private double maxCover;

    public ExcessOfLossTreaty(String id, String ced, String rein, String date, double premium, double retention, double maxCover){
        super(id, ced, rein, date, premium);
        this.retention = retention;
        this.maxCover = maxCover;
    }

    @Override
    public double calculateReinsuredLoss(double loss){

        return Math.min(Math.max(loss - retention, 0), maxCover);
    }
    public double calculateRetainedLoss(double loss){

        return loss - calculateReinsuredLoss(loss);
    }
    public double calculateReinsurancePremium(double premium){
        return premium;
    }
    public String getTreatyType(){
        return "ExcessOfLoss";
    }
}

class StopLossTreaty extends ReinsuranceTreaty{
    private double threshold;

    public StopLossTreaty(String id, String ced, String rein, String date, double premium, double threshold){
        super(id, ced, rein, date, premium);
        this.threshold = threshold;
    }

    @Override
    public double calculateReinsuredLoss(double loss){
        double ratio = loss / premiumPaid;
        if(ratio > threshold){
            return loss - (threshold * premiumPaid);
        }
        return 0;
    }

    public double calculateRetainedLoss(double loss){
        return loss - calculateReinsuredLoss(loss);
    }
    public double calculateReinsurancePremium(double premium){
        return premium;
    }
    public String getTreatyType(){
        return "StopLoss";
    }
}

class TreatyPortfolio{
    private List<ReinsuranceTreaty> treaties = new ArrayList<>();

    public void addTreaty(ReinsuranceTreaty t){
        treaties.add(t);
    }

    public void processClaim(String treatyId, double loss){
        for(ReinsuranceTreaty t : treaties){
            if(t.getTreatyId().equals(treatyId)){

                double reinsured = t.calculateReinsuredLoss(loss);
                double retained = t.calculateRetainedLoss(loss);

                System.out.println("\nClaim on " + treatyId + " (" + t.getTreatyType() + ")");
                System.out.println("Original Loss: " + loss);
                System.out.println("Retained: " + retained);
                System.out.println("Reinsured: " + reinsured);
            }
        }
    }

    public double getTotalReinsurancePremium(){
        double total = 0;
        for(ReinsuranceTreaty t : treaties){
            total += t.premiumPaid;
        }
        return total;
    }

    public void generateRecoveryReport(double[] losses){
        for(double loss : losses){
            System.out.println("\nLoss: " + loss);
            for(ReinsuranceTreaty t : treaties){
                System.out.println(t.getTreatyType() + " -> Reinsured: " + t.calculateReinsuredLoss(loss));
            }
        }
    }
}


public class Main{
    public static void main(String[] args){
        TreatyPortfolio portfolio = new TreatyPortfolio();

        ReinsuranceTreaty t1 = new ProportionalTreaty("PT01", "InsureCo", "ReinsureCo", "2024-01-01", 500000, 0.4);
        ReinsuranceTreaty t2 = new ExcessOfLossTreaty("EL01", "InsureCo", "ReinsureCo", "2024-01-01", 200000, 1000000, 5000000);

        portfolio.addTreaty(t1);
        portfolio.addTreaty(t2);

        portfolio.processClaim("PT01", 2500000);
        portfolio.processClaim("XL01", 3500000);

        System.out.println("\nTotal Premium: " + portfolio.getTotalReinsurancePremium());

        double[] losses = {1000000, 200000, 3000000};
        portfolio.generateRecoveryReport(losses);

    }
}