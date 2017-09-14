package net.aineuron.eagps.model.transfer;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 11.09.2017.
 */

public class Paging {
    private int mSkip = 0;
    private int mTake = 10;

    public Paging() {
    }

    public Paging(int skip, int take) {
        mSkip = skip;
        mTake = take;
    }

    public void nextPage() {
        mSkip += mTake;
    }

    public int getSkip() {
        return mSkip;
    }

    public void setSkip(int skip) {
        mSkip = skip;
    }

    public int getTake() {
        return mTake;
    }

    public void setTake(int take) {
        mTake = take;
    }
}
