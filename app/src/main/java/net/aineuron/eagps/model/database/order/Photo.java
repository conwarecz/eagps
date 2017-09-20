package net.aineuron.eagps.model.database.order;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 30.08.2017.
 */

public class Photo {
    private String fileString;
    private String extension;
    private String fileName;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileString() {
        return fileString;
    }

    public void setFileString(String fileString) {
        this.fileString = fileString;
    }
}
