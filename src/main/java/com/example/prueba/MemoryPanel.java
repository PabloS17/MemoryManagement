package com.example.prueba;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class MemoryPanel extends Pane {
    private static final int RECT_WIDTH = 15;  // Ancho del rectángulo
    private static final int RECT_HEIGHT = 25;  // Altura del rectángulo
    private static final int NUM_RECTS_PER_ROW = 50;  // Número de rectángulos por fila
    private Rectangle[] memoryBlocks;  // Array de rectángulos que representa las páginas de memoria
    private Map<Integer, Color> colorMap = new HashMap<>();
    private Color[] availableColors = {
            Color.RED, Color.BLUE, Color.GREEN, Color.CORAL, Color.ORANGE,
            Color.PINK, Color.CYAN, Color.MAGENTA, Color.BEIGE, Color.GRAY,
            Color.YELLOW, Color.BLACK, Color.WHITE, Color.DARKBLUE, Color.BEIGE,
            Color.DARKORCHID, Color.AQUAMARINE, Color.BROWN, Color.CRIMSON, Color.GOLD
    };
    private int nextColorIndex = 0;

    public MemoryPanel() {
        super();
        initializeMemoryBlocks();
    }

    private void initializeMemoryBlocks() {
        this.memoryBlocks = new Rectangle[100];  // Suponiendo que queremos mostrar 100 bloques
        for (int i = 0; i < memoryBlocks.length; i++) {
            Rectangle rect = new Rectangle(RECT_WIDTH, RECT_HEIGHT, Color.GRAY);  // Inicializar como vacío
            rect.setX((i % NUM_RECTS_PER_ROW) * (RECT_WIDTH + 1));  // Calcula la posición X
            rect.setY((i / NUM_RECTS_PER_ROW) * (RECT_HEIGHT + 1));  // Calcula la posición Y
            this.getChildren().add(rect);
            memoryBlocks[i] = rect;
        }
    }

    public void clearMemoryBlocks() {
        // Establecer todos los rectángulos a color gris, indicando que no están en uso
        for (Rectangle rect : memoryBlocks) {
            rect.setFill(Color.GRAY);
        }
    }

    public void addMemoryBlock(int index, Page memoryStatus) {
        if (memoryStatus != null) {
            Integer pId = memoryStatus.getPId();
            Color color = colorMap.get(pId);
            if (color == null) {
                color = getNextColor();
                colorMap.put(pId, color);
            }
            memoryBlocks[index].setFill(color);
        } else {
            // Manejar el caso en el que memoryStatus es null
            // Por ejemplo, podrías establecer el color del bloque como gris
            memoryBlocks[index].setFill(Color.GRAY);
        }
    }


    private Color getNextColor() {
        Color color = availableColors[nextColorIndex];
        nextColorIndex = (nextColorIndex + 1) % availableColors.length;
        return color;
    }
}
