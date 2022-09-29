
package pokerfxv2;

import java.util.ArrayList;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Player {
    boolean big;
    String username, oponente;
    ArrayList<Card> hand = new ArrayList<Card>();
    int credits = 1000; 
    int creditsOponente = 1000; 
    int creditsBeginningOfRound;
    
    int higherBet = 4;    
    int playerBet = 0;
    int oponentBet = 0;
    int creditosTable = 0;
    
    ArrayList<Card> tableHand = new ArrayList<Card>();
    ArrayList<Card> oponentHand = new ArrayList<Card>();    

    /**
     *
     * @param username nome do jogador
     */
    public Player(String username)
    {
        this.username = username;
    }

    /**
     * Método receve o big, e altera os créditos de aposta com tal.
     * @param big decide se o jogador é o big blind
     */
    public void setBig(boolean big) 
    {        
        this.big = big;
        if(big)
        {
            playerBet = 4;
            oponentBet = 2;
        }
        else if(!big)
        {
            playerBet=2; 
            oponentBet = 4;
        }
        
        credits -= playerBet;
        creditsOponente -= oponentBet;
    }
    
    /**
     *
     * @param playerBet a aposta atual do jogador
     */
    public void setPlayerBet(int playerBet)
    {
        this.playerBet = playerBet;
        credits = creditsBeginningOfRound - playerBet;       
    }
    
    /**
     *
     * @param credits creditos totais do jogador
     */
    public void setCredits(int credits)
    {
        this.credits = credits;
    }
    
    /**
     *
     * @param creditsOponente creditos totais do oponente
     */
    public void setCreditsOponent(int creditsOponente)
    {
        this.creditsOponente = creditsOponente;
    }       
}
