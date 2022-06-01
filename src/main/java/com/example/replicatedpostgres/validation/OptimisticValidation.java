package com.example.replicatedpostgres.validation;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
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
        TransactionRecord record = new TransactionRecord(id, timeStamp.getAndAdd(1), false);
        onGoing.put(id, record);
    }

    public void AddTrasactionReadOnly(int id) {
        TransactionRecord record = new TransactionRecord(id, timeStamp.getAndAdd(1), true);
        onGoing.put(id, record);
    }

    public synchronized boolean Validate(int id, List<String> readSet, List<String> writeSet) {
        if (! onGoing.containsKey(id)) {
            log.error("In Validate, " + id + " is not in ongoing set");
            return false;
        }

        // Get current record object
        TransactionRecord current = onGoing.get(id);
        onGoing.remove(id);

        if (current.isReadonly) {
            return true;
        }

        current.RecordSet(readSet, writeSet, timeStamp.getAndAdd(1));

        for (TransactionRecord record : validated.values()) {
            if (current.startTime > record.writeTime) {
                // This record is fully executed, no need to validate
                continue;
            } else if (current.validationTime > record.writeTime) {
                // This record finished write before validation
                // Only check read set
                if (hasConflict(current.readSet, record.writeSet)) {
                    logConflict(current, record);
                    return false;
                }
            } else {
                // Check both read and write set
                if (hasConflict(current.readSet, record.writeSet)) {
                    logConflict(current, record);
                    return false;
                }

                if (hasConflict(current.writeSet, record.writeSet)) {
                    logConflict(current, record);
                    return false;
                }
            }
        }

        validated.put(current.trxId, current);
        return true;
    }

    public void CompleteWrite(int id) {
        if (!validated.containsKey(id)) {
            return;
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

    private void logConflict(TransactionRecord a, TransactionRecord b){
        log.info("There is conflict between T" + a.trxId + " and T" + b.trxId);
        log.info(a.toString());
        log.info("ReadSet: " + a.readSet);
        log.info("WriteSet: " + a.writeSet);
        log.info(b.toString());
        log.info("ReadSet: " + b.readSet);
        log.info("WriteSet: " + b.writeSet);
    }
}
