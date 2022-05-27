package com.example.replicatedpostgres.validation;

import java.util.List;

public class TransactionRecord {
    public int trxId;
    public long startTime;
    public long validationTime;
    public long writeTime;

    public List<String> readSet;
    public List<String> writeSet;

    public TransactionRecord(int id, long time) {
        this.trxId = id;
        startTime = time;
        writeTime = -1;
    }

    public void RecordSet(List<String> readSet, List<String> writeSet, long time) {
        this.readSet = readSet;
        this.writeSet = writeSet;
        validationTime = time;
    }

    public void CompleteWrite(long time) {
        this.writeTime = time;
    }
}
