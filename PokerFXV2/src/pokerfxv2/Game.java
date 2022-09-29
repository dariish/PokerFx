
package pokerfxv2;

import java.util.*;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Game {
    int big , notBig;
    ArrayList<Card> deck = new ArrayList<Card>();
    
    ArrayList<Card> player1Hand =  new ArrayList<Card>();
    ArrayList<Card> player2Hand =  new ArrayList<Card>();
    
    Comparator comparar0 = new Comparator(player1Hand);
    Comparator comparar1 = new Comparator(player2Hand);
    
    int tableCredits = 4;
    int higherBet = 4;
    int player1Credits, player2Credits;
    
    ArrayList<Card> tableHand =  new ArrayList<Card>();
    
    /**
     * Método dá as cartas aos jogadores e mesa.
     * @param value com um switch dentro, a variável é usada para saber qual caso correr
     */
    public void drawCards(int value){
        switch(value){
            case 1:
                 player1Hand.add(deck.get(0)); player1Hand.add(deck.get(1));
                 player2Hand.add(deck.get(2)); player2Hand.add(deck.get(3));    
                break;
            case 2:
                 player1Hand.add(deck.get(4));player2Hand.add(deck.get(4));tableHand.add(deck.get(4));   
                 player1Hand.add(deck.get(5));player2Hand.add(deck.get(5));tableHand.add(deck.get(5));  
                 player1Hand.add(deck.get(6));player2Hand.add(deck.get(6));tableHand.add(deck.get(6));       
                break;
            case 3:
                 player1Hand.add(deck.get(7));player2Hand.add(deck.get(7));tableHand.add(deck.get(7));    
                break;
            case 4:
                player1Hand.add(deck.get(8));player2Hand.add(deck.get(8));tableHand.add(deck.get(8));     
                break;
            
        } 
    }
    
    /**
     * Método baralha as cartas-
     */
    public void shuffleDeck(){
            deck.clear();
        
          //Criar o deck
            for (int a=0; a<=3; a++){
                for (int b=0; b<=12; b++){
                    deck.add( new Card(a,b) );   
                }
            }  
          //baralhar o deck  
          Collections.shuffle(deck);
          
    }
    
    /**
     * Método trabalha com a class comparator para comparar duas mãos e decidir assim o vencedor da ronda
     * @return a mão vencedora
     */
    public int compareHands(){
            
        comparar0.fluxe();
        comparar1.fluxe();
        
        int[] value = comparar0.value;
        int[] value1 = comparar1.value;
        
        
         for (int x=0; x<6; x++){
            if (value[x]>value1[x]){
                return 0;
            }else if (value[x]<value1[x]){
               return 1;
            }
        }
         return -1;
    }
    
    
}
