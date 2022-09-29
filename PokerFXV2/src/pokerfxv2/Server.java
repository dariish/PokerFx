/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pokerfxv2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Server {
        
    //Lista dos clienteHandlers
    static Vector<ClientHandler> clientHandlers = new Vector<>();
    
    static boolean stop  =false;
  
    /**
     * Método estático main, quando corrido, corre um server e espera pelos dois clientHandlers encher. Quando isso acontece, começa um loop do jogo.
     * @param args static
     * @throws IOException para erros do servidor
     */
    public static void main(String[] args) throws IOException {
        
        // Criar o server
        System.out.println("[SERVER] Aceita conexões...");
        ServerSocket ss = new ServerSocket(6666);
        
        Socket s;
        for(int i=0; i<2; i++){
            //á espera do cliente aqui (pára aqui até encontrar)
            s = ss.accept();
            System.out.println("[SERVER] Novo cliente recebido: " + s);
            
            // Receber e enviar informação
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
 
            //  Cria o clientHandler, adiciona á lista, começa a uma thread apenas desse Clienthandler.
            ClientHandler client = new ClientHandler(s, dis, dos);
            Thread t = new Thread(client);
            clientHandlers.add(client);
            t.start(); 
        }   

        while(true){

                if(clientHandlers.get(1).username != null ){
                    gameLoop(ss);
                    break;
                }
                }
                           
    }
    
    /**
     * Método cria um loop de rondas até algum dos jogadores tiver 0 créditos.
     * @param ss serversocket. para enviar ao checkwinner() e assim fechar o server
     * @throws IOException para erros do servidor
     */
    public static void gameLoop(ServerSocket ss) throws IOException{
           int big = 1;
           while(true){
            Game game = new Game();
                        
            big = defineBigPlayer(game, big);
            
            for(ClientHandler mc: Server.clientHandlers){
                           mc.dos.writeUTF("#startGame");
            }
            
            System.out.println("Chegou aqui " + Server.clientHandlers.get(1).username + " e "+ Server.clientHandlers.get(0).username);
            
            Server.clientHandlers.get(0).dos.writeUTF(Server.clientHandlers.get(1).username);
            Server.clientHandlers.get(1).dos.writeUTF(Server.clientHandlers.get(0).username);
            
            round(game);
     
            if(checkWinner(game, ss)){  break; }
     
            }
           
    }
    
    /**
     * Método define o bigblind player sequencialmente. Alterando no inicio de cada ronda.
     * @param game para alterar as informações do jogo
     * @param big é alterado sequencialmente, decide o "big blind" player
     * @return big Player
     */
    public static int defineBigPlayer(Game game, int big){

      if(big == 1){
            game.notBig=1;
            game.big = 0;
            return 0;
        }else if(big ==0){
            game.notBig=0;
            game.big =1;
            return 1;
        }  
      
      return -1;
    }
            
    /**
     * Método da ronda, tem todo o fluxo normal, desde o enviar cartas, ao momento das apostas, no fim, e se chegar ao fim, então envia o vencedor
     * @param game para receber e alterar as variaveis do jogo
     * @return boolean, apenas para terminar o método caso este chegue ao fim mais cedo.
     * @throws IOException para erros do servidor
     */
    public static boolean round(Game game) throws IOException{
        stop = false;   
        
        Server.clientHandlers.get(game.big).dos.writeUTF("isBig");
        Server.clientHandlers.get(game.notBig).dos.writeUTF("isNotBig");
        
        game.shuffleDeck();
        game.drawCards(1);
        
        //Draw as primeiras 2 cartas       
        sendCard(game, 1);
        
        //Fazer apostas 
        updateInformation(game);
        normalBetFluxe(game);
        
        //pára a ronda caso alguem de fold
        if(stop){ return true; }
        
        //Mostrar as 3 cartas da mesa
        game.drawCards(2);
        sendCard(game, 2);  
        
         //Fazer apostas 
        updateInformation(game);
        normalBetFluxe(game);
           
        //pára a ronda caso alguem de fold
        if(stop){ return true; }
        
         //Mostrar 1 carta da mesa
         game.drawCards(3);
         sendCard(game, 3);            
        
         //Fazer apostas
        updateInformation(game);
        normalBetFluxe(game);
        //pára a ronda caso alguem de fold
        if(stop){ return true; }
        
        //Mostrar ultima carta da mesa
        game.drawCards(4);
        sendCard(game, 4);
          
        //Fazer apostas
        updateInformation(game);
        normalBetFluxe(game);
        //pára a ronda caso alguem de fold
        if(stop){ return true; }
        
          //Decide o vencedor da ronda, e printa
          int winner = game.compareHands();
            for(ClientHandler mc: Server.clientHandlers){                
                if(winner == 0){
                  mc.dos.writeUTF(Server.clientHandlers.get(0).username + ", é o vencedor com " + game.comparar0.display() + "!");      
                }else if(winner ==1){
                  mc.dos.writeUTF(Server.clientHandlers.get(1).username + ", é o vencedor com " + game.comparar1.display() + "!");    
                }
            } 
            
            endRound(game, winner);
            updateInformation(game);

            return true;
        
    }
    
    /**
     * Método enviar cartas ao cliente
     * @param game para receber as cartas do jogo
     * @param choice com um switch, precisa de saber qual dos casos vai correr
     * @throws IOException para erros do servidor
     */
    public static void sendCard(Game game, int choice) throws IOException{
        
        for(ClientHandler mc: Server.clientHandlers){
            mc.dos.writeUTF("#card");
            mc.dos.writeInt(choice);
        }

        switch(choice){
            case 1:

                    //Envia as duas carta de cada um primeiro
                    Server.clientHandlers.get(0).dos.writeInt(game.player1Hand.get(0).getRank());
                    Server.clientHandlers.get(0).dos.writeInt(game.player1Hand.get(0).getSuit());

                    Server.clientHandlers.get(1).dos.writeInt(game.player2Hand.get(0).getRank());
                    Server.clientHandlers.get(1).dos.writeInt(game.player2Hand.get(0).getSuit());

                    Server.clientHandlers.get(0).dos.writeInt(game.player1Hand.get(1).getRank());
                    Server.clientHandlers.get(0).dos.writeInt(game.player1Hand.get(1).getSuit());

                    Server.clientHandlers.get(1).dos.writeInt(game.player2Hand.get(1).getRank());
                    Server.clientHandlers.get(1).dos.writeInt(game.player2Hand.get(1).getSuit());
                    //Envia as duas cartas do oponente, para serem reveladas mais tarde.
                    Server.clientHandlers.get(0).dos.writeInt(game.player2Hand.get(0).getRank());
                    Server.clientHandlers.get(0).dos.writeInt(game.player2Hand.get(0).getSuit());

                    Server.clientHandlers.get(1).dos.writeInt(game.player1Hand.get(0).getRank());
                    Server.clientHandlers.get(1).dos.writeInt(game.player1Hand.get(0).getSuit());

                    Server.clientHandlers.get(0).dos.writeInt(game.player2Hand.get(1).getRank());
                    Server.clientHandlers.get(0).dos.writeInt(game.player2Hand.get(1).getSuit());

                    Server.clientHandlers.get(1).dos.writeInt(game.player1Hand.get(1).getRank());
                    Server.clientHandlers.get(1).dos.writeInt(game.player1Hand.get(1).getSuit());
            break;
            case 2:
                   for(ClientHandler mc: Server.clientHandlers){
                       mc.dos.writeInt(game.tableHand.get(0).getRank());
                       mc.dos.writeInt(game.tableHand.get(0).getSuit());

                       mc.dos.writeInt(game.tableHand.get(1).getRank());
                       mc.dos.writeInt(game.tableHand.get(1).getSuit());

                       mc.dos.writeInt(game.tableHand.get(2).getRank());
                       mc.dos.writeInt(game.tableHand.get(2).getSuit());
                   }
            break;

            case 3:
                 for(ClientHandler mc: Server.clientHandlers){
                    mc.dos.writeInt(game.tableHand.get(3).getRank());
                    mc.dos.writeInt(game.tableHand.get(3).getSuit());
                 }

            break;
            case 4:
                 for(ClientHandler mc: Server.clientHandlers){
                    mc.dos.writeInt(game.tableHand.get(4).getRank());
                    mc.dos.writeInt(game.tableHand.get(4).getSuit());
                 }

            break;
        }
    }
    
    /**
     * Método para finalizar a ronda
     * @param game para alterar informações do jogo
     * @param winner enviar os créditos so vencedor
     * @throws IOException para erros do servidor
     */
    public static void endRound(Game game, int winner) throws IOException{

        stop = true;

        //Envia os créditos da mesa ao vencedor da ronda.            
            Server.clientHandlers.get(winner).dos.writeUTF("#winner");
            Server.clientHandlers.get(winner).dos.writeInt(game.tableCredits);

        //reseta as variáveis para a proxima ronda.
            for(ClientHandler mc: Server.clientHandlers){                
                mc.dos.writeUTF("#endRound"); 
            }


    }

    /**
     * Método da ronda de apostas, isto caso nenhum deles faça raise ou fold
     * @param game para alterar as informações do jogo
     * @throws IOException para erros do servidor
     */
    public static void normalBetFluxe(Game game) throws IOException{
         
        Server.clientHandlers.get(game.notBig).dos.writeUTF("#timeForBet");
        int bet = Server.clientHandlers.get(game.notBig).dis.readInt();
        bets(game, bet, game.notBig);
        
        
        //Se a bet acima não for raise, então, entra num normal fluxe, onde ambos apostam.
        //Porque se for raise, então o método raise() é invocado, fazendo um loop, até um não der mais raises.
        if(bet == 0){
    
        Server.clientHandlers.get(game.big).dos.writeUTF("#timeForBet");
        bet  = Server.clientHandlers.get(game.big).dis.readInt();
        bets(game, bet, game.big);    
         
        }

          
    }
    
    /**
     * Método para as apostas, recebe as apostas do jogador e decide que fluxo o resto da ronda de apostas segue
     * @param game para alterar as informações do jogo.
     * @param bet para receber a aposta.
     * @param player para saber qual o player que vai apostar.
     * @throws IOException para erros do servidor
     */
    public static void bets(Game game, int bet, int player) throws IOException{

         if(bet == 0){
            //foi check 
            
            //Print a todos os jogadores, a escolha anterior
             for(ClientHandler mc: Server.clientHandlers){      
                mc.dos.writeUTF(Server.clientHandlers.get(player).username  + ", passou a vez! " ); 
             }
            
            //update as variaveis de jogos (total na mesa, player.credits, game.HigherBets, player.HigherBet)
            updateInformation(game);
            
        }else if(bet > 0 ){
             
            game.higherBet = bet;

            //foi raise
             updateInformation(game);

           //Print a todos os jogadores, a escolha anterior
           for(ClientHandler mc: Server.clientHandlers){      
                mc.dos.writeUTF(Server.clientHandlers.get(player).username  + ", aumentou a aposta para " + bet + "!"); 
           }
            raise(game, player);
            
           
            
        } else if(bet == -1){
           int player1 = player;
           
           if(player == 1){ player = 0; }else{ player = 1; }
           
           for(ClientHandler mc: Server.clientHandlers){      
                mc.dos.writeUTF(Server.clientHandlers.get(player1).username  + " desistiu! " +  Server.clientHandlers.get(player).username + " é o vencedor!"); 
           }
           
            //Tenho de fechar o jogo, e mandar os créditos ao outro jogador

             endRound(game, player);
            
        }
            
        }
    
    /**
     * Método de aumento de aposta
     * @param game para alterar as informações do jogo
     * @param player para saber qual player aumentou.
     * @throws IOException para erros do servidor
     */
    public static void raise(Game game, int player) throws IOException{
        if(player == 1){ player = 0; }else{ player = 1; }
        
        Server.clientHandlers.get(player).dos.writeUTF("#timeForBet");
        int bet  = Server.clientHandlers.get(player).dis.readInt();
        

         bets(game, bet, player);    
           
    }

    /**
     * Método para verificar o vencedor do jogo total, ou seja, verifica se algum jogador tem 0 créditos, e caso um tenho, faz o necessário para terminar o server, e 
     * encerrar tudo.
     * @param game para o método receber informações do jogo
     * @param ss serversocket, fechar o server quando o jogo acaba
     * @return boolean, se for true o jogo acaba, se for false o jogo continua.
     * @throws IOException para erros do servidor
     */
    public static boolean checkWinner(Game game, ServerSocket ss) throws IOException{
           if(game.player1Credits == 0){
                            for(ClientHandler mc: Server.clientHandlers){  
                                mc.dos.writeUTF(Server.clientHandlers.get(1).username + ", é o vencedor com " + game.comparar0.display() + "! \n "+Server.clientHandlers.get(1).username + " venceu o jogo!");
                                mc.dos.writeUTF("#endGame");  
                                mc.dos.writeUTF(Server.clientHandlers.get(1).username + " venceu o jogo contra " + Server.clientHandlers.get(0).username );
                           }
                            ss.close();
                            
             return true;               
           } else if(game.player2Credits == 0){
                           for(ClientHandler mc: Server.clientHandlers){  
                                mc.dos.writeUTF(Server.clientHandlers.get(0).username + ", é o vencedor com " + game.comparar0.display() + "! \n "+Server.clientHandlers.get(0).username + " venceu o jogo!");
                                mc.dos.writeUTF("#endGame");
                                mc.dos.writeUTF(Server.clientHandlers.get(0).username + " venceu o jogo contra " + Server.clientHandlers.get(1).username );
                           }
                           ss.close();

             return true;              
           }
           return false;           
            
        }

    /**
     * Método para atualização de informação, existe um método igual do lado do cliente, desta forma, ambos o server e os clientes Têm sempre a mesma informação 
     * @param game variável do jogo para receber informações e dar update
     * @throws IOException para erros do servidor
     */
    public static void updateInformation(Game game) throws IOException{
        
            for(ClientHandler mc: Server.clientHandlers){
             mc.dos.writeUTF("#updateInformation");

            //Aposta mais alta atual.
             mc.dos.writeInt(game.higherBet);
            } 

            //Recebe e manda as apostas de ambos os jogadores
            int playerBet1 = Server.clientHandlers.get(0).dis.readInt();
            int playerBet2 = Server.clientHandlers.get(1).dis.readInt();
            Server.clientHandlers.get(0).dos.writeInt(playerBet2);
            Server.clientHandlers.get(1).dos.writeInt(playerBet1);

            //Recebe e manda os credits de ambos os jogadores
            game.player1Credits = Server.clientHandlers.get(0).dis.readInt();
            game.player2Credits = Server.clientHandlers.get(1).dis.readInt();
            Server.clientHandlers.get(0).dos.writeInt(game.player2Credits);
            Server.clientHandlers.get(1).dos.writeInt(game.player1Credits);

            //Receive bet from both players
            int player1Bet = Server.clientHandlers.get(0).dis.readInt();
            int player2Bet = Server.clientHandlers.get(1).dis.readInt();

            game.tableCredits = player1Bet + player2Bet;

              for(ClientHandler mc: Server.clientHandlers){
                  mc.dos.writeInt(game.tableCredits);
              }
    }
    
    
    
    private static class ClientHandler implements Runnable {
        Socket s;
        final DataInputStream dis;
        final DataOutputStream dos;
        private String username;

        
        public ClientHandler(Socket socket, DataInputStream dis, DataOutputStream dos){
          this.s = socket;
          this.dis = dis;
          this.dos = dos;

        }
        
        
        //Mensagens recebidas a trabalhar numa thread diferente
        @Override
        public void run() {
               String mensagem;
               
                try{
                //Recebe o username
                username = dis.readUTF();
                
                }catch (IOException e) {   e.printStackTrace();} 

        }
    
    }
}
