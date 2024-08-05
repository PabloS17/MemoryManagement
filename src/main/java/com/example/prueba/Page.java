package com.example.prueba;

public class Page {
    private final int id;
    private Integer physicalAddress;
    private Boolean inRealMemory = false;
    private final int pId;
    private boolean referenceBit = true; // SC
    private int loadedTime;
    private int indexOnMemory;

    public Page(Integer pId, Integer pageID) {
        this.id = pageID;
        this.pId = pId;
    }

    public int getId() {
        return id;
    }

    public Boolean getInRealMemory() {
        return inRealMemory;
    }

    public Integer getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(Integer physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public void setInRealMemory(Boolean inRealMemory) {
        this.inRealMemory = inRealMemory;
    }

    public int getPId() {
        return pId;
    }

    public boolean getReferenceBit() {
        return referenceBit;
    }

    public void setReferenceBit(boolean referenceBit) {
        this.referenceBit = referenceBit;
    }

    public int getLoadedTime() {
        return loadedTime;
    }

    public void setLoadedTime(int loadedTime) {
        this.loadedTime += loadedTime;
    }

    public int getIndexOnMemory() {
        return indexOnMemory;
    }

    public void setIndexOnMemory(int indexOnMemory) {
        this.indexOnMemory = indexOnMemory;
    }

    @Override
    public String toString() {
        return "com.example.prueba.Page{" +
                "id=" + id +
                ", physicalAddress=" + physicalAddress +
                ", inRealMemory=" + inRealMemory +
                ", pId=" + pId +
                '}';
    }
}
