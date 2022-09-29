package pokerfxv2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Client {
  
    /**
     *
     */
    public String username; 

    /**
     *
     */
    public String mensagemJogo;

    /**
     *
     */
    public boolean canStart = false;

    /**
     *
     */
    public boolean timeToBet = false;

    /**
     *
     */
    public boolean fimDeRonda = false;
    Player player;

    /**
     *
     */
    public DataInputStream dis; 

    /**
     *
     */
    public DataOutputStream dos; 

    /**
     *
     */
    public ArrayList<String> todasMensagens;    
    
    /**
     *
     * @param player Jogador que guarda todas as informações
     */
    public Client(Player player){
        this.player=player;
    }
    
    /**
     * Método para connectar ao server, cria o socket e as variáveis necessários para receber e enviar.
     * Tem os métodos recetivos que o server envia
     * @throws IOException erro de servidor
     */
    public void ConnectToServer() throws IOException {
                
        Socket s = new Socket("localhost", 6666);        
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        todasMensagens = new ArrayList<String>();
        
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                  
                //Manda o username para o ClientHandler  
                dos.writeUTF(username);
              /*  while(true) {
                    
                    //Input mensagem
                    
                    
                    String msg= sc.nextLine(); 

                   
                        //Output da mensagem
                        dos.writeUTF(msg);  
                }
             */
              } catch (IOException e){}
              
            }
            });
        
        
        
        Thread readMessage = new Thread(new Runnable(){
            @Override
            public void run() {
                while(true) {
                try {
                    String msg = dis.readUTF();
                    
                    if(msg.startsWith("#")){                        
                    
                        if(msg.endsWith("#startGame")){                           
                           player.oponente = dis.readUTF();
                           canStart = true;
                           round(dis, player);  
                        }
                        if(msg.endsWith("#timeForBet")){   
                            if(player.credits == 0 || (player.creditsOponente == 0 && player.playerBet == player.oponentBet)){ 
                                try {
                                    System.out.println("ENTROU AQUI---");
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                                bets(player, dos, 0); 
                                timeToBet = false;
                            }else{
                                timeToBet = true;    
                            }
                        }
                        if(msg.endsWith("#updateInformation")){
                            updateInformation(player, dis, dos);                              
                        }
                        if(msg.endsWith("#endRound")){
                            fimDeRonda = true; 
                            endRound(player);
                        }
                        if(msg.endsWith("#winner")){
                           int money = dis.readInt();
                           player.credits = player.credits + money;
                        }
                        
                        if(msg.endsWith("#card")){
                            receiveCard(player, dis);
                        }
                        if(msg.endsWith("#endGame")){
                            String finalMsg = dis.readUTF();
                            s.close();
                            
                            if(player.credits > 0){
                                try {
                                    File myFile = new File("history.txt");
                                    FileWriter myWriter = new FileWriter("history.txt", true);
                                    if(myFile.createNewFile()){
                                        System.out.println("File created" + myFile.getName());
                                        myWriter.append(finalMsg + " \n");                                    
                                        myWriter.close();
                                    } else {
                                        System.out.println("File already exists.");
                                        myWriter.append(finalMsg + " \n");
                                        myWriter.close();
                                    }
                                } catch (IOException ex) {
                                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                                    System.out.println("An error occured.");
                                }
                            }                            
                        }

                    } else {
                        System.out.println(msg);
                        todasMensagens.add(msg);                        
                    }
                    
                } catch (IOException e) {
                }
                }
            }
         });

         
        sendMessage.start();
        readMessage.start();
    
    }
    
    /**
     * Método que limpa/reinicia as varáveis no final de cada ronda
     * @param player Jogador para limpar todas as suas variáveis e assim começar uma nova.
     */
    public static void endRound(Player player){    
    player.higherBet = 0;
    player.playerBet = 0;
    player.oponentBet = 0;
    player.hand.clear();  
    player.tableHand.clear();
    player.oponentHand.clear();
} 
        
    /**
     * Método do inicio da ronda, decide quem é o big.
     * @param dis datainputStream, de forma a receber informação do server
     * @param player variável do jogador, guardar informaçoes
     * @throws IOException para os erros do servidor
     */
    public static void  round(DataInputStream dis, Player player) throws IOException{
    player.creditsBeginningOfRound = player.credits;
    
    //Decide o Big
    String big1 = dis.readUTF();
    
     if(big1.endsWith("isBig")){
         player.setBig(true);
     }else if(big1.endsWith("isNotBig")){
          player.setBig(false);
     }     
}

    /**
     * Método para receber cartas, desde as cartas da sua mão até às cartas da mesa
     * @param player alterar as informações do jogador  
     * @param dis datainputstream para receber informações do server
     * @throws IOException para os erros do servidor
     */
    public static void receiveCard(Player player, DataInputStream dis) throws IOException{
    
    int choice = dis.readInt();
    int rank,suit;

    switch(choice){
        case 1:
            //Recebe as suas cartas
            rank = dis.readInt();
            suit = dis.readInt();
            player.hand.add(new Card(suit, rank));

            rank = dis.readInt();
            suit = dis.readInt();
            player.hand.add(new Card(suit, rank));

            //recebe as cartas do oponente
            rank = dis.readInt();
            suit = dis.readInt();
            player.oponentHand.add(new Card(suit, rank));

            rank = dis.readInt();
            suit = dis.readInt();
            player.oponentHand.add(new Card(suit, rank));

        break;
        case 2:
            rank = dis.readInt();
            suit = dis.readInt();
            player.tableHand.add(new Card(suit, rank));

            rank = dis.readInt();
            suit = dis.readInt();
            player.tableHand.add(new Card(suit, rank));

            rank = dis.readInt();
            suit = dis.readInt();
            player.tableHand.add(new Card(suit, rank));

        break;
        default:
            rank = dis.readInt();
            suit = dis.readInt();
            player.tableHand.add(new Card(suit, rank));
            break;

        }
}

    /**
     * Método para atualizar toda a informação, de forma ao server e o client estarem sempre iguais.
     * @param player para guardar informações do jogador
     * @param dis datainpitstream para receber informações do servidor
     * @param dos dataoutputstream para enviar informaçãos ao servidor
     * @throws IOException para os erros do servidor
     */
    public static void updateInformation(Player player, DataInputStream dis, DataOutputStream dos) throws IOException{
    
    //Recebe a aposta mais alta!
    int higherBet = dis.readInt();

    player.higherBet = higherBet;


    //Envia a aposta e recebe a aposta do jogador oponente
    dos.writeInt(player.playerBet);
    player.oponentBet = dis.readInt();


    //Envia os créditos e recebe os créditos do jogador oponente
    dos.writeInt(player.credits);
    player.creditsOponente = dis.readInt();


    //Send player bet to update information on game.
    dos.writeInt(player.playerBet);
    
    //Recebe quantos créditos existem na mesa totais. 
    player.creditosTable = dis.readInt();    
}

    /**
     * Método das apostas, tem algumas proteções contra os erros, e envia então para o servidor a aposta escolhida pelo utilizador.
     * @param player para guardar informaçoes do jogador
     * @param dos dataoutputstream para enviar informações ao servidor
     * @param choice enviar a escolha nas apostas 
     * @throws IOException para os erros do servidor
     */
    public static void bets(Player player, DataOutputStream dos, int choice) throws IOException{

    // 0=call/check >1=raise -1=fold

    //Verifica se os créditos que tem apostados na mesa, são iguais á maior aposta na mesa.
    if(player.playerBet == player.higherBet){

       if(choice == 0){

           dos.writeInt(0);

       }else if(choice > 0){           
           player.setPlayerBet(choice);
           dos.writeInt(choice);

       }else if(choice == -1){

           dos.writeInt(-1);
       }
    }

    //Verifica se os créditos que tem apostados na mesa, são iguais á maior aposta na mesa.
      if(player.playerBet < player.higherBet){

        if(choice == 0){

            //Verifica se a call é possivel, 
            //se nao for possivel, simplesmente dá all-in
            if(player.higherBet <= player.creditsBeginningOfRound){
                       player.setPlayerBet(player.higherBet);
                       dos.writeInt(0); 
            }else{
                player.setPlayerBet(player.creditsBeginningOfRound);
                dos.writeInt(0); 
            }

            // Call, mete os mesmos créditos que a higher bet.


        }else if(choice > 0){

            player.setPlayerBet(choice);
            dos.writeInt(choice);

        }else if(choice == -1){
            dos.writeInt(-1);
        }
    }
      
}

}
