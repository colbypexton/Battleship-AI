
package battleship.players;

import battleship.Board;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

public class cpexton_BattleshipPlayer implements BattleshipPlayer {
    //track spaces fired at 
    private char[][] firedAt;
    private char turnRes;
    
    //first pass search
    private int[] fpRows;
    private int[] fpCols;
    
    //tracks when we are hunting a hit ship and 
    //activates the hunting function
    private boolean hunting;
    private char currentHunt;
    private char[] huntingQueue;
    
    //huntingCoordinates
    private int[] huntRows;
    private int[] huntCols;
    
    private HashMap originRow;
    private HashMap originCol;
    
    boolean isVert;
    boolean isHori;
    boolean foundOrient;

    
    //tracks boat hits
    
    private int B;
    private int A;
    private int S;
    private int D;
    private int P;
    
    
    //tracks start of game
    private boolean sog;
    
    //this be the queue for seen boats
    private ArrayList huntQueue;
    
    
    
    
    
    
    //include your instance variables here to maintain a record of your game state
    //remember which of your opponent's squares you've shot at
    //remember what was revealed at each square so you can strategize future moves


    /**
     * hideShips - This method is called once at the beginning of each game
     * when you need to hide your ships.
     * <p>
     * You must return a valid Board object. See that class for details.
     * Note carefully: under *no* circumstances should you return the same
     * board twice in a row; i.e., two successive calls to your hideShips()
     * method must always return *different* answers!
     */
    public Board hideShips() {

        //this code prevents cheaters - leave this here to prevent cheaters from looking at your board
        try {
            //SecurityManager antiCheater = new SecurityManager();
            //System.setSecurityManager(antiCheater);
        } catch (SecurityException e) {
            System.out.println("");
        }

        // INSERT YOUR AMAZING CODE HERE
        Random rand = new Random();
        char[][] myBoard = 
           {{' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        };
        
        //Sets each boat randomly
        myBoard = setA(myBoard);
        myBoard = setB(myBoard);
        myBoard = setS(myBoard);
        myBoard = setD(myBoard);
        myBoard = setP(myBoard);
        
        
        
        //int boardNum = rand.nextInt(3);
        int boardNum = 0;
        Board colbyBoard = null;
        
        try{
            colbyBoard = new Board(myBoard);
        } 
        
        catch (Exception e){
            e.printStackTrace();
        }
        
        
        return colbyBoard;
    }

    /**
     * go - This method is called repeatedly throughout the game, every
     * time it's your turn.
     * <p>
     * When it's your turn, and go() is called, you must call fireAt() on
     * the Board object which is passed as a parameter. You must do this
     * exactly *once*: trying to fire more than once during your turn will
     * be detected as cheating.
     */
    public void go(Board opponentsBoard) {
        // Starting game state
        if (sog){
            start();
            sog = false;
        }
        //regular search
        if(!hunting){
            
            firstPassSearch(opponentsBoard);
            //hunt search
        }else if(hunting){
            
            huntingBoat(opponentsBoard);
        }
        
        
    }

    /**
     * reset - This method is called when a game has ended and a new game
     * is beginning. It gives you a chance to reset any instance variables
     * you may have created, so that your BattleshipPlayer starts fresh.
     */
    public void reset() {
        // just reset the member data
        //DO THIS LAST
        sog = true;
        B=4;
        A=5;
        S=3;
        D=3;
        P=2;
        
    }
    
    //essentially my constructor 
    public void start(){
        B=4;
        A=5;
        S=3;
        D=3;
        P=2;
        
        firedAt = new char[10][10];
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                firedAt[i][j] = ' ';
            }
        }
        
        hunting = false;
        
        int[] tempRowArray = {0,1,0,1,2,3,4,0,1,2,3,4,5,6,7,1,2,3,4,5,6,7,8,9,
            4,5,6,7,8,9,7,8,9
        };
        
        int[] tempColArray = {8,9,5,6,7,8,9,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,
            0,1,2,3,4,5,0,1,2
        };
        
        fpRows = tempRowArray;
        fpCols = tempColArray;
        
        huntingQueue = new char[5];
        
        
        int[] tempHuntRow = {-1,0,1,0,-2,0,2,0,-3,0,3,0,-4,0,4,0
        };
        
        int[] tempHuntCol = {0,1,0,-1,0,2,0,-2,0,3,0,-3,0,4,0,-4
        };
        
        huntRows = tempHuntRow;
        huntCols = tempHuntCol;
        
        HashMap<Character, Integer> tempRowOrigin = new HashMap<Character, Integer>();
        HashMap<Character, Integer> tempColOrigin = new HashMap<Character, Integer>();
        originRow = tempRowOrigin;
        originCol = tempColOrigin;
        
        isVert = false;
        isHori = false;
        foundOrient = false;
        
        huntQueue = new ArrayList();
        
        
    }
    
    // catch edge cases and have them return false
    // and checks if we have already shot at a space
    private boolean validShot(int row, int col) {
        if(row>= 10 || row<0){
            return false;
        }
        if(col>=10 || row<0){
            return false;
        }
        
        if(firedAt[row][col] == ' '){
            return true;
        }
        
        
        return false;
    }
    
    //These are the non-hunting search methods that will call 
    //each other as more shots are fired -- at worst they will hit all 100 spaces
    private void firstPassSearch(Board opponenetsBoard) {
        boolean hasShot = false;
        for(int i = 0; i < fpRows.length; i++){
            if(validShot(fpRows[i], fpCols[i])){
                
                turnRes = opponenetsBoard.fireAt(fpRows[i], fpCols[i]);
                firedAt[fpRows[i]][fpCols[i]] = 'X';
                
                if(turnRes != ' '){
                    
                    hunting = true;
                    currentHunt = turnRes;
                    adjustBoats(turnRes);
                    //addQueue(turnRes);
                    firedAt[fpRows[i]][fpCols[i]] = turnRes;
                    publishOrigin(turnRes, fpRows[i], fpCols[i]);
                }
                
                hasShot = true;
                break;
            }
        }
        if(!hasShot){
            secondPassSearch(opponenetsBoard);
        }
    }
    private void secondPassSearch(Board opponenetsBoard){
        boolean hasShot = false;
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(j != 10 && !hasShot && j+1 != 10){ 
                    if(validShot(i, j) && validShot(i, j+1) && !hasShot){
                        turnRes = opponenetsBoard.fireAt(i, j);
                        firedAt[i][j] = 'X';
                        if(turnRes != ' '){
                            hunting = true;
                            currentHunt = turnRes;
                            adjustBoats(turnRes);
                            //addQueue(turnRes);
                            firedAt[i][j] = turnRes;
                            publishOrigin(turnRes, i, j);
                        }
                        
                        hasShot = true;
                        break;
                    }    
                }
        }
    }
    if(!hasShot){
            finalPassSearch(opponenetsBoard);
        }
}
    private void finalPassSearch(Board opponenetsBoard){
    boolean hasShot = false;
    
    for(int i = 0; i < 10; i++){
        if(!hasShot){
        for(int j = 0; j < 10; j++){
            if(firedAt[i][j] == ' '){
                turnRes = opponenetsBoard.fireAt(i, j);
                firedAt[i][j] = 'X';
                if(turnRes != ' '){
                    hunting = true;
                    currentHunt = turnRes;
                    adjustBoats(turnRes);
                    //addQueue(turnRes);
                    firedAt[i][j] = turnRes;
                    publishOrigin(turnRes, i, j);
                }
                
                hasShot = true;
                break;
            }
        }
    }
    }    
}
    
    //This is the hunting search method
    private void huntingBoat(Board opponenetsBoard){
        checkQueue(currentHunt, opponenetsBoard); 
        int livesCheck = getLives(currentHunt);
        if(livesCheck > 0)
        checkOrient(currentHunt);
        
        
}
    
    //essentially just adds the ship to a "seen" ArrayList 
    private void addQueue(char ship){
        huntQueue.add(ship);
}
    
    //Adjusts number of hits each boat has left
    private void adjustBoats(char ship){
    if(ship == 'B'){
        B--;
    }else if(ship == 'A'){
        A--;
    }else if(ship == 'S'){
        S--;
    }else if(ship == 'D'){
        D--;
    }else if(ship == 'P'){
        P--;
    }
}

    //Main preliminary check when using the hunting search
    //checks if boat is destroyed -> returns to normal searhcing
    //if boat is destroyed but there is another boat found during the hunt search
    //we should continue the hunt on the next boat in the queue
    private void checkQueue(char ship, Board opponenetsBoard){
        if(ship == 'B'){
            if(B == 0){
            hunting = false;
            isVert = false;
            isHori = false;
            foundOrient = false;
            currentHunt = '1';
            checkHuntQueue(opponenetsBoard);
            
            if(!hunting){
                firstPassSearch(opponenetsBoard);
            }else if(hunting){
                huntingBoat(opponenetsBoard);
            }
        }else if(B > 0){
            orientShots(ship, opponenetsBoard);
        }
        }else if(ship == 'A'){
                
            if(A == 0){
                
            hunting = false;
            isVert = false;
            isHori = false;
            foundOrient = false;
            currentHunt = '1';
            checkHuntQueue(opponenetsBoard);
                
            if(!hunting){
                
                firstPassSearch(opponenetsBoard);
            }else if(hunting){
                huntingBoat(opponenetsBoard);
            }
        }else if(A > 0){
            orientShots(ship, opponenetsBoard);
        }
        }else if(ship == 'S'){
            if(S == 0){
            hunting = false;
            isVert = false;
            isHori = false;
            foundOrient = false;
            currentHunt = '1';
            checkHuntQueue(opponenetsBoard);
            if(!hunting){
                firstPassSearch(opponenetsBoard);
            }else if(hunting){
                huntingBoat(opponenetsBoard);
            }
        }else if(S > 0){
            orientShots(ship, opponenetsBoard);
        }
        }else if(ship == 'D'){
            if(D == 0){
            hunting = false;
            isVert = false;
            isHori = false;
            foundOrient = false;
            currentHunt = '1';
            checkHuntQueue(opponenetsBoard);
            if(!hunting){
                firstPassSearch(opponenetsBoard);
            }else if(hunting){
                huntingBoat(opponenetsBoard);
            }
        }else if(D > 0){
            orientShots(ship, opponenetsBoard);
        }
        }else if(ship == 'P'){
            if(P == 0){
                
            hunting = false;
            isVert = false;
            isHori = false;
            foundOrient = false;
            currentHunt = '1';
            checkHuntQueue(opponenetsBoard);
            if(!hunting){
                
                firstPassSearch(opponenetsBoard);
            }else if(hunting){
                huntingBoat(opponenetsBoard);
            }
        }else if(P > 0){
                
            orientShots(ship, opponenetsBoard);
        }
        }
    
    }
    
    //checks if ship is vertical or horizontal -- if found it will progres further into the
    //hunting search -- found in the actualHunt() method
    private void orientShots(char ship, Board opponenetsBoard){
        int tempRow = (int) originRow.get(ship);
        int tempCol = (int) originCol.get(ship);
        
        if(!foundOrient){
           
        for(int i = 0; i < 4; i++){
            System.out.print(tempRow+huntRows[i]);
            
            if(validShot(tempRow+huntRows[i], tempCol+huntCols[i])){
                turnRes = opponenetsBoard.fireAt(tempRow+huntRows[i], tempCol+huntCols[i]);
                firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = 'X';
                        if(turnRes != ' ' && turnRes != ship){
                            adjustBoats(turnRes);
                            //addQueue(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            publishOrigin(turnRes, tempRow+huntRows[i], tempCol+huntRows[i]);
                            
                            break;
                        }else if(turnRes == ship){
                            adjustBoats(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            break;
                        }else{
                            break;
                        }
                        
                       
        }
        
        
        }
    }else if(foundOrient){
            
        actualHunt(ship,opponenetsBoard);
    }
        
    }
    
    //Short hand to input a hits ships coordinates into two hashmaps that serves 
    //as the origin point for our hunt search
    private void publishOrigin(char ship, int row, int col){
        originRow.put(ship, row);
        originCol.put(ship, col);
    }
    
    
    //Another validity check that essentially handles index errors
    //by preventing them from happening in the first place
    private boolean inBoard(int row, int col){
        if(row>= 10 || row<0){
            return false;
        }
        if(col>=10 || col<0){
            return false;
        }
        
        return true;
    }
    
    
    //Sets the relevant boolean variables to tell us 
    //vertical or horizontal and if we have determined the boat is either
    private void checkOrient(char ship){
        int tempRow = (int) originRow.get(ship);
        int tempCol = (int) originCol.get(ship);
        
        
        if(inBoard(tempRow+1, tempCol)){
            if(firedAt[tempRow+1][tempCol] == ship){
                isVert = true;
                foundOrient = true;
            }
            
    }if(inBoard(tempRow-1, tempCol)){
        if(firedAt[tempRow-1][tempCol] == ship){
                isVert = true;
                foundOrient = true;
            }
    }if(inBoard(tempRow, tempCol+1)){
            
        if(firedAt[tempRow][tempCol+1] == ship){
                isHori = true;
                foundOrient = true;
            }
    }if(inBoard(tempRow, tempCol-1)){
       
        if(firedAt[tempRow][tempCol-1] == ship){
            
                isHori = true;
                foundOrient = true;
            }
    }
    
}
    
    
    //The very last block the huting search should hit after detemining the orientation of the boat
    private void actualHunt(char ship, Board opponenetsBoard){
        if(isVert){
            int tempRow = (int) originRow.get(currentHunt);
            int tempCol = (int) originCol.get(currentHunt);
            
            for(int i = 0; i < huntRows.length; i+=2){
            if(inBoard(tempRow+huntRows[i], tempCol+huntCols[i])){
                if(validShot(tempRow+huntRows[i], tempCol+huntCols[i])){
                turnRes = opponenetsBoard.fireAt(tempRow+huntRows[i], tempCol+huntCols[i]);
                firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = 'X';
                        if(turnRes != ' ' && turnRes != currentHunt){
                            adjustBoats(turnRes);
                            //addQueue(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            publishOrigin(turnRes, tempRow+huntRows[i], tempCol+huntRows[i]);
                            break;
                        }else if(turnRes == currentHunt){
                            adjustBoats(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            break;
                        }else{
                            break;          
        }
        }
            }
        }
        
       
        
    }else if(isHori){
            int tempRow = (int) originRow.get(currentHunt);
            int tempCol = (int) originCol.get(currentHunt);
            
            for(int i = 1; i < huntRows.length; i+=2){
            if(inBoard(tempRow+huntRows[i], tempCol+huntCols[i])){
                if(validShot(tempRow+huntRows[i], tempCol+huntCols[i])){
                turnRes = opponenetsBoard.fireAt(tempRow+huntRows[i], tempCol+huntCols[i]);
                firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = 'X';
                        if(turnRes != ' ' && turnRes != currentHunt){
                            adjustBoats(turnRes);
                            //addQueue(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            publishOrigin(turnRes, tempRow+huntRows[i], tempCol+huntRows[i]);
                            break;
                        }else if(turnRes == currentHunt){
                            adjustBoats(turnRes);
                            firedAt[tempRow+huntRows[i]][tempCol+huntCols[i]] = turnRes;
                            break;
                        }else{
                            break;          
        }
        }
            }
        }
        
       
        
    }
    }
    
    //These all set each respective boat randomly on the board
    private char[][] setA(char[][] theBoard){
        Random rand = new Random();
        int tempRow = rand.nextInt(10);
        int tempCol = rand.nextInt(10);
        
        
        boolean canPlace = false;
        boolean keepLooking = true;
        
        while(!canPlace){
            if(inBoard(tempRow+5, tempCol)){
                 boolean run = true;
            for(int i = tempRow; i < tempRow+5; i++){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempRow; i < tempRow+5; i++){
                    theBoard[i][tempCol] = 'A';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow-5, tempCol) && keepLooking){
                 boolean run = true;
            for(int i = tempRow; i > tempRow-5; i--){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempRow-5; i--){
                    theBoard[i][tempCol] = 'A';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol+5) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i < tempCol+5; i++){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempCol; i < tempCol+5; i++){
                    theBoard[tempRow][i] = 'A';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol-5) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i > tempCol-5; i--){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempCol-5; i--){
                    theBoard[tempRow][i] = 'A';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            }
            
            if(!canPlace){
                tempRow = rand.nextInt(10);
                tempCol = rand.nextInt(10);
            }
        }
        
        return theBoard;
        
    }

    private char[][] setB(char[][] theBoard){
        Random rand = new Random();
        int tempRow = rand.nextInt(10);
        int tempCol = rand.nextInt(10);
        
        
        boolean canPlace = false;
        boolean keepLooking = true;
        
        while(!canPlace){
            if(inBoard(tempRow+4, tempCol)){
                 boolean run = true;
            for(int i = tempRow; i < tempRow+4; i++){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempRow; i < tempRow+4; i++){
                    theBoard[i][tempCol] = 'B';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow-4, tempCol) && keepLooking){
                 boolean run = true;
            for(int i = tempRow; i > tempRow-4; i--){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempRow-4; i--){
                    theBoard[i][tempCol] = 'B';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol+4) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i < tempCol+4; i++){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempCol; i < tempCol+4; i++){
                    theBoard[tempRow][i] = 'B';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol-4) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i > tempCol-4; i--){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempCol-4; i--){
                    theBoard[tempRow][i] = 'B';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            }
            
            if(!canPlace){
                tempRow = rand.nextInt(10);
                tempCol = rand.nextInt(10);
            }
        }
        
        return theBoard;
        
    }
    
    private char[][] setS(char[][] theBoard){
        Random rand = new Random();
        int tempRow = rand.nextInt(10);
        int tempCol = rand.nextInt(10);
        
        
        boolean canPlace = false;
        boolean keepLooking = true;
        
        while(!canPlace){
            if(inBoard(tempRow+3, tempCol)){
                 boolean run = true;
            for(int i = tempRow; i < tempRow+3; i++){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempRow; i < tempRow+3; i++){
                    theBoard[i][tempCol] = 'S';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow-3, tempCol) && keepLooking){
                 boolean run = true;
            for(int i = tempRow; i > tempRow-3; i--){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempRow-3; i--){
                    theBoard[i][tempCol] = 'S';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol+3) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i < tempCol+3; i++){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempCol; i < tempCol+3; i++){
                    theBoard[tempRow][i] = 'S';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol-3) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i > tempCol-3; i--){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempCol-3; i--){
                    theBoard[tempRow][i] = 'S';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            }
            
            if(!canPlace){
                tempRow = rand.nextInt(10);
                tempCol = rand.nextInt(10);
            }
        }
        
        return theBoard;
        
    }
    
    private char[][] setD(char[][] theBoard){
        Random rand = new Random();
        int tempRow = rand.nextInt(10);
        int tempCol = rand.nextInt(10);
        
        
        boolean canPlace = false;
        boolean keepLooking = true;
        
        while(!canPlace){
            if(inBoard(tempRow+3, tempCol)){
                 boolean run = true;
            for(int i = tempRow; i < tempRow+3; i++){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempRow; i < tempRow+3; i++){
                    theBoard[i][tempCol] = 'D';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow-3, tempCol) && keepLooking){
                 boolean run = true;
            for(int i = tempRow; i > tempRow-3; i--){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempRow-3; i--){
                    theBoard[i][tempCol] = 'D';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol+3) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i < tempCol+3; i++){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempCol; i < tempCol+3; i++){
                    theBoard[tempRow][i] = 'D';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol-3) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i > tempCol-3; i--){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempCol-3; i--){
                    theBoard[tempRow][i] = 'D';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            }
            
            if(!canPlace){
                tempRow = rand.nextInt(10);
                tempCol = rand.nextInt(10);
            }
        }
        
        return theBoard;
        
    }
    
    private char[][] setP(char[][] theBoard){
        Random rand = new Random();
        int tempRow = rand.nextInt(10);
        int tempCol = rand.nextInt(10);
        
        
        boolean canPlace = false;
        boolean keepLooking = true;
        
        while(!canPlace){
            if(inBoard(tempRow+2, tempCol)){
                 boolean run = true;
            for(int i = tempRow; i < tempRow+2; i++){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempRow; i < tempRow+2; i++){
                    theBoard[i][tempCol] = 'P';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow-2, tempCol) && keepLooking){
                 boolean run = true;
            for(int i = tempRow; i > tempRow-2; i--){
                if(theBoard[i][tempCol] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempRow-2; i--){
                    theBoard[i][tempCol] = 'P';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol+2) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i < tempCol+2; i++){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = tempCol; i < tempCol+2; i++){
                    theBoard[tempRow][i] = 'P';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            //
            }if(inBoard(tempRow, tempCol-2) && keepLooking){
                 boolean run = true;
            for(int i = tempCol; i > tempCol-2; i--){
                if(theBoard[tempRow][i] != ' '){
                run = false;
                break;
                }
            }   
            if(run){
                for(int i = 0; i > tempCol-2; i--){
                    theBoard[tempRow][i] = 'P';
                    
                    canPlace = true;
                    keepLooking = false;
                }    
            }
            }
            
            if(!canPlace){
                tempRow = rand.nextInt(10);
                tempCol = rand.nextInt(10);
            }
        }
        
        return theBoard;
        
    }
    
    //Gets the current lives of the ship passed through
    private int getLives(char ship){
        int a = 0;
        
        if(ship == 'A'){
            a = A;
        }else if(ship == 'B'){
            a = B;
        }else if(ship == 'S'){
            a = S;
        }else if(ship == 'D'){
            a = D;
        }else if(ship == 'P'){
            a = P;
        }
        
        
        return a;
    }
    
    //Shorthand to check the huntQueue to detemine if we should continue hunting 
    //after completing a hunt
    private void checkHuntQueue(Board opponentsBoard){
        if(A == 0 && huntQueue.size()>0){
            hunting=true;
            currentHunt = (char) huntQueue.get(0);
            
        }else if(B == 0 && huntQueue.size()>0){
            hunting=true;
            currentHunt = (char) huntQueue.get(0);
            
        }else if(S == 0 && huntQueue.size()>0){
            hunting=true;
            currentHunt = (char) huntQueue.get(0);
            
        }else if(D == 0 && huntQueue.size()>0){
            hunting=true;
            currentHunt = (char) huntQueue.get(0);
            
        }else if(P == 0 && huntQueue.size()>0){
            hunting=true;
            currentHunt = (char) huntQueue.get(0);
            
        }
    }

}

 

    
    

   