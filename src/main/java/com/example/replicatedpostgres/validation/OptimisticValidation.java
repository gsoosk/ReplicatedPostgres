package com.example.replicatedpostgres.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OptimisticValidation {
    private AtomicLong timeStamp;
    private Map<Integer, TransactionRecord> validated;
    private Map<Integer, TransactionRecord> onGoing;

    public OptimisticValidation() {
        validated = new HashMap<>();
        onGoing = new HashMap<>();
        timeStamp = new AtomicLong(0);
    }

    public void AddTrasaction(int id) {
        TransactionRecord record = new TransactionRecord(id, timeStamp.getAndAdd(1));
        onGoing.put(id, record);
    }

    public synchronized boolean Validate(int id, List<String> readSet, List<String> writeSet) {
        if (! onGoing.containsKey(id)) {
            System.out.println("[Error]: In Validate, " + id + " is not in ongoing set");
            return false;
        }

        // Get current record object
        TransactionRecord current = onGoing.get(id);
        onGoing.remove(id);
        current.RecordSet(readSet, writeSet, timeStamp.getAndAdd(1));

        for (TransactionRecord record : validated.values()) {
            if (current.startTime > record.writeTime) {
                // This record is fully executed, no need to validate
                continue;
            } else if (current.validationTime > record.writeTime) {
                // This record finished write before validation
                // Only check read set
                if (hasConflict(current.readSet, record.writeSet)) {
                    return false;
                }
            } else {
                // Check both read and write set
                if (hasConflict(current.readSet, record.writeSet)) {
                    return false;
                }

                if (hasConflict(current.writeSet, record.writeSet)) {
                    return false;
                }
            }
        }

        validated.put(current.trxId, current);
        return true;
    }

    public void CompleteWrite(int id) {
        if (!validated.containsKey(id)) {
            System.out.println("[Error]: In CompleteWrite, " + id + " is not in validated set");
        }

        validated.get(id).CompleteWrite(timeStamp.getAndAdd(1));
    }

    private boolean hasConflict(List<String> list1, List<String> list2){
        for (String s : list1) {
            if (list2.contains(s)) {
                return true;
            }
        }
        return false;
    }
}
