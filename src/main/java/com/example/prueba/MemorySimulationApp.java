package com.example.prueba;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.example.prueba.CreateFile.writeInstructionsToFile;

public class MemorySimulationApp extends Application {

    private String rutaArchivo = null;
    private TextArea logArea;
    private MMU mmuOptimal = new MMU();
    private MMU mmuOther = new MMU();
    private MemoryPanel memoryPanelOpt = new MemoryPanel();
    private MemoryPanel memoryPanelAlg = new MemoryPanel();
    private StatisticsDetailsPanel statsPanelOpt = new StatisticsDetailsPanel();
    private StatisticsDetailsPanel statsPanelAlg = new StatisticsDetailsPanel();
    private TableView<PageDetails> memoryDetailsTableOpt = new TableView<>();
    private TableView<PageDetails> memoryDetailsTableAlg = new TableView<>();
    private static boolean isSimulationPaused = false;

    @Override
    public void start(Stage primaryStage) {
        // Layout principal
        BorderPane root = new BorderPane();

        // Configurar la parte superior con controles de simulación
        VBox topControls = setupTopControls(primaryStage);
        root.setTop(topControls);

        // Configurar la parte central con las visualizaciones de memoria
        VBox memoryVisualizations = setupMemoryVisualizations();
        root.setCenter(memoryVisualizations);

        ScrollPane scrollPane = new ScrollPane(root);

        // Configurar la escena y mostrar el stage
        Scene scene = new Scene(scrollPane, 1200, 800); // Ajusta el tamaño según sea necesario
        primaryStage.setTitle("Simulación de Gestión de Memoria");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox setupTopControls(Stage primaryStage) {
        VBox vbox = new VBox(10);
        Label lblAlgorithm = new Label("Seleccione el algoritmo:");
        ComboBox<String> comboAlgorithms = new ComboBox<>();
        comboAlgorithms.getItems().addAll("FIFO", "SC", "MRU", "RND");


        Label lblSeed = new Label("Semilla para random:");
        TextField seedInput = new TextField();

        Label lblProcesses = new Label("Número de procesos:");
        ComboBox<Integer> comboProcesses = new ComboBox<>();
        comboProcesses.getItems().addAll(10, 50, 100);

        Label lblOperations = new Label("Número de operaciones:");
        ComboBox<Integer> comboOperations = new ComboBox<>();
        comboOperations.getItems().addAll(500, 1000, 5000);

        Button btnStart = new Button("Iniciar Simulación");
        Button btnPause = new Button("Pausar Simulación");
        Button btnReset = new Button("Reiniciar Simulación");
        Button btnLoad = new Button("Cargar Archivo");

        // Handlers
        btnStart.setOnAction(e -> startSimulation(comboAlgorithms, seedInput, comboProcesses, comboOperations));
        btnPause.setOnAction(e -> pauseSimulation());
        btnReset.setOnAction(e -> resetSimulation());
        btnLoad.setOnAction(e -> loadOperationsFile(primaryStage));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100); // Ajustar altura según necesidades

        vbox.getChildren().addAll(lblAlgorithm, comboAlgorithms, lblSeed, seedInput, lblProcesses, comboProcesses, lblOperations, comboOperations, btnStart, btnPause, btnReset, btnLoad, logArea);
        return vbox;
    }

    private void updateMemoryVisualization(MMU mmu, MemoryPanel panel) {
        panel.clearMemoryBlocks();
        List<Page> realMemory = mmu.getRealMemory();
        for (int i = 0; i < realMemory.size(); i++) {
            panel.addMemoryBlock(i, realMemory.get(i));
        }
    }

