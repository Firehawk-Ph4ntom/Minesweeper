import java.util.Random;

public class MinesweeperBoard
{
    // size of board
    private int size;
    // number of mines
    private int mines;
    // 2D array for the board, minecount, revealed tiles, flagged tiles, and shield tiles
    // handles the boundary of where to select
    private char[][] board;
    private int[][] mineCount;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private boolean[][] shielded;

    // empty tiles are considered "Empty" while mined tiles are considered "Mined"
    // variables used in the random generation
    private static final int EMPTY = 0;
    private static final int MINE = 1;

    // default
    public MinesweeperBoard(int size, int mines) {
        this.size = size;
        this.mines = mines;
        board = new char[size][size];
        mineCount = new int[size][size];
        revealed = new boolean[size][size];
        flagged = new boolean[size][size];
        shielded = new boolean[size][size];
        initializeBoard();
    }

    // all of the board is initialized as empty at first when generated once the program is first launched
    // the board then fills up later on with numbers and mines
    private void initializeBoard()
    {
        for (int row = 0; row < size; row++)
        {
            for (int column = 0; column < size; column++)
            {
                board[row][column] = EMPTY;
                revealed[row][column] = false;
                flagged[row][column] = false;
                shielded[row][column] = false;
                mineCount[row][column] = 0;
            }
        }
    }

    // function that handles mine placement
    public void placeMines(int initialRow, int initialColumn)
    {
        // random placement, and a counter for the mines that exist
        Random random = new Random();
        int minesPlaced = 0;
        // num of mines placed cant exceed the total num of mines
        // if the num of mines placed is less than total, fetch a random tile and turn it into a mined tile
        while (minesPlaced < mines)
        {
            int row = random.nextInt(size);
            int column = random.nextInt(size);

            if ((row != initialRow || column != initialColumn) && board[row][column] != MINE)
            {
                board[row][column] = MINE;
                minesPlaced++;
            }
        }
        // call a second function
        calculateMineCounts();
    }

    // used to check the tiles adjacent to mines to place the number tiles
    // there are tiles where mines can be adjacent to, so to calculate the number, we need to check each adjacent tile
    // in the end, there might be some tiles where there are no adjacent mines, those remain empty
    // empty tiles have no number
    private void calculateMineCounts()
    {
        // go through adjacent directions 1 tile at a time (left/right/up/down)
        int[] directions = {-1, 0, 1};
    
        // traverse board
        for (int row = 0; row < size; row++)
            for (int column = 0; column < size; column++)
                // Only continue if tile is not a mine
                if (board[row][column] != MINE)
                    // get the row and column of the adjacent tiles
                    for (int direction_row : directions)
                        for (int direction_column : directions)
                        {
                            int newRow = row + direction_row;
                            int newColumn = column + direction_column;

                            // check if the tile being traversed is within the boundaries of the board, and check if that specific tile is also a mine
                            if (isInBounds(newRow, newColumn) && board[newRow][newColumn] == MINE)
                                mineCount[row][column]++;
                        }
    }
    
    // check if tile is in board bounds
    //necessary function as a problem arised when trying to find the adjacent tiles of a tile that was at frame border
    public boolean isInBounds(int row, int column) {
        return row >= 0 && row < size && column >= 0 && column < size;
    }

    // get the number of mines
    public int getMineCount(int row, int column) {
        return mineCount[row][column];
    }
    
    // check if the tile is revealed
    public boolean isRevealed(int row, int column) {
        return revealed[row][column];
    }
    
    // function that reveals the current tile
    public void setRevealed(int row, int column, boolean value) {
        revealed[row][column] = value;
    }
    
    // check if tile is flagged
    public boolean isFlagged(int row, int column) {
        return flagged[row][column];
    }

    // function that sets the current tile as flagged
    public void setFlagged(int row, int column, boolean value) {
        flagged[row][column] = value;
    }
    
    // check if tile has a mine
    public boolean isMine(int row, int column) {
        return board[row][column] == MINE;
    }
    
    // get the size of the board
    public int getSize() {
        return size;
    }
    
    // get the number of mines on the board
    public int getMines() {
        return mines;
    }
}