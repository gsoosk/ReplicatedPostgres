package com.example.replicatedpostgres.validation;

import java.util.List;

public class TransactionRecord {
    public int trxId;
    public long startTime;
    public long validationTime;
    public long writeTime;
    public boolean isReadonly;

    public List<String> readSet;
    public List<String> writeSet;

    public TransactionRecord(int id, long time, boolean isReadonly) {
        this.trxId = id;
        startTime = time;
        writeTime = Long.MAX_VALUE;
        validationTime = Long.MAX_VALUE;
        this.isReadonly = isReadonly;
    }

    public void RecordSet(List<String> readSet, List<String> writeSet, long time) {
        this.readSet = readSet;
        this.writeSet = writeSet;
        validationTime = time;
    }

    public void CompleteWrite(long time) {
        this.writeTime = time;
    }

    @Override
    public String toString() {
        String x = "Trx id: " + this.trxId + ", start: " + startTime
        + ", validate: " + validationTime  + ", writeTime" + writeTime;

        return x;
    }
}