    private void startSimulation(ComboBox<String> comboAlgorithms, TextField seedInput, ComboBox<Integer> comboProcesses, ComboBox<Integer> comboOperations) {
        try {
            String algorithm = Optional.ofNullable(comboAlgorithms.getValue()).orElse("FIFO");
            int seed = parseInput(seedInput.getText().isEmpty() ? "10" : seedInput.getText(), "Semilla para random");
            int numProcesses = Optional.ofNullable(comboProcesses.getValue()).orElse(0);
            int numOperations = Optional.ofNullable(comboOperations.getValue()).orElse(0);

            int valorAlgoritmo = 0;

            switch (algorithm) {
                case "FIFO":
                    valorAlgoritmo = 1;
                    break;
                case "SC":
                    valorAlgoritmo = 2;
                    break;
                case "MRU":
                    valorAlgoritmo = 3;
                    break;
                case "RND":
                    valorAlgoritmo = 4;
                    break;
            }

            if (rutaArchivo == null) {
                writeInstructionsToFile(numOperations, numProcesses, seed);
                rutaArchivo = "instructions.txt";
            }

            Path filePath = Paths.get(rutaArchivo);
            List<String> instructions = ReadFile.readLines(String.valueOf(filePath));

            // Set the instructions for the optimal algorithm
            mmuOptimal.setOptimalAlgorithm(instructions);

            mmuOther.setPaginationAlgorithm(valorAlgoritmo);

            // Execute simulation in a separate thread
            new Thread(() -> {
                for (String instruction : instructions) {

                    if (isSimulationPaused) {
                        // Espera hasta que la simulación se reanude
                        while (isSimulationPaused) {
                            try {
                                Thread.sleep(100); // Espera activa
                            } catch (InterruptedException e) {
                                System.err.println("An error occurred while sleeping: " + e.getMessage());
                            }
                        }
                    }
                    mmuOptimal.executeInstruction(instruction);
                    try {
                        Thread.sleep(5); // Pausa de milisegundos
                    } catch (InterruptedException e) {
                        System.err.println("An error occurred while sleeping: " + e.getMessage());
                    }
                    Platform.runLater(() -> {
                        updateMemoryVisualization(mmuOptimal, memoryPanelOpt);
                        updateMemoryDetailsTable(mmuOptimal.getPagesDetails(), memoryDetailsTableOpt);
                        updateStatisticsOptimal();

                    });

                    mmuOther.executeInstruction(instruction);
                    try {
                        Thread.sleep(5); // Pausa de milisegundos
                    } catch (InterruptedException e) {
                        System.err.println("An error occurred while sleeping: " + e.getMessage());
                    }
                    Platform.runLater(() -> {
                        updateMemoryVisualization(mmuOther, memoryPanelAlg);
                        updateMemoryDetailsTable(mmuOther.getPagesDetails(), memoryDetailsTableAlg);
                        updateStatisticsOther();
                    });


                }
            }).start();

        } catch (NumberFormatException e) {
            log("Error: Entrada numérica no válida. " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private int parseInput(String input, String fieldName) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + " está vacío.");
        }
        return Integer.parseInt(input.trim());
    }

    private void updateStatisticsOptimal() {
        // Actualizar el panel de estadísticas, suponiendo que StatisticsDetailsPanel tiene métodos para actualizar cada valor
        statsPanelOpt.setProcessesCount(mmuOptimal.getAmountOfProcesses());
        statsPanelOpt.setSimulationTime(mmuOptimal.getTotalTime() + "s");
        statsPanelOpt.setRAMUsage(mmuOptimal.getUsedRam() + " KB");
        statsPanelOpt.setVRAMUsage(mmuOptimal.getUsedVM() + " KB");
        statsPanelOpt.setFragmentation(mmuOptimal.getFragmentation() + " KB");
        statsPanelOpt.setPageFaults(mmuOptimal.getPageFaults());
        statsPanelOpt.setPageHits(mmuOptimal.getPageHits());
        statsPanelOpt.setRamPercentage(mmuOptimal.getUsedRamPercentage() + "%");
        statsPanelOpt.setVramPercentage(mmuOptimal.getUsedVMPercentage() + "%");
        statsPanelOpt.setTotalTime(mmuOptimal.getTotalTime() + "s");
        statsPanelOpt.setTrashingTime(mmuOptimal.getTrashingTime() + "s");
        statsPanelOpt.setTrashingPercentage(mmuOptimal.getPercentageOfTrashingTime() + "%");
    }

    private void resetStatisticsOptimal() {
        statsPanelOpt.setProcessesCount(0);
        statsPanelOpt.setSimulationTime("0s");
        statsPanelOpt.setRAMUsage("0 KB");
        statsPanelOpt.setVRAMUsage("0 KB");
        statsPanelOpt.setFragmentation("0 KB");
        statsPanelOpt.setPageFaults(0);
        statsPanelOpt.setPageHits(0);
        statsPanelOpt.setRamPercentage("0%");
        statsPanelOpt.setVramPercentage("0%");
        statsPanelOpt.setTotalTime("0s");
        statsPanelOpt.setTrashingPercentage("0%");
        statsPanelOpt.setTrashingTime("0s");
    }

    private void updateStatisticsOther() {
        // Repetir para mmuOther y statsPanelAlg
        statsPanelAlg.setProcessesCount(mmuOther.getAmountOfProcesses());
        statsPanelAlg.setSimulationTime(mmuOther.getTotalTime() + "s");
        statsPanelAlg.setRAMUsage(mmuOther.getUsedRam() + " KB");
        statsPanelAlg.setVRAMUsage(mmuOther.getUsedVM() + " KB");
        statsPanelAlg.setFragmentation(mmuOther.getFragmentation() + " KB");
        statsPanelAlg.setPageFaults(mmuOther.getPageFaults());
        statsPanelAlg.setPageHits(mmuOther.getPageHits());
        statsPanelAlg.setRamPercentage(mmuOther.getUsedRamPercentage() + "%");
        statsPanelAlg.setVramPercentage(mmuOther.getUsedVMPercentage() + "%");
        statsPanelAlg.setTotalTime(mmuOther.getTotalTime() + "s");
        statsPanelAlg.setTrashingTime(mmuOther.getTrashingTime() + "s");
        statsPanelAlg.setTrashingPercentage(mmuOther.getPercentageOfTrashingTime() + "%");
    }

