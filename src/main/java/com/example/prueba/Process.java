package com.example.prueba;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private Integer pId;
    private static List<String> instructions;

    public Process(Integer pId) {
        this.pId = pId;
        instructions = new ArrayList<>();
    }

    public Integer getPId() {
        return pId;
    }

    public void setPId(Integer pId) {
        this.pId = pId;
    }

    public static List<String> getInstructions() {
        return instructions;
    }

    public static void setInstructions(List<String> instructions) {
        Process.instructions = instructions;
    }

    public void addInstruction(String instruction){
        instructions.add(instruction);
    }
}
