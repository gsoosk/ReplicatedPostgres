package com.example.replicatedpostgres.validation;

import java.util.Arrays;

public class ValidationUnitTest {
    public static OptimisticValidation validation;

    public static void main(String[] args) {
        test1();
        test2();
    }

    // Test write before start
    public static void test1(){
        validation = new OptimisticValidation();
        validation.AddTrasaction(1);
        String[] readset1 = new String[] {"x", "y"};
        String[] writeset1 = new String[] {"x"};
        boolean commit = validation.Validate(1, Arrays.asList(readset1), Arrays.asList(writeset1));
        validation.CompleteWrite(1);

        validation.AddTrasaction(2);
        String[] readset2 = new String[] {"x", "y"};
        String[] writeset2 = new String[] {"x"};
        commit = validation.Validate(2, Arrays.asList(readset2), Arrays.asList(writeset2));
        validation.CompleteWrite(2);
    }

    // Test write before validation
    public static void test2(){
        validation = new OptimisticValidation();

        validation.AddTrasaction(1);
        validation.AddTrasaction(2);
        validation.AddTrasaction(3);
        String[] readset1 = new String[] {"x", "y"};
        String[] writeset1 = new String[] {"x"};
        boolean commit = validation.Validate(1, Arrays.asList(readset1), Arrays.asList(writeset1));
        validation.CompleteWrite(1);

        // T2 can commit
        // no read write conflict
        // has write conflict but does not matter
        String[] readset2 = new String[] {"y"};
        String[] writeset2 = new String[] {"x"};
        commit = validation.Validate(2, Arrays.asList(readset2), Arrays.asList(writeset2));
        validation.CompleteWrite(2);

        // T3 cannot commit
        // reading x, which is written by T1
        String[] readset3 = new String[] {"x","y"};
        String[] writeset3 = new String[] {"y"};
        commit = validation.Validate(3, Arrays.asList(readset3), Arrays.asList(writeset3));
        if (commit != false) {
            System.out.println("Test2 T3 should fail");
        }
    }
}
