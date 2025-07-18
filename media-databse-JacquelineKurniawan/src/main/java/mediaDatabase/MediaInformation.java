package mediaDatabase;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import javafx.scene.control.Label;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Author: Jacqueline Kurniawan
 */
public class MediaInformation implements Initializable {
    @FXML
    Label lblTitle, lblAuthor, lblYear;

    @FXML
    ImageView imageView;

    @FXML
    VBox vBox, vGenre, vSubGenre, vTheme, vElement, vNotes;

    DBConnection dbConn = new DBConnection();

    public void btnBackClick(ActionEvent actionEvent) throws IOException {
        Parent mediaDatabaseControl = FXMLLoader.load(getClass().getResource("mainPage-view.fxml"));
        Scene scene = new Scene(mediaDatabaseControl);
        Stage stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Main Page");
        scene.getRoot().setStyle("-fx-font-family: SansSerif");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        try {
            ResultSet rs = dbConn.getInfo();

            while(rs.next()){
                Blob blob = rs.getBlob("image");
                String title = rs.getString("title");
                String creator = rs.getString("creator");
                int year = rs.getInt("year");
                String editor = rs.getString("editor");
                String cinematographer = rs.getString("cinematographer");
                String genres = rs.getString("genres");
                String subGenres = rs.getString("subGenres");
                String themes = rs.getString("themes");
                String element = rs.getString("element");
                String notes = rs.getString("note");

                byte[] byteImage = blob.getBytes(1, (int)blob.length());
                ByteArrayInputStream bais = new ByteArrayInputStream(byteImage);
                BufferedImage bufferedImage = ImageIO.read(bais);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);

                imageView.setImage(image);

                lblTitle.setText("Title: " + title);
                lblAuthor.setText("Author: " + creator);
                lblYear.setText("Year of Release: " + year);

                if(editor != null){
                    Label lblCinematographer = new Label("Cinematogrpaher: " + cinematographer);
                    lblCinematographer.setFont(new Font("SansSerif Regular",16));
                    Label lblEditor = new Label("Editor: " + editor);
                    lblEditor.setFont(new Font("SansSerif Regular",16));
                    vBox.getChildren().add(lblCinematographer);
                    vBox.getChildren().add(lblEditor);
                }

                ArrayList<String> genre = new ArrayList<>();
                ArrayList<String> subgenre = new ArrayList<>();
                ArrayList<String> theme = new ArrayList<>();
                ArrayList<String> literaryElements = new ArrayList<>();

                if(genres != null){
                    String[] results = genres.split("[,]", 0);
                    Collections.addAll(genre, results);

                    for(int i = 0; i < genre.size(); i++){
                        Text txt = new Text("• " + genre.get(i));
                        txt.setFont(new Font("SansSerif Regular",14));
                        txt.setWrappingWidth(130);
                        vGenre.getChildren().add(txt);
                    }
                }
                if(subGenres != null){
                    String[] results = subGenres.split(",", 0);
                    Collections.addAll(subgenre, results);

                    for(int i = 0; i < subgenre.size(); i++){
                        Text txt = new Text("• " + subgenre.get(i));
                        txt.setFont(new Font("SansSerif Regular",14));
                        txt.setWrappingWidth(130);
                        vSubGenre.getChildren().add(txt);
                    }
                }
                if(themes != null){
                    String[] results = themes.split(",", 0);
                    Collections.addAll(theme, results);

                    for(int i = 0; i < theme.size(); i++){
                        Text txt = new Text("• " + theme.get(i));
                        txt.setFont(new Font("SansSerif Regular",14));
                        txt.setWrappingWidth(130);
                        vTheme.getChildren().add(txt);
                    }
                }
                if(element != null){
                    String[] results = element.split(",", 0);
                    Collections.addAll(literaryElements, results);

                    for(int i = 0; i < literaryElements.size(); i++){
                        Text txt = new Text("• " + literaryElements.get(i));
                        txt.setFont(new Font("SansSerif Regular",14));
                        txt.setWrappingWidth(130);
                        vElement.getChildren().add(txt);
                    }
                }

                Text txtNotes = new Text(notes);
                txtNotes.setFont(new Font("SansSerif Regular",14));
                txtNotes.setWrappingWidth(550);
                vNotes.getChildren().add(txtNotes);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}