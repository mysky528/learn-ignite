package com.mycookcode.bigData.ignite.computegrid.montecarlo;

/**
 * 信用模型
 *
 * Created by zhaolu on 2018/2/11.
 */
public class Credit {

    /*剩余的授信金额*/
    private final double remAmnt;


    private final int remTerm;

    /*年度百分比例*/
    private final double apr;

    /*预期违约的年概率*/
    private final double edf;


    public Credit(double remAmnt, int remTerm, double apr, double edf) {
        this.remAmnt = remAmnt;
        this.remTerm = remTerm;
        this.apr = apr;
        this.edf = edf;
    }

    /**
     * Gets remained crediting amount.
     *
     * @return Remained amount of credit.
     */
    double getRemainingAmount() {
        return remAmnt;
    }

    /**
     * Gets remained crediting remTerm.
     *
     * @return Remained crediting remTerm in days.
     */
    int getRemainingTerm() {
        return remTerm;
    }

    /**
     * Gets annual percentage rate.
     *
     * @return Annual percentage rate in relative percents (percentage / 100).
     */
    double getAnnualRate() {
        return apr;
    }

    /**
     * Gets either credit probability of default for the given period of time
     * if remaining term is less than crediting time or probability of default
     * for whole remained crediting time.
     *
     * @param term Default term.
     * @return Credit probability of default in relative percents
     *     (percentage / 100).
     */
    double getDefaultProbability(int term) {
        return 1 - Math.exp(Math.log(1 - edf) * Math.min(remTerm, term) / 365.0);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClass().getName());
        buf.append(" [remAmnt=").append(remAmnt);
        buf.append(", remTerm=").append(remTerm);
        buf.append(", apr=").append(apr);
        buf.append(", edf=").append(edf);
        buf.append(']');

        return buf.toString();
    }


}
