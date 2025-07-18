package mediaDatabase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Author: Jacqueline Kurniawan
 */
public class Media {
    //private String id;
    private String title;
    private String creator;
    private int yearOfRelease;
    private byte[] picture;
    private String note;
    private Timestamp time;
    private ArrayList<String> genre;
    private ArrayList<String> theme;
    private ArrayList<String> subgenre;
    private ArrayList<String> literaryElements;

    /**
     * Default constructor — method used when creating a media object with no parameters
     * @throws IOException
     */
    public Media() throws IOException {
        // Read the image file as a byte array and assign it to the 'picture' attribute
        this.picture = Files.readAllBytes(Paths.get("src/main/resources/mediaDatabase/240_F_64672736_U5kpdGs9keUll8CRQ3p3YaEv2M6qkVY5.jpg"));
    }

    public Media(String title, String creator, int yearOfRelease) throws IOException {
        this.title = title;
        this.creator = creator;
        this.yearOfRelease = yearOfRelease;
        this.picture = Files.readAllBytes(Paths.get("src/main/resources/mediaDatabase/240_F_64672736_U5kpdGs9keUll8CRQ3p3YaEv2M6qkVY5.jpg"));

    }

    public Media(String title, String creator, int yearOfRelease, Timestamp time) throws IOException {
        this.title = title;
        this.creator = creator;
        this.yearOfRelease = yearOfRelease;
        this.time = time;

    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getYearOfRelease() {
        return yearOfRelease;
    }

    public void setYearOfRelease(int yearOfRelease) {
        this.yearOfRelease = yearOfRelease;
    }

    public ArrayList<String> getGenre() {
        return genre;
    }

    public void setGenre(ArrayList<String> genre) {
        this.genre = genre;
    }

    public ArrayList<String> getTheme() {
        return theme;
    }

    public void setTheme(ArrayList<String> theme) {
        this.theme = theme;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public ArrayList<String> getSubgenre() {
        return subgenre;
    }

    public void setSubgenre(ArrayList<String> subgenre) {
        this.subgenre = subgenre;
    }

    public ArrayList<String> getLiteraryElements() {
        return literaryElements;
    }

    public void setLiteraryElements(ArrayList<String> literaryElements) {
        this.literaryElements = literaryElements;
    }
}
