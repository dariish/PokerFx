/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerfxv2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javafx.scene.image.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import static jdk.nashorn.internal.objects.NativeMath.max;
import static jdk.nashorn.internal.objects.NativeMath.min;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class FXMLDocumentController implements Initializable {
    
    // variáveis
    @FXML
    private TextField userTxtField;
    @FXML
    private TextField txtFieldBet;    
    @FXML
    private Slider sliderBet;
    @FXML 
    private Pane paneWaiting, paneFading, panelFimRonda;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnRaise, btnCall, btnFold;
    @FXML
    private AnchorPane anchorMenu;
    @FXML
    private AnchorPane anchorGame;    
    @FXML
    private AnchorPane anchorFimJogo;
    @FXML
    private Label lblWarning, invalidLbl;
    @FXML
    private Text lblNotification, textFimRonda, txtEndGame;
    @FXML
    private Text labelP1Credits, labelP2Credits;
    @FXML
    private Text labelPlayer1, labelPlayer2;
    @FXML 
    private Text p1BetLbl, p2BetLbl;
    @FXML
    private Label tablePontsLbl;
    @FXML
    private ImageView imageTCard1, imageTCard2, imageTCard3, imageTCard4, imageTCard5;    
    @FXML
    private ImageView imageP1Card1, imageP1Card2, imageP2Card1, imageP2Card2; 
    @FXML
    private ProgressBar progressBarTime;
    @FXML
    private Image tempP1Img1, tempP1Img2, tempP2Img1, tempP2Img2, tempimageTCard1, tempimageTCard2, tempimageTCard3, tempimageTCard4, tempimageTCard5;
    @FXML
    private TextArea txtArea, txtAreaHistory;
            
    int timerFimDeRonda = 10;
    int lastSize = 0;
    int notificationTimer = 4;
    
    Player player;
    Client client;
    
    String user;
    
    boolean canShowHistory = true;
    
    double roundTime;
    
    // método chamado quando o botão de jogar é pressionado, e encerra o programa.
    // verifica se o text field do nome não está vazio e o tamanho não é superior a 8
    // chama o método de conectar ao servidor, presente no cliente
    @FXML
    private void handlePlayAction(ActionEvent event) {        
        // verificar se o nome não está vazio
        if(userTxtField.getText().isEmpty() || userTxtField.getText().length() > 8){
            (lblWarning).setVisible(true);
            (lblWarning).setManaged(true);
        } else {
            (lblWarning).setVisible(false);
            (lblWarning).setManaged(false);
            user = userTxtField.getText();        
        
            player = new Player(user);
            client = new Client(player); 

            client.username = user;
            System.out.println(client.username);  

            try {
                client.ConnectToServer();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            (paneFading).setVisible(true);
            (paneFading).setManaged(true);
            (paneWaiting).setVisible(true);
            (paneWaiting).setManaged(true);        

            RefreshGame(client);
            roundTime = 60;
        }
    }
    
    // método chamado quando o botão de sair é pressionado, e encerra o programa.
    @FXML
    private void handleExitAction(ActionEvent event){        
        System.exit(0);
    }    
    
    // método chamado quando o botão do call é pressionado, e chama o método do cliente que dá call.
    @FXML
    private void handleCallAction(ActionEvent event){
        try {
            client.bets(client.player, client.dos, 0);
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.timeToBet = false;
        roundTime = 60;
    } 
    
    // método chamado quando o botão do raise é pressionado, e chama o método do cliente que dá raise.
    // verifica o valor do textfield das apostas para verificar o valor a apostar.
    @FXML
    private void handleRaiseAction(ActionEvent event){
        String value = txtFieldBet.getText();
        int intValue=0;
        
        try{
            intValue = Integer.parseInt(value);
        } catch(Exception ex){
            System.out.println("Not an integer");
        }
        
        
        if(intValue > 0 && (intValue <= client.player.credits) && (intValue + client.player.higherBet <= client.player.creditsBeginningOfRound)){
          if( intValue + client.player.higherBet >= client.player.oponentBet + client.player.creditsOponente){
             intValue = client.player.creditsOponente;
           }
            try {
                client.bets(client.player, client.dos, intValue + client.player.higherBet);
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.timeToBet = false;
                roundTime = 60;
            }
    }
    
    // método chamado quando o botão do fold é pressionado, e chama o método do cliente que dá fold.
    @FXML
    private void handleFoldAction(ActionEvent event){
        try {
            client.bets(client.player, client.dos, -1);
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.timeToBet = false;
        roundTime = 60;
    }
    
    // método chamado quando se inicializa o jogo, esconde algumas labels e paineis e aplica estilos.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        (lblWarning).setVisible(false);
        (lblWarning).setManaged(false);
        (invalidLbl).setVisible(false);
        (invalidLbl).setManaged(false);
        (lblNotification).setVisible(false);
        (lblNotification).setVisible(false); 
        (panelFimRonda).setVisible(false);
        (panelFimRonda).setVisible(false);
        
        txtArea.setStyle("-fx-control-inner-background:#19282F; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
        txtAreaHistory.setStyle("-fx-control-inner-background:#19282F; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
    } 
   
    /**
     * método que atualiza a cada 1 segundo. É onde grande parte da lógica é verificada.
     * quando este método é chamado esconde o painel do menu e mostra o painel do jogo
     * mostra as cartas da mesa e do jogador
     * chama métodos de mostrar os botões, decrescer o tempo dos timers, e verificar se a ronda acabou.
     */
    private void RefreshGame(Client client) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        
                        if(client.canStart){
                            (anchorMenu).setVisible(false);
                            (anchorMenu).setManaged(false);
                            (anchorMenu).setDisable(true);
                            (anchorGame).setVisible(true);
                            (anchorGame).setManaged(true);
                            labelPlayer1.setText(user);
                            labelPlayer2.setText(client.player.oponente);
                        }
                        
                        if(client.player.hand.size() == 2){
                            //Cartas do jogador
                            if(!client.fimDeRonda){
                                tempCardImages(client);
                            }
                            imageP1Card1.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.hand.get(0).getRank() + "" + client.player.hand.get(0).getSuit() + ".png").toString()));
                            imageP1Card2.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.hand.get(1).getRank() + "" + client.player.hand.get(1).getSuit() + ".png").toString()));
                            //Cartas do Oponente
                            imageP2Card1.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                            imageP2Card2.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                            
                            labelP1Credits.setText(String.valueOf(client.player.credits)); 
                            labelP2Credits.setText(String.valueOf(client.player.creditsOponente)); 
                            
                            p1BetLbl.setText(String.valueOf(client.player.playerBet));
                            p2BetLbl.setText(String.valueOf(client.player.oponentBet));
                            tablePontsLbl.setText(String.valueOf(client.player.creditosTable)); 
                        }   
                        
                        if(!client.timeToBet)roundTime = 60;
                        
                         //mostrar botões de apostas
                        showBetButtons(client, client.timeToBet);
                        //mostrar cartas da mesa
                        showTableCards(client);
               
                        
                        if(client.todasMensagens.size() != lastSize){
                            lastSize = client.todasMensagens.size();
                            showNotification(client);                            
                        }
                        
                        decreaseNotificationTimer();
                        
                        isRoundOver(client);
                        
                        if((client.todasMensagens.size() - 1 > 0) && client.todasMensagens.get(client.todasMensagens.size() - 1).endsWith("venceu o jogo!")){
                            txtEndGame.setText(client.todasMensagens.get(client.todasMensagens.size() - 1));
                            (anchorGame).setVisible(false);
                            (anchorGame).setManaged(false);
                            (anchorGame).setDisable(true);
                            (anchorFimJogo).setVisible(true);
                            (anchorFimJogo).setManaged(true);
                            
                            if(canShowHistory){
                                
                                canShowHistory = false;
                                
                                BufferedReader reader;
                                try{
                                    reader = new BufferedReader(new FileReader("history.txt"));
                                    String line = reader.readLine();
                                    
                                    while(line != null){
                                        txtAreaHistory.appendText(line + "\n");
                                        line = reader.readLine();
                                    }
                                    reader.close();
                                } catch(IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 1000);
    }
    
    /**
     * método para retornar uma string (recebe um int)
     * @param i recebe um int
     * @return o valor em string
     */
    public static String toString(int i){
        String s = String.valueOf(i);
        return s;
    }  
    
    /**
     * método para mostrar as cartas da mesa na altura certa e quando for preciso volta a esconde-las.
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     */
    public void showTableCards(Client client){
        
        switch(client.player.tableHand.size()){
            case 3:
                imageTCard1.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(0).getRank() + "" + client.player.tableHand.get(0).getSuit() + ".png").toString()));
                imageTCard2.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(1).getRank() + "" + client.player.tableHand.get(1).getSuit() + ".png").toString()));
                imageTCard3.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(2).getRank() + "" + client.player.tableHand.get(2).getSuit() + ".png").toString()));
                break;
            case 4:
                imageTCard4.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(3).getRank() + "" + client.player.tableHand.get(3).getSuit() + ".png").toString()));
                break;
            case 5:
                imageTCard5.setImage(new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(4).getRank() + "" + client.player.tableHand.get(4).getSuit() + ".png").toString()));
                break;
            default:
                imageTCard1.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                imageTCard2.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                imageTCard3.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                imageTCard4.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                imageTCard5.setImage(new Image(getClass().getResource("images/cardImg/default.png").toString()));
                break;
        }
    }

    /**
     * método para guardas as cartas em variáveis temporárias que serão apresentadas no fim da ronda
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     */
    public void tempCardImages(Client client){
        tempP1Img1 = new Image(getClass().getResource("images/cardImg/" + client.player.hand.get(0).getRank() + "" + client.player.hand.get(0).getSuit() + ".png").toString());
        tempP1Img2 = new Image(getClass().getResource("images/cardImg/" + client.player.hand.get(1).getRank() + "" + client.player.hand.get(1).getSuit() + ".png").toString());
        tempP2Img1 = new Image(getClass().getResource("images/cardImg/" + client.player.oponentHand.get(0).getRank() + "" + client.player.oponentHand.get(0).getSuit() + ".png").toString());
        tempP2Img2 = new Image(getClass().getResource("images/cardImg/" + client.player.oponentHand.get(1).getRank() + "" + client.player.oponentHand.get(1).getSuit() + ".png").toString());
        
        switch(client.player.tableHand.size()){
            case 3:
                tempimageTCard1 = new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(0).getRank() + "" + client.player.tableHand.get(0).getSuit() + ".png").toString());
                tempimageTCard2 = new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(1).getRank() + "" + client.player.tableHand.get(1).getSuit() + ".png").toString());
                tempimageTCard3 = new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(2).getRank() + "" + client.player.tableHand.get(2).getSuit() + ".png").toString());
                break;
            case 4:
                tempimageTCard4 = new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(3).getRank() + "" + client.player.tableHand.get(3).getSuit() + ".png").toString());
                break;
            case 5:
                tempimageTCard5 = new Image(getClass().getResource("images/cardImg/" + client.player.tableHand.get(4).getRank() + "" + client.player.tableHand.get(4).getSuit() + ".png").toString());
                break;
            default :
         tempimageTCard1 = new Image(getClass().getResource("images/cardImg/default.png").toString());
         tempimageTCard2 = new Image(getClass().getResource("images/cardImg/default.png").toString());
         tempimageTCard3 = new Image(getClass().getResource("images/cardImg/default.png").toString());
        tempimageTCard4 = new Image(getClass().getResource("images/cardImg/default.png").toString());
         tempimageTCard5 = new Image(getClass().getResource("images/cardImg/default.png").toString());
         break;

                
        }
    }
    
    /**
     * método para mostrar e esconder os botões do jogo, caso seja a vez do utilizador jogar ou não.
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     * @param state precisa de saber qual o valor deste boolean, ou seja, se for true, aparece os botões..
     */
    public void showBetButtons(Client client, boolean state){
        
        if(client.player.playerBet == client.player.higherBet){
            btnCall.setText("Check");
        } else {
            btnCall.setText("Call " + (client.player.higherBet -  client.player.playerBet));
        }  
        
        String value = txtFieldBet.getText();
        int intValue;
        
        try{
            intValue = Integer.parseInt(value);
            btnRaise.setText("Raise: " + intValue);
            (invalidLbl).setVisible(false);
            (invalidLbl).setManaged(false);
        } catch(Exception ex){
            (invalidLbl).setVisible(true);
            (invalidLbl).setManaged(true);
        }   
        
        (btnRaise).setVisible(state);
        (btnRaise).setManaged(state);
        (btnCall).setVisible(state);
        (btnCall).setManaged(state);
        (btnFold).setVisible(state);
        (btnFold).setManaged(state);
        (txtFieldBet).setVisible(state);
        (txtFieldBet).setManaged(state);
        
        (progressBarTime).setVisible(state);
        (progressBarTime).setManaged(state); 
        
        roundTime(client);
    }

    /**
     * método que serve de contador para o temepo que o utilizador tem para jogar. Caso acabe o tempo dá "fold".
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     */
    public void roundTime(Client client){
        double a = 1.0 / 60 * roundTime;
        roundTime--;
        progressBarTime.setProgress(a);
        
        if(a <=  0){
            try {
            client.bets(client.player, client.dos, -1);
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            client.timeToBet = false;
            roundTime = 60;
        }
    }

    /**
     * método para mostrar as notificações da ronda
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     */
    public void showNotification(Client client){        
        lblNotification.setText(client.todasMensagens.get(client.todasMensagens.size() - 1));
        (lblNotification).setVisible(true);
        (lblNotification).setVisible(true); 
        notificationTimer = 4;
        txtArea.appendText(client.todasMensagens.get(client.todasMensagens.size() - 1) + "\n");
    }    

    /**
     *  método que serve de contador para esconder as notificações
     */
    public void decreaseNotificationTimer(){
        double a = 1.0 / 60 * notificationTimer;
        notificationTimer--;
        
        if(notificationTimer <= 0){
            (lblNotification).setVisible(false);
            (lblNotification).setVisible(false);
        }
    }

    /**
     * método para verificar se a ronda acabou. Mostra notificações finais e permite que nova ronda comece.
     * @param client trabalha em conjunto com o cliente, usado várias vezes
     */
    public void isRoundOver(Client client){
        if(client.fimDeRonda){
            timerFimDeRonda--;
            
            System.out.println("fim de ronda");
            (btnRaise).setVisible(false);
            (btnRaise).setManaged(false);
            (btnCall).setVisible(false);
            (btnCall).setManaged(false);
            (btnFold).setVisible(false);
            (btnFold).setManaged(false);
            (txtFieldBet).setVisible(false);
            (txtFieldBet).setManaged(false);
            (progressBarTime).setVisible(false);
            (progressBarTime).setManaged(false);
            
            imageP1Card1.setImage(tempP1Img1);
            imageP1Card2.setImage(tempP1Img2);
            imageP2Card1.setImage(tempP2Img1);
            imageP2Card2.setImage(tempP2Img2);
            imageTCard1.setImage(tempimageTCard1);
            imageTCard2.setImage(tempimageTCard2);
            imageTCard3.setImage(tempimageTCard3);
            imageTCard4.setImage(tempimageTCard4);
            imageTCard5.setImage(tempimageTCard5);
            
            textFimRonda.setText(client.todasMensagens.get(client.todasMensagens.size() - 1)+"\n" + timerFimDeRonda);
            
            (panelFimRonda).setVisible(true);
            (panelFimRonda).setVisible(true);
            
            if(timerFimDeRonda <= 0){
                client.fimDeRonda = false;
                timerFimDeRonda = 10;    
                System.out.println("ronda nova");
                (panelFimRonda).setVisible(false);
                (panelFimRonda).setVisible(false);
            }            
        } 
    }
}
