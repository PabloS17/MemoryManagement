package com.example.prueba;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.IntStream;

public class CreateFile {

    private static class IdRecord {
        int id;
        boolean isDeleted;
        boolean isKilled;

        IdRecord(int id) {
            this.id = id;
            this.isDeleted = false;
            this.isKilled = false;
        }

        @Override
        public String toString() {
            return "[" + id + isDeleted + isKilled +
                    ']';
        }
    }

    private static int CANTIDAD_PROCESOS = 50; // Cantidad de procesos
    private static int CANTIDAD_OPERACIONES = 10; // Cantidad de instrucciones
    private static final double PORCENTAJE_KILL = 0.10; // Probabilidad de operación kill
    private static final double PORCENTAJE_DELETE = 0.15; // Probabilidad de operación delete
    private static final double PORCENTAJE_USE = 0.30; // Probabilidad de operación use
    private static final int MIN_SIZE = 500; // Tamaño mínimo de un proceso
    private static final int MAX_SIZE = 5000; // Tamaño máximo de un proceso
    private static Random random = new Random();
    private static final List<IdRecord> idList = new ArrayList<>();
    private static final String FILENAME = "instructions.txt";
    private static int idCounter = 1;

    public static void writeInstructionsToFile(int instrucciones, int procesos, int seed) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            int killCount = 0;
            int deleteCount = 0;
            int useCount = 0;

//            pedirDatos();
            CANTIDAD_OPERACIONES = instrucciones;
            CANTIDAD_PROCESOS = procesos;
            random = new Random(seed);

            int instructionCount = CANTIDAD_OPERACIONES;

            for (int i = 0; i < instructionCount; i++) {

                double chance = random.nextDouble();
                String instruction;

                if (i >= 5 ) { // Realizar operaciones kill y delete después de las primeras n iteraciones
                    if (chance < PORCENTAJE_KILL && killCount < CANTIDAD_PROCESOS) {
                        int idToKill = getRandomIdToKill();
                        if (idToKill != -1) {
                            instruction = "kill(" + idToKill + ")";
                            markAsKilled(idToKill); // Marca el ID seleccionado como eliminado
                            killCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else if (chance < PORCENTAJE_KILL + PORCENTAJE_DELETE && deleteCount < instructionCount * PORCENTAJE_DELETE) {
                        int indexToDelete = getRandomIndice();
                        int indexToPrint = indexToDelete + 1;
                        if (indexToDelete != -1) {
                            instruction = "delete(" + indexToPrint + ")";
                            idList.get(indexToDelete).isDeleted = true; // Marca el índice seleccionado como eliminado
                            deleteCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else if (chance < PORCENTAJE_KILL + PORCENTAJE_DELETE + PORCENTAJE_USE && useCount < instructionCount * PORCENTAJE_USE) {
                        int idUse = getRandomIndice();
                        int indexToPrint = idUse + 1;
                        if (idUse != -1) {
                            instruction = "use(" + indexToPrint + ")";
                            useCount++;
                        } else {
                            instruction = generateNewInstruction();
                        }
                    } else {
                        instruction = generateNewInstruction();
                    }
                } else {
                    instruction = generateNewInstruction();
                }

//                System.out.println("Instrucción generada: " + instruction);
//                System.out.println("Estado actual de idList: " + idList.toString());
//                System.out.println("\n\n");

                writer.write(instruction);
                writer.newLine();
            }
        }
    }

    private static String generateNewInstruction() {
        double chance = random.nextDouble();
        int id;
        if (chance < 0.7 && CANTIDAD_PROCESOS > 0) {
            id = idCounter++; // Incrementar el contador de IDs si con una proba de 0.7
            CANTIDAD_PROCESOS--;
        } else {
            id = Math.abs(getRandomReuseId()); // Reutilizar un ID existente si la probabilidad lo indica
        }
        int size = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
        idList.add(new IdRecord(id)); // Agregar nuevo IdRecord
        return "new(" + id + "," + size + ")";
    }

    private static int getRandomIdToKill() {
        List<Integer> validIds = idList.stream()
                .filter(idRecord -> !idRecord.isKilled)
                .map(idRecord -> idRecord.id)
                .toList();
        return validIds.isEmpty() ? -1 : validIds.get(random.nextInt(validIds.size()));
    }

    private static void markAsKilled(int id) {
        idList.stream()
                .filter(idRecord -> idRecord.id == id)
                .forEach(idRecord -> {
                    idRecord.isKilled = true;
                    idRecord.isDeleted = true;
                });
    }


    private static int getRandomIndice() {
        List<Integer> validIndices = IntStream.range(0, idList.size())
                .filter(i -> !idList.get(i).isDeleted &&!idList.get(i).isKilled)
                .boxed()
                .toList();
        return validIndices.isEmpty() ? -1 : validIndices.get(random.nextInt(validIndices.size()));
    }

    private static int getRandomReuseId() {
        List<Integer> validIds = idList.stream()
                .filter(idRecord -> !idRecord.isKilled)
                .map(idRecord -> idRecord.id)
                .toList();
        return validIds.isEmpty() ? -1 : validIds.get(random.nextInt(validIds.size()));
    }

    static void pedirDatos() {
        Scanner scanner = new Scanner(System.in);

        // Pedir el número de instrucciones
        System.out.println("Por favor, elige el número de instrucciones:");
        System.out.println("1. 500");
        System.out.println("2. 1000");
        System.out.println("3. 5000");
        System.out.println("4. Otro");
        int opcionInstrucciones = scanner.nextInt();

        switch (opcionInstrucciones) {
            case 1:
                CANTIDAD_OPERACIONES = 500;
                break;
            case 2:
                CANTIDAD_OPERACIONES = 1000;
                break;
            case 3:
                CANTIDAD_OPERACIONES = 5000;
                break;
            case 4:
                System.out.println("Introduce el número de instrucciones (mayor a 10):");
                int customInstrucciones = scanner.nextInt();
                while (customInstrucciones <= 10) {
                    System.out.println("El número de instrucciones debe ser mayor a 10. Inténtalo de nuevo:");
                    customInstrucciones = scanner.nextInt();
                }
                CANTIDAD_OPERACIONES = customInstrucciones;
                break;
            default:
                System.out.println("Opción no válida. Se seleccionará 500 como valor predeterminado.");
                CANTIDAD_OPERACIONES = 500;
        }

        // Pedir el número de procesos
        System.out.println("Ahora, elige el número de procesos:");
        System.out.println("1. 10");
        System.out.println("2. 50");
        System.out.println("3. 100");
        System.out.println("4. Otro");
        int opcionProcesos = scanner.nextInt();

        switch (opcionProcesos) {
            case 1:
                CANTIDAD_PROCESOS = 10;
                break;
            case 2:
                CANTIDAD_PROCESOS = 50;
                break;
            case 3:
                CANTIDAD_PROCESOS = 100;
                break;
            case 4:
                System.out.println("Introduce el número de procesos (mayor a 10):");
                int customProcesos = scanner.nextInt();
                while (customProcesos <= 10) {
                    System.out.println("El número de procesos debe ser mayor a 10. Inténtalo de nuevo:");
                    customProcesos = scanner.nextInt();
                }
                CANTIDAD_PROCESOS = customProcesos;
                break;
            default:
                System.out.println("Opción no válida. Se seleccionará 10 como valor predeterminado.");
                CANTIDAD_PROCESOS = 10;
        }

        scanner.close();
    }
}