    private void resetStatisticsOther() {
        statsPanelAlg.setProcessesCount(0);
        statsPanelAlg.setSimulationTime("0s");
        statsPanelAlg.setRAMUsage("0 KB");
        statsPanelAlg.setVRAMUsage("0 KB");
        statsPanelAlg.setFragmentation("0 KB");
        statsPanelAlg.setPageFaults(0);
        statsPanelAlg.setPageHits(0);
        statsPanelAlg.setRamPercentage("0%");
        statsPanelAlg.setVramPercentage("0%");
        statsPanelAlg.setTotalTime("0s");
        statsPanelAlg.setTrashingTime("0s");
        statsPanelAlg.setTrashingPercentage("0%");
    }

    private void pauseSimulation() {
        if (!isSimulationPaused) {
            log("Simulación pausada.");
            isSimulationPaused = true;
        } else {
            isSimulationPaused = false;
            log("Simulación reanudada.");
        }
    }
    private void resetSimulation() {
        mmuOptimal.reset();
        mmuOther.reset();
        memoryPanelOpt.clearMemoryBlocks();
        memoryPanelAlg.clearMemoryBlocks();
        updateMemoryDetailsTable(mmuOptimal.getPagesDetails(), memoryDetailsTableOpt);
        updateMemoryDetailsTable(mmuOther.getPagesDetails(), memoryDetailsTableAlg);
        resetStatisticsOptimal();
        resetStatisticsOther();

        log("Simulación reiniciada.");
    }

    private void loadOperationsFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            log("Archivo cargado: " + file.getPath());
            rutaArchivo = file.getPath();
        }
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    private VBox setupMemoryVisualizations() {
        VBox vbox = new VBox(10);

        // Tablas de detalles de memoria para cada algoritmo con ScrollPane
        setupMemoryDetailsTable(memoryDetailsTableOpt);
        ScrollPane scrollTableOpt = new ScrollPane(memoryDetailsTableOpt);
        scrollTableOpt.setPrefHeight(250); // Ajusta esta altura según necesidad
        scrollTableOpt.setFitToWidth(true);

        setupMemoryDetailsTable(memoryDetailsTableAlg);
        ScrollPane scrollTableAlg = new ScrollPane(memoryDetailsTableAlg);
        scrollTableAlg.setPrefHeight(250); // Ajusta esta altura según necesidad
        scrollTableAlg.setFitToWidth(true);

        // Tablas de detalles de estadisticas para cada algoritmo con ScrollPane
        ScrollPane scrollStatsOpt = new ScrollPane(statsPanelOpt);
        scrollStatsOpt.setPrefHeight(150); // Ajusta esta altura según necesidad
        scrollStatsOpt.setFitToWidth(true);

        ScrollPane scrollStatsAlg = new ScrollPane(statsPanelAlg);
        scrollStatsAlg.setPrefHeight(150); // Ajusta esta altura según necesidad
        scrollStatsAlg.setFitToWidth(true);

        VBox vboxOpt = new VBox(5, new Label("MMU - OPT"), scrollTableOpt, memoryPanelOpt, scrollStatsOpt);
        VBox vboxAlg = new VBox(5, new Label("MMU - ALG"), scrollTableAlg, memoryPanelAlg, scrollStatsAlg);
        HBox hbox = new HBox(10, vboxOpt, vboxAlg);  // Usar HBox para distribuir horizontalmente

        vbox.getChildren().add(hbox);
        return vbox;
    }

    private void setupMemoryDetailsTable(TableView<PageDetails> memoryDetailsTable) {
        TableColumn<PageDetails, String> pageIdColumn = new TableColumn<>("Page ID");
        pageIdColumn.setCellValueFactory(new PropertyValueFactory<>("pageId"));

        TableColumn<PageDetails, String> pidColumn = new TableColumn<>("PID");
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));

        TableColumn<PageDetails, String> loadedColumn = new TableColumn<>("Loaded");
        loadedColumn.setCellValueFactory(new PropertyValueFactory<>("loaded"));

        TableColumn<PageDetails, String> lAddrColumn = new TableColumn<>("L-ADDR");
        lAddrColumn.setCellValueFactory(new PropertyValueFactory<>("lAddr"));

        TableColumn<PageDetails, String> mAddrColumn = new TableColumn<>("M-ADDR");
        mAddrColumn.setCellValueFactory(new PropertyValueFactory<>("mAddr"));

        TableColumn<PageDetails, String> loadedTColumn = new TableColumn<>("Loaded-T");
        loadedTColumn.setCellValueFactory(new PropertyValueFactory<>("loadedT"));

        TableColumn<PageDetails, String> MARKColumn = new TableColumn<>("MARK");
        MARKColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));

        memoryDetailsTable.getColumns().addAll(pageIdColumn, pidColumn, loadedColumn, lAddrColumn, mAddrColumn, loadedTColumn, MARKColumn);
    }

    private void updateMemoryDetailsTable(List<PageDetails> pages, TableView<PageDetails> memoryDetailsTable) {
        ObservableList<PageDetails> data = FXCollections.observableArrayList(pages);
        memoryDetailsTable.setItems(data);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
