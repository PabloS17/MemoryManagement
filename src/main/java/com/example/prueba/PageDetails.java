package com.example.prueba;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PageDetails {
    private StringProperty pageId;
    private StringProperty pid;
    private StringProperty loaded;
    private StringProperty lAddr;
    private StringProperty mAddr;
    private StringProperty loadedT;
    private StringProperty mark;
    public PageDetails(String pageId, String pid, String loaded, String lAddr, String mAddr, String loadedT, String mark) {
        this.pageId = new SimpleStringProperty(pageId);
        this.pid = new SimpleStringProperty(pid);
        this.loaded = new SimpleStringProperty(loaded);
        this.lAddr = new SimpleStringProperty(lAddr);
        this.mAddr = new SimpleStringProperty(mAddr);
        this.loadedT = new SimpleStringProperty(loadedT);
        this.mark = new SimpleStringProperty(mark);
    }

    // Getters
    public String getPageId() { return pageId.get(); }
    public String getPid() { return pid.get(); }
    public String getLoaded() { return loaded.get(); }
    public String getLAddr() { return lAddr.get(); }
    public String getMAddr() { return mAddr.get(); }
    public String getLoadedT() { return loadedT.get(); }
    public String getMark() { return mark.get(); }

    // Setters
    public void setPageId(String value) { pageId.set(value); }
    public void setPid(String value) { pid.set(value); }
    public void setLoaded(String value) { loaded.set(value); }
    public void setLAddr(String value) { lAddr.set(value); }
    public void setMAddr(String value) { mAddr.set(value); }
    public void setLoadedT(String value) { loadedT.set(value); }
    public void setMark(String value) { mark.set(value); }

    // Property getters
    public StringProperty pageIdProperty() { return pageId; }
    public StringProperty pidProperty() { return pid; }
    public StringProperty loadedProperty() { return loaded; }
    public StringProperty lAddrProperty() { return lAddr; }
    public StringProperty mAddrProperty() { return mAddr; }
    public StringProperty loadedTProperty() { return loadedT; }
    public StringProperty markProperty() { return mark; }

    @Override
    public String toString() {
        return "PageDetails{" +
                "pageId=" + pageId.toString() +
                ", pid=" + pid.toString() +
                ", loaded=" + loaded.toString() +
                ", lAddr=" + lAddr.toString() +
                ", mAddr=" + mAddr.toString() +
                ", loadedT=" + loadedT.toString() +
                ", mark=" + mark.toString() +
                '}';
    }
}
