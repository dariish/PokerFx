package pokerfxv2;

import java.util.ArrayList;

/**
 *
 * @author Tiago Santos e Jorge Pereira
 */
public class Comparator {
    
     ArrayList<Card> cards; 
     int[] value = new int[8];
     
     
    int[] ranks = new int[14];
     
     
     
    //No caso de haver mais que 1 par são criadas duas variaveis
    int sameCards = 1, sameCards1 = 1;
    int largeGroupRank=0,smallGroupRank=0;
        
    boolean flush = false, straight = false;
         
    int topStraightValue=0;

    
    int[] orderedRanks = new int[7];
     
    /**
     *
     * @param player usa-se o jogador para receber as mãos de cada jogador
     */
    public Comparator(ArrayList<Card> player){
         this.cards = player;
     }
     
    /**
     * Método do fluxo do jogo, ou seja, todos os métodos abaixo , são chamados através deste.
     */
    public void fluxe(){
        rank();
        checkForPars();
        checkForFlush();
        checkForStraight();
        orderRanks();
        
        handEvaluation();
        
        display();
     }
     
    /**
     * Método cria um array de 13 valores, enche esse array de acordo com a mão do jogador, ou seja,
     * Se o jogador tiver um ÀS, o index 0 vai encher para 1, se tiver dois ÁS então o index 0 sobe para 2
     * o método é usado em quase todos os métodos abaixo.
     */
    public void rank(){       
            //Poe todos os valores do array ranks a 0
             for(int x=0; x<=13; x++){
                 ranks[x] = 0;
             }

             //Sempre que encontar uma carta do valor na mão, incrementa o rank ++;
             for (int x=0; x<=6; x++)
             {
               ranks[ cards.get(x).getRank() ]++; 
             }
     }

    /**
     * Método verifica se existem pares,triplos,quadras na mão, consegue também ver se existe mais que um par.
     */      
    public void checkForPars(){      

         //Corre os valores todos.
         for (int x=12; x>=0; x--){
             //Ve se existe mais de um par, e mete os pares o valor dos pares no smallGroup e largeGroup
            if (ranks[x] > sameCards) {
                    if (sameCards != 1){
                       sameCards1 = sameCards;
                       smallGroupRank = largeGroupRank;
                    } 
                sameCards = ranks[x];
                largeGroupRank = x;
         
            } else if (ranks[x] > sameCards1){
                sameCards1 = ranks[x];
                smallGroupRank = x;
            }
        }
         
     }
     
    /**
     *Método verifica por flush, ou seja, se a mão tem 5 cartas com o mesmo naipe.
     */
    public void checkForFlush(){
         
        int hearts=0, spades=0, diamonds=0, clubs = 0;
        
        //Enche os naipes para ver se existe + do que 5 do mesmo naipe
         for(int x=0; x<6; x++){
             
             //Alterar para um switch talvez.
             if(cards.get(x).getSuit() ==  0){
                 hearts++;
             }else if(cards.get(x).getSuit() ==  1){
                 spades++;
             }else if(cards.get(x).getSuit() ==  2){
                 diamonds++;
             }else if(cards.get(x).getSuit() ==  3){
                 clubs++;
             }
         }
         
         if(hearts>4 || spades>4 ||diamonds>4 || clubs>4){
             flush=true;
         }
         
     }
     
    /**
     * Método verifica por Straight, ou seja, sequencia, quando o numerador vai de por exemplo 2,3,4,5,6 .
     */
    public void checkForStraight(){


         int value =0;
         
          for(int x=0; x<=13; x++){
            if(ranks[x] >= 1 ){
                value++;
                if(value == 5){
                    straight = true;
                        //No caso milagroso de haver 6 cards straight
                        if(ranks[x+1] >= 1){
                            topStraightValue= x+1;
                            break;
                        }
                    topStraightValue= x;
                    break;
                }
            }else if(ranks[x] == 0){
                value=0;
            } 
         }
          
          //Verifica o 10,J,Q,K,ÁS (já que o ÁS esta no index [0]...)
         if(ranks[10]==1 && ranks[11]==1 && ranks[12]==1 && ranks[13]==1 && ranks[1]==1){
             straight = true;
             topStraightValue=14;//Talvez a ser alterado para 13.
         }
   
     }
     
     
     //Ordena os valores das cartas. 

