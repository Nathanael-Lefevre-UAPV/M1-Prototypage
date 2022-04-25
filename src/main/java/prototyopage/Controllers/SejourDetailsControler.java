package prototyopage.Controllers;

import DB.DemSejDB.DemSej;
import DB.DemSejDB.DemSejDAO;
import DB.SejourDB.Sejour;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import prototyopage.Context;
import prototyopage.MainApp;
import prototyopage.Ressources.SVGpaths;

import java.io.File;

public class SejourDetailsControler extends ControllerAbstract {
    private MainApp mainApp;

    @FXML private Label sejourName;
    @FXML private Label locationi;
    @FXML private Text description;
    @FXML private Text dateBegin;
    @FXML private Text dateEnd;
    @FXML private Text statusText;
    @FXML private SVGPath statusLogo;
    @FXML private ImageView img1;
    @FXML private ImageView img2;
    @FXML private Button reservationActionButton;
    @FXML private Button chatButton;

    public static final String VIEW_VOYAGER = "view_voyager";
    public static final String VIEW_HOST = "view_host";
    public static final String VIEW_DISCONNECTED = "view_disconnected";

    public void setMainApp(MainApp mainApp) { this.mainApp = mainApp; }

    @Override
    public void init() {
        Sejour sejour = Context.getSejour();

        sejourName.setText(sejour.getName());
        locationi.setText(sejour.getLocation());
        description.setText(sejour.getDescription());
        dateBegin.setText(sejour.getStrDateBegin());
        dateEnd.setText(sejour.getStrDateEnd());

        File file = new File("src/main/resources/Images/Sejours/" + sejour.getImageBundle() + "/img_1.jpg");
        Image image = new Image(file.toURI().toString());
        img1.setImage(image);

        file = new File("src/main/resources/Images/Sejours/" + sejour.getImageBundle() + "/img_2.jpg");
        image = new Image(file.toURI().toString());
        img2.setImage(image);

        adaptDisplayToContext();
    }

    public void adaptDisplayToContext(){
        // si connecté comme Voyageur ou si un hote visite le séjour d'un tiers
        String viewType;
        if (Context.getUser() != null) {
            if (!Context.getUser().isHost()) {
                viewType = VIEW_VOYAGER;
            } else {
                if (Context.getSejour().getIdHost() == Context.getUser().getUserId()) {
                    viewType = VIEW_HOST;
                } else {
                    viewType = VIEW_VOYAGER;
                }
            }
        } else {
            viewType = VIEW_DISCONNECTED;
        }

        switch (viewType) {
            case VIEW_VOYAGER -> {
                DemSejDAO demSejDAO = new DemSejDAO();
                DemSej demSej = demSejDAO.getDemSejByVoyagerAndSejour(Context.getUser().getUserId(), Context.getSejour().getSejourId());
                Context.setDemSej(demSej);
                chatButton.setVisible(true);
                if (demSej == null) {  // Si il n'existe pas de reservation
                    statusLogo.setVisible(false);
                    statusText.setVisible(false);
                } else {  // Si il existe une reservation
                    reservationActionButton.setText("Reservation");

                    statusText.setText(demSej.validationToString());
                    statusText.setVisible(true);

                    statusLogo.setContent(demSej.getSVG().get(SVGpaths.PATH));
                    statusLogo.setFill(Paint.valueOf(demSej.getSVG().get(SVGpaths.COLOR)));
                }
            }
            case VIEW_DISCONNECTED, VIEW_HOST -> {
                reservationActionButton.setVisible(false);
                chatButton.setVisible(false);
                statusLogo.setVisible(false);
                statusText.setVisible(false);
            }
        }
    }

    @FXML
    private void backToPredView(){
        mainApp.backView();
    }

    @FXML
    private void openReservationModal() {
        mainApp.showReservationModal();
    }

    @FXML
    protected void showChat() {
        if (Context.getUser() != null && Context.getSejour() != null) {
            mainApp.showChat(Context.getUser().getUserId(), Context.getSejour().getIdHost(), Context.getSejour().getSejourId());
        }
        else
        {
            System.out.println("Vous n'êtes pas connectés !");
        }
    }
}