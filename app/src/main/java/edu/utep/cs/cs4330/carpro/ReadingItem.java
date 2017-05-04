package edu.utep.cs.cs4330.carpro;

/**
 * Created by marcolopez on 5/3/17.
 */

public class ReadingItem {
    String title;
    String description;
    String details;

    public ReadingItem(){
        this.title = "";
        this.description = "";
        this.details = "";
    }

    public ReadingItem(String newTitle, String newDescription, String newDetail){
        this.title = newTitle;
        this.description = newDescription;
        this.details = newDetail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