    /**
     * Método para ordenar os numerador, com isto é possivel verificar as cartas mais altas, em caso de empates.
     */
     public void orderRanks(){
 
        int index=0;
        
         if(ranks[0] == 1){
             orderedRanks[index] = 14;
             index++;
         }
         
         //Percorre os valores das cartas, e preenche a lista orderedRanks.
         for(int x=13; x>=1; x--){
             if(ranks[x]==1){
                 orderedRanks[index]=x;
                 index++;
             }
         }
         
     }
     
    /**
     * Método dá um valor à mão de acordo com o recebido dos métodos anteriors. um array até ao valor 5 é criado.
     */
    public void handEvaluation(){
     //start hand evaluation
            if ( sameCards==1 ) {    //Se não tiver nenhum par
                value[0]=1;          //Valor mais baixo
                value[1]=orderedRanks[0];  //O primeiro determinante será a maior carta
                value[2]=orderedRanks[1];  //depois a proxima
                value[3]=orderedRanks[2];  //e por ai adiante.
                value[4]=orderedRanks[3];
                value[5]=orderedRanks[4];
            }

            if (sameCards==2 && sameCards1==1) //Se tiver 1 par
            {
                value[0]=2;                //Valor de 2 quando tem 1 par
                value[1]=largeGroupRank;   //O rank do par 
                value[2]=orderedRanks[0];  //A proxima carta mais alta
                value[3]=orderedRanks[1];
                value[4]=orderedRanks[2];
                value[5]=orderedRanks[3];

            }

            if (sameCards==2 && sameCards1==2) //Dois pares
            {
                value[0]=3; //Valor de 3 quando tem 2 pares
                value[1]= largeGroupRank>smallGroupRank ? largeGroupRank : smallGroupRank; //rank do par maior
                value[2]= largeGroupRank<smallGroupRank ? largeGroupRank : smallGroupRank; //rank do par menor              
                value[3]=orderedRanks[0];  //extra card
                value[4]=orderedRanks[1];  //extra card

            }

            if (sameCards==3 && sameCards1!=2) //Triplo sem Full house
            {
                value[0]=4; // Tem valor de 4 quando tem triplo
                value[1]= largeGroupRank; //rank do triplo
                value[2]=orderedRanks[0];
                value[3]=orderedRanks[1];
            }

            if (straight && !flush)
            {
                value[0]=5; //5 cartas com valor consecutivo
                value[1]= topStraightValue; // Verifica o maior straight, após verificar a maior carta nesse straight
            }

            if (flush && !straight)   
            {
                value[0]=6; //5 cartas com o mesmo naipe
                value[1]=orderedRanks[0]; //se houver um empate, desempata através das cartas com a mesma cor.
                value[2]=orderedRanks[1];
                value[3]=orderedRanks[2];
                value[4]=orderedRanks[3];
                value[5]=orderedRanks[4];
            }

            if (sameCards==3 && sameCards1==2)  //full house
            {
                value[0]=7; //7 com full house, triplo + par
                value[1]=largeGroupRank; // em caso de empate.. duvido
                value[2]=smallGroupRank;
                value[3]=orderedRanks[0];
            }

            if (sameCards==4)  //Quadra
            {
                value[0]=8; // 8 para a quadra
                value[1]=largeGroupRank; // em caso de empate.. duvido
                value[2]=orderedRanks[0];
            }

            if (straight && flush)  //straight flush
            {
                value[0]=9; //0 para o Straight flush
                value[1]= topStraightValue;
            }  
     }
     
    /**
     *
     * @return String da mão do jogador.
     */
    public String display(){
    String s;
    switch( value[0] )
    {
        case 1:
            s="carta alta";
            break;
        case 2:
            s="par de " + Card.rankAsString(value[1]) + "\'s";
            break;
        case 3:
            s="dois pares de " + Card.rankAsString(value[1]) + " " + 
              Card.rankAsString(value[2]);
            break;
        case 4:
            s="triplo de " + Card.rankAsString(value[1]) + "\'s";
            break;
        case 5:
            s=Card.rankAsString(value[1]) + " straight";
            break;
        case 6:
            s="flush";
            break;
        case 7:
            s="full house " + Card.rankAsString(value[1]) + 
              " over " + Card.rankAsString(value[2]);
            break;
        case 8:
            s="quadra de " + Card.rankAsString(value[1]);
            break;
        case 9:
            s="straight flush " + Card.rankAsString(value[1]) + " alto";
            break;
        default:
            s="error in Hand.display: value[0] contains invalid value";
    }
   return s;
    }
    
    
}
