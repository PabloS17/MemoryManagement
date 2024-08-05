package com.example.prueba;

import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class StatisticsDetailsPanel extends GridPane {
    private Label processesCountLabel, simulationTimeLabel, ramUsageLabel, vramUsageLabel, fragmentationLabel,
            pageFaultsLabel, pageHitsLabel, ramPercentageLabel, vramPercentageLabel,totalTimeLabel,
            trashingTimeLabel, trashingPercentageLabel;

    public StatisticsDetailsPanel() {
        super();
        setHgap(5);
        setVgap(5);
        setupLabels();
    }

    private void setupLabels() {
        processesCountLabel = new Label();
        simulationTimeLabel = new Label();
        ramUsageLabel = new Label();
        vramUsageLabel = new Label();
        fragmentationLabel = new Label();
        pageFaultsLabel = new Label();
        pageHitsLabel = new Label();
        ramPercentageLabel = new Label();
        vramPercentageLabel = new Label();
        totalTimeLabel = new Label();
        trashingTimeLabel = new Label();
        trashingPercentageLabel = new Label();

        addRowWithLabel("Processes:", processesCountLabel);
        addRowWithLabel("Simulation Time:", simulationTimeLabel);
        addRowWithLabel("RAM Usage (KB):", ramUsageLabel);
        addRowWithLabel("VRAM Usage (KB):", vramUsageLabel);
        addRowWithLabel("Fragmentation (KB):", fragmentationLabel);
        addRowWithLabel("Page Faults:", pageFaultsLabel);
        addRowWithLabel("Page Hits:", pageHitsLabel);
        addRowWithLabel("RAM Usage (%):", ramPercentageLabel);
        addRowWithLabel("VRAM Usage (%):", vramPercentageLabel);
        addRowWithLabel("Total Time (s):", totalTimeLabel);
        addRowWithLabel("Trashing Time (s):", trashingTimeLabel);
        addRowWithLabel("Trashing Time (%):", trashingPercentageLabel);
    }

    private void addRowWithLabel(String labelText, Label valueLabel) {
        int rowCount = getRowCount();
        add(new Label(labelText), 0, rowCount);
        add(valueLabel, 1, rowCount);
    }

    public void setProcessesCount(int count) {
        processesCountLabel.setText(String.valueOf(count));
    }

    public void setSimulationTime(String time) {
        simulationTimeLabel.setText(time);
    }

    public void setRAMUsage(String usage) {
        ramUsageLabel.setText(usage);
    }

    public void setVRAMUsage(String usage) {
        vramUsageLabel.setText(usage);
    }

    public void setFragmentation(String fragmentation) {
        fragmentationLabel.setText(fragmentation);
    }

    public void setPageFaults(int faults) {
        pageFaultsLabel.setText(String.valueOf(faults));
    }

    public void setPageHits(int hits) {
        pageHitsLabel.setText(String.valueOf(hits));
    }

    public void setRamPercentage(String percentage) {
        ramPercentageLabel.setText(percentage);
    }

    public void setVramPercentage(String percentage) {
        vramPercentageLabel.setText(percentage);
    }

    public void setTotalTime(String time) {
        totalTimeLabel.setText(time);
    }

    public void setTrashingTime(String time) {
        trashingTimeLabel.setText(time);
    }

    public void setTrashingPercentage(String percentage) {
        trashingPercentageLabel.setText(percentage);

        // Obtener el valor numérico del porcentaje
        String percentageValueStr = percentage.replace("%", "");
        double percentageValue = Double.parseDouble(percentageValueStr);

        // Aplicar el estilo según el valor del porcentaje
        if (percentageValue > 50) {
            trashingPercentageLabel.setTextFill(Color.RED);
            trashingPercentageLabel.setFont(javafx.scene.text.Font.font("System", FontWeight.BOLD, 12));
        } else {
            trashingPercentageLabel.setTextFill(Color.BLACK);
            trashingPercentageLabel.setFont(javafx.scene.text.Font.font("System", FontWeight.NORMAL, 12));
        }
    }
}

