package net.aineuron.eagps.model.database.order;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 30.08.2017.
 */

public class Photo {
    private byte[] fileBytes;
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

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }
}
