import java.time.LocalDate;

class RentrenchCalc {
    public static void main(String args[]) {
        double retrenchmentpay;

        System.out.println("Calculating your retrenchment since 2013");

        retrenchmentpay = Calcs.calcRetrenchPay(LocalDate.of(1967, 2, 22), LocalDate.of(2009, 2, 19), LocalDate.now(),
                "yes", 10, 0, 220000);
        System.out.println(retrenchmentpay);
    }
}

class Calcs {
    public static double julianDate(LocalDate gregorianDate) {
        int MM = gregorianDate.getMonthValue();
        int DD = gregorianDate.getDayOfMonth();
        int YYYY = gregorianDate.getYear();
        int HR = 12;
        int MN = 0;

        int SC = 0;
        double JD, J1;
        int GGG, S, A;

        if (YYYY <= 1585) {
            GGG = 0;
        } else {
            GGG = 1;
        }

        HR = HR + (MN / 60) + (SC / 3600);

        JD = -1 * Math.floor(7 * (Math.floor((MM + 9) / 12) + YYYY) / 4);
        S = 1;
        if ((MM - 9) < 0) {
            S = -1;
        }

        A = Math.abs(MM - 9);
        J1 = Math.floor(YYYY + S * Math.floor(A / 7));
        J1 = -1 * Math.floor((Math.floor(J1 / 100) + 1) * 3 / 4);
        JD = JD + Math.floor(275 * MM / 9) + DD + (GGG * J1);
        JD = JD + 1721027 + 2 * GGG + 367 * YYYY - 0.5;
        JD = JD + (HR / 24);

        return JD;
    }

    public static double calcRetrenchPay(LocalDate dateofbirth, LocalDate startdate, LocalDate finishdate, String pilon,
            double al, double lslt, double bcr) {
        double yeardiff, lsl, weeklypay, alw, taxfree, grossretrenchpay, grossleavepay, netretrenchpay, netleavepay,
                netpay;

        yeardiff = (julianDate(finishdate) - julianDate(startdate)) / 365.25;

        weeklypay = bcr / 52;

        // pay for long service leave only available after 5 years
        // note lsl is caclulated here in weeks!
        if (yeardiff > 5) {
            lsl = 0.867 * yeardiff;
        } else {
            lsl = 0;
        }

        // garden leave or pay in lieu
        if (pilon.equals("yes")) {
            al = 20 + al;
            // over 45 you get an extra week! This is a quick but slightly dud way of
            // calcing age need to rewrite properly
            if ((julianDate(finishdate) - julianDate(dateofbirth)) > 16435) {
                al = al + 5;
            }
        }

        alw = al / 5;

        // redundancy pay tax free limit is $10155 + ($5078 * completed years of
        // service)
        taxfree = 10155 + (Math.floor(yeardiff) * 5078);

        // ETP Cap is currently $200,000 need to incorporate this somewhere, tax rate
        // below prservation age (usually 60) is 32%.
        // Whole of income Cap is $180,000 - whatever else you earned in the tax year of
        // retrenchment (including payments for accrued leave and long service)

        // Accrued leave and long service is non-ETP
        // Pay in lieu of notice is ETP
        // Unused sick leave is ETP

        grossretrenchpay = (4 * weeklypay) + (3 * (yeardiff - 1) * weeklypay);

        // accrued annual and long service leave are taxed at 32%
        // lslt is provided as days vs lsl in weeks hence the /5
        grossleavepay = ((lsl - (lslt / 5)) * weeklypay) + (alw * weeklypay);
        // is payroll tax payable for leave payment calculation?
        netleavepay = grossleavepay * 0.94 * 0.68;

        if (grossretrenchpay <= taxfree) {
            netretrenchpay = grossretrenchpay;
        } else {
            // if grossretrenchpay is greater than tax free limit, net pay is (taxfree
            // amount) + (everything above the taxfree amount taxed at ETP rates, then
            // marginal rates if it exceeds Caps)
            netretrenchpay = taxfree + (((grossretrenchpay - taxfree) * 0.94) * 0.68);
        }

        netpay = netretrenchpay + netleavepay;

        return Math.floor(netpay);

    }
}