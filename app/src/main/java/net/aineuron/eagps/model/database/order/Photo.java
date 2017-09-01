package net.aineuron.eagps.model.database.order;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 30.08.2017.
 */

public class Photo {
    private String fileBytes;
    private String extension;
    private String fileName;

    public String getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(String fileBytes) {
        this.fileBytes = fileBytes;
    }

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
}
