package mediaDatabase;

import java.io.IOException;

/**
 * Author: Jacqueline Kurniawan
 */
public class Film extends Media{
    //attributes that applies to a film but not a media in general
    private String editor;
    private String cinematographer;

    //constructor that takes in all variables of a film as parameters
    public Film(String title, String creator, int yearOfRelease, String editor, String cinematographer) throws IOException {
        super(title, creator, yearOfRelease);
        this.editor = editor;
        this.cinematographer = cinematographer;
    }

    //constructor taking in the required variables needed to create a media as parameters
    public Film(String title, String creator, int yearOfRelease) throws IOException {
        super(title, creator, yearOfRelease);
    }

    //accessors and mutators for the instance variables
    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getCinematographer() {
        return cinematographer;
    }

    public void setCinematographer(String cinematographer) {
        this.cinematographer = cinematographer;
    }
}
