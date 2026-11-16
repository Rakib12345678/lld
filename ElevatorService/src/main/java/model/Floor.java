package model;

public class Floor {
    private int id;
    private boolean upwardReqest=false;
    private boolean downwardRequest=false;
    private Display display;

    public Floor(int id, boolean upwardReqest, boolean downwardRequest, Display display) {
        this.id = id;
        this.upwardReqest = upwardReqest;
        this.downwardRequest = downwardRequest;
        this.display = display;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isUpwardReqest() {
        return upwardReqest;
    }

    public void setUpwardReqest(boolean upwardReqest) {
        this.upwardReqest = upwardReqest;
    }

    public boolean isDownwardRequest() {
        return downwardRequest;
    }

    public void setDownwardRequest(boolean downwardRequest) {
        this.downwardRequest = downwardRequest;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }
}
