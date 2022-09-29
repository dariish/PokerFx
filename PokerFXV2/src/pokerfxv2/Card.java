
package pokerfxv2;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Card {
    private int rank, suit;

    private static String[] suits = { "copas", "espadas", "ouros", "paus" };
    private static String[] ranks  = { "Às", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Valete", "Dama", "Rei" };

    /**
     *
     * @param suit numerador
     * @param rank naipe
     */
    public Card(int suit, int rank){
        this.rank=rank;
        this.suit=suit;
    }

    /**
     *
     * @param rankNum carta como string
     * @return retorna o numero dos numerados
     */
    public static String rankAsString( int rankNum ) {
        return ranks[rankNum];
    }
        
    public @Override String toString(){
          return ranks[rank] + " de " + suits[suit];
    }

    /**
     *
     * @return naipe da carta
     */
    public int getRank() {
         return rank;
    }

    /**
     *
     * @return numerador da carta
     */
    public int getSuit() {
        return suit;
    }
}
