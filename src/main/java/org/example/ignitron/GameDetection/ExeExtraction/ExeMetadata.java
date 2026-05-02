package org.example.ignitron.GameDetection.ExeExtraction;

public class ExeMetadata {
    private String productName;
    public String fileDescription;
    public String companyName;
    public String productVersion;
    public String fileVersion;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
