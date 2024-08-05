package com.example.prueba;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        MMU mmuOptimal = new MMU();
        MMU mmuOther = new MMU();

//        try {
//            CreateFile.writeInstructionsToFile();
//        } catch (IOException e) {
//            System.err.println("An error occurred while writing to the file: " + e.getMessage());
//        }

        List<String> instructions = new ArrayList<>();
//        try {
//            instructions = ReadFile.readLines("instructions.txt");
//        } catch (IOException e) {
//            System.err.println("An error occurred while reading the file: " + e.getMessage());
//        }
        instructions.add("new(0,9300)");
        instructions.add("new(1,4500)");
        instructions.add("new(2,4500)");
        instructions.add("use(0)");
        instructions.add("use(0)");
        instructions.add("use(2)");
        instructions.add("use(0)");
        instructions.add("use(2)");
        instructions.add("delete(2)");
        instructions.add("new(1,2000)");
        instructions.add("new(2,3000)");

        // Set the instructions for the optimal algorithm
        mmuOptimal.setOptimalAlgorithm(instructions);

        // Ask the user for the algorithm to use
        //mmuOther.choosePaginationAlgorithm();

        mmuOther.setPaginationAlgorithm(3);

        mmuOptimal.executeAll(instructions);

        mmuOther.executeAll(instructions);
//
//        // Create a CyclicBarrier for 2 threads
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//        CyclicBarrier barrier = new CyclicBarrier(2); // Create a CyclicBarrier for 2 threads
//
//        // Runnable task for the optimal algorithm
//        Runnable optimalTask = () -> {
//            for (String instruction : instructions) {
//                System.out.println("Executing instruction: " + instruction + " with the optimal algorithm");
//                mmuOptimal.executeInstruction(instruction);
//                Page.setIdCounter(0);
//                try {
//                    barrier.await(); // Wait for the other thread
//                } catch (Exception e) {
//                    System.err.println("An error occurred while waiting for the other thread: " + e.getMessage());
//                }
//            }
//        };
//
//        // Runnable task for the user-chosen algorithm
//        Runnable userChosenTask = () -> {
//            for (String instruction : instructions) {
//                System.out.println("Executing instruction: " + instruction + " with the other algorithm");
//                mmuOther.executeInstruction(instruction);
//                Page.setIdCounter(0);
//                try {
//                    barrier.await(); // Wait for the other thread
//                } catch (Exception e) {
//                    System.err.println("An error occurred while waiting for the other thread: " + e.getMessage());
//                }
//            }
//        };
//
//        // Execute the tasks in parallel
//        executorService.submit(optimalTask);
//        executorService.submit(userChosenTask);
//
//        // Shutdown the executor service
//        executorService.shutdown();
//
//        try {
//            // Wait for both tasks to finish executing before printing the results
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            System.err.println("Execution was interrupted: " + e.getMessage());
//        }

        mmuOptimal.executeAll(instructions);
        System.out.println("----------------------------------");
        System.out.println("Optimal Algorithm");
        System.out.println("----------------------------------");
        mmuOptimal.printRealMemory();
        mmuOptimal.printVirtualMemory();
        mmuOptimal.printSymbolTable();
        System.out.println("Page faults: " + mmuOptimal.getPageFaults());
        System.out.println("Page hits: " + mmuOptimal.getPageHits());
        System.out.println("Trashing Time: " + mmuOptimal.getTrashingTime() + " seconds");
        System.out.println("Total Time: " + mmuOptimal.getTotalTime() + " seconds");
        System.out.println("----------------------------------");
        System.out.println("Other Algorithm");
        System.out.println("----------------------------------");
        mmuOther.executeAll(instructions);
        mmuOther.printRealMemory();
        mmuOther.printVirtualMemory();
        mmuOther.printSymbolTable();
        System.out.println("\nFragmentation: " + mmuOther.getFragmentation() + " KB");
        System.out.println("Page faults: " + mmuOther.getPageFaults());
        System.out.println("Page hits: " + mmuOther.getPageHits());
        System.out.println("Processes in execution: " + mmuOther.getAmountOfProcesses());
        System.out.println("Used RAM: " + mmuOther.getUsedRam() + " KB");
        System.out.println("Percentage of RAM used: " + mmuOther.getUsedRamPercentage() + "%");
        System.out.println("Used Virtual Memory: " + mmuOther.getUsedVM() + " KB");
        System.out.println("Percentage of Virtual Memory used: " + mmuOther.getUsedVMPercentage() + "%");
        System.out.println("Total Time: " + mmuOther.getTotalTime() + " seconds");
        System.out.println("Trashing Time: " + mmuOther.getTrashingTime() + " seconds");
        System.out.println("Percentage of time in trashing: " + mmuOther.getPercentageOfTrashingTime() + "%");
    }
}
