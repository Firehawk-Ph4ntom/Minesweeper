import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.Random;

public class MinesweeperGUI extends JFrame implements ActionListener
{
    private MinesweeperBoard board; // MinesweeperBoard class
    private JButton[][] buttons; // 2D Array of buttons
    private JFrame frame = new JFrame("Minesweeper"); // frame
    private JMenuBar menuBar = new JMenuBar(); // menu bar
    private JMenu gameMenu = new JMenu("Menu"); // menu
    // menu elements (for difficulties and restart)
    private JMenuItem helpMenuItem = new JMenuItem("Help"); 
    private JMenuItem easyMenuItem = new JMenuItem("Easy");; 
    private JMenuItem mediumMenuItem = new JMenuItem("Medium");;
    private JMenuItem hardMenuItem = new JMenuItem("Hard");;
    private JMenuItem restartMenuItem = new JMenuItem("Restart");;
    private JLabel flagsLabel; // Label to display number of flags
    private int flagsAvailable; // Number of flags available
    private boolean firstClick; // flag for first click (for mine generation)
    private Timer timer; // Timer to track the time
    private JLabel timerLabel = new JLabel("Time: 00:00:00:00"); // Label to display the timer

    private JLabel shieldsLabel; // Label to display number of shields
    private int shieldsAvailable; // Number of shields available

    // external icons for shield, mine, flag, and tiles
    private ImageIcon unrevealedIcon = new ImageIcon("../resources/icons/unrevealed_tile.png");
    private ImageIcon revealedIcon = new ImageIcon("../resources/icons/revealed_tile.png");
    private ImageIcon flagIcon = new ImageIcon("../resources/icons/flag_icon.png");
    private ImageIcon wrongFlagIcon = new ImageIcon("../resources/icons/wrong_flag_icon.png");
    private ImageIcon mineIcon = new ImageIcon("../resources/icons/mine_icon.png");
    private ImageIcon mineClickedIcon = new ImageIcon("../resources/icons/mine_clicked_icon.png");
    private ImageIcon shieldIcon = new ImageIcon("../resources/icons/shield_icon.png");
    private ImageIcon numberIcon;
    private Image variableImage;

    private long startTime = 0, currentTime = 0, elapsedTime = 0;

    private String message = "Welcome to the Minesweeper game!\n\n"
    + "Here's all what you need to know about this game:\n"
    + "1. The board will generate tiles for you to click on.\n"
    + "2. Each tile either contains a mine or is a free tile (doesn't contain a mine).\n"
    + "3. Your job is to clear the board of mines by using right click (placing a flag) "
    + "or revealing all the free tiles.\n\n"
    + "Gameplay details:\n"
    + "- The initial total number of flags represents the number of mines on the board.\n"
    + "- The total number of mines on the board is also indicated in the difficulty descriptions.\n"
    + "- You start with 2 shields; if you click on a mine accidentally, they will protect you.\n"
    + "- Once all shields are depleted, you will lose if you click on a mine again.\n\n"
    + "Good luck playing!";

    // Randomize sound effect for opening up a tile
    private Random random = new Random();

    // default
    private MinesweeperGUI()
    {
        // call the function to create a new board first and foremost
        getBoard(10, 10);

        // handles program termination
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // tooltips for the menu settings (hovering the mouse over the buttons reveals the text desc)
        helpMenuItem.setToolTipText("Display the game info on a seperate screen");
        restartMenuItem.setToolTipText("Restart the game (current difficulty level)");
        easyMenuItem.setToolTipText("Play on easy mode (5x5 grid, 5 mines)");
        mediumMenuItem.setToolTipText("Play on medium mode (10x10 grid, 20 mines)");
        hardMenuItem.setToolTipText("Play on hard mode (15x15 grid, 45 mines)");

        // method references to the Action Listener
        helpMenuItem.addActionListener(this::help);
        easyMenuItem.addActionListener(this::diffEasy);
        mediumMenuItem.addActionListener(this::diffMedium);
        hardMenuItem.addActionListener(this::diffHard);
        restartMenuItem.addActionListener(this::restartCurrent);

        // add items to the game menu
        gameMenu.add(helpMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(easyMenuItem);
        gameMenu.add(mediumMenuItem);
        gameMenu.add(hardMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(restartMenuItem);
    
        menuBar.add(gameMenu);
    
        frame.setPreferredSize(new Dimension(520, 600));
        frame.setResizable(false);
        frame.setJMenuBar(menuBar);

        // panel for the flag counter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        flagsLabel = new JLabel("Flags remaining: " + flagsAvailable); // Initialize flags label
        shieldsLabel = new JLabel("Shields remaining: " + shieldsAvailable); // Initialize shields label

        flagsLabel.setBorder(new EmptyBorder(0, 20, 0, 20)); // Add spacing
        shieldsLabel.setBorder(new EmptyBorder(0, 20, 0, 20)); // Add spacing
        timerLabel.setBorder(new EmptyBorder(0, 20, 0, 20)); // Add spacing

        // add panel elements
        topPanel.add(flagsLabel);
        topPanel.add(timerLabel);
        topPanel.add(shieldsLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // panel size based on board size
        JPanel centerPanel = new JPanel(new GridLayout(board.getSize(), board.getSize()));
        initializeButtons(board.getSize(), centerPanel);
        frame.add(centerPanel, BorderLayout.CENTER);

        // resize the frame automatically based on the components available
        frame.pack();
        //center frame to center of the screen
        frame.setLocationRelativeTo(null);
        // make everything visible
        frame.setVisible(true);
    }

    private void getBoard(int size, int mines)
    {
        // new instance of the Board class
        board = new MinesweeperBoard(size, mines);
        buttons = new JButton[size][size];
        flagsAvailable = mines; // Initialize flags available
        shieldsAvailable = 2; // Initialize shields available
        firstClick = true;

        // Initialize the timer
        int timerDelay = 10; // milliseconds
        ActionListener tListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event) {
                calculateElapsedTime();
                updateTimer();
            }
        };
        timer = new Timer(timerDelay, tListener);
    }

    private void calculateElapsedTime()
    {
        currentTime = System.nanoTime();
        elapsedTime += currentTime - startTime;
        startTime = currentTime;
    }

    private void updateTimer()
    {
        long milliseconds;
        int hours, minutes, seconds, centiseconds;

        milliseconds = elapsedTime / 1000000;

        centiseconds = (int) (milliseconds / 10);
        seconds = (int) (centiseconds / 100);
        minutes = (int) (seconds / 60);
        hours = (int) (minutes / 60);

        centiseconds = centiseconds % 100;
        seconds = seconds % 60;
        minutes = minutes % 60;

        String time = String.format("Time: %02d:%02d:%02d:%02d", hours, minutes, seconds, centiseconds);
        timerLabel.setText(time);
    }

    // difficulties:
    // easy 5x5 board
    // med 10x20 board
    // hard 15x45 board
    private void diffEasy(ActionEvent e) {
        restartGame(10, 10);
    }
    
    private void diffMedium(ActionEvent e) {
        restartGame(15, 20);
    }
    
    private void diffHard(ActionEvent e) {
        restartGame(20, 60);
    }
    
    private void restartCurrent(ActionEvent e) {
        restartGame(board.getSize(), board.getMines());
    }
    
    private void help(ActionEvent e) {    
        JOptionPane.showMessageDialog(this, message, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void initializeButtons(int size, JPanel panel)
    {
        // needed to set all buttons back to blank 
        panel.removeAll();

        // Check if size matches the dimensions of buttons array
        // Resize buttons depending on size of the frame
        if (buttons.length != size || buttons[0].length != size)
            buttons = new JButton[size][size];

        // create a new button, for loops depending on the difficulty aka board/array size
        for (int row = 0; row < size; row++)
        {
            for (int column = 0; column < size; column++)
            {
                // new button instance
                buttons[row][column] = new JButton();
                // preferred size for buttons initially before resizing
                buttons[row][column].setPreferredSize(new Dimension(50, 50));
                // don't use java's button texture or border, instead we want our own images and style
                buttons[row][column].setContentAreaFilled(false);
                buttons[row][column].setBorderPainted(false);
                // set button to our icon
                buttons[row][column].setIcon(unrevealedIcon);
                // button clicked by left or right mouse button has 2 different functions
                // left mouse button to reveal the tile
                // right mouse to place or remove a flag

                // row and column variables must be final
                final int final_row = row;
                final int final_column = column;

                buttons[row][column].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent evt) {
                        if (SwingUtilities.isRightMouseButton(evt)) {
                            flagCell(final_row, final_column);
                        }
                    }
                });

                buttons[row][column].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleButtonClick(final_row, final_column);
                    }
                });

                // add button to panel
                panel.add(buttons[row][column]);
            }
        }

        panel.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeIcons();
            }
        });

        // repaint and redraw frame
        frame.revalidate();
        frame.repaint();
    }

    private void resizeIcons()
    {
        int iconSize = buttons[0][0].getWidth();
        for (int row = 0; row < board.getSize(); row++)
        {
            for (int column = 0; column < board.getSize(); column++)
            {
                if (!board.isRevealed(row, column) && !board.isFlagged(row, column))
                {
                    // If the cell is unrevealed and not flagged, resize the unrevealed icon
                    variableImage = unrevealedIcon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                    buttons[row][column].setIcon(new ImageIcon(variableImage));
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JButton clickedButton = (JButton) e.getSource();
        for (int row = 0; row < board.getSize(); row++)
        {
            for (int column = 0; column < board.getSize(); column++)
            {
                if (buttons[row][column] == clickedButton)
                {
                    handleButtonClick(row, column);
                    return;
                }
            }
        }
    }

    // handles audio events
    // checking if audio exists, if not throw exception
    private void playSound(String soundFileName)
    {
        try {
            File soundFile = new File(soundFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    // sound handling for different variations of block hit/ mine hit
    private void playBlockHitSound() {
        String[] blockHitSounds =
        {
            "../resources/audio/Block/BlockReveala.wav",
            "../resources/audio/Block/BlockRevealb.wav"
        };
        int index = random.nextInt(blockHitSounds.length);
        playSound(blockHitSounds[index]);
    }

    private void playMineHitSound() {
        String[] mineHitSounds = {
            "../resources/audio/Explosion/BlockExploa.wav",
            "../resources/audio/Explosion/BlockExplob.wav",
            "../resources/audio/Explosion/BlockExploc.wav"
        };
        int index = random.nextInt(mineHitSounds.length);
        playSound(mineHitSounds[index]);
    }

    private void handleButtonClick(int row, int column)
    {
        // depending on row or column
        // if the click on the board is the very first click, the mines then generate after the first click
        // this was done to prevent mines from generating before the first click and player losing instantly
        // also starts the timer
        if (firstClick)
        {
            //placeMines function from the constructor is called to place mines if the first click is registered
            board.placeMines(row, column);
            firstClick = false;
            startTime = System.nanoTime();
            timer.start();
        }
        //this flag is necessary in order to prevent the user from left-clicking a flagged mine and losing
        if (!board.isFlagged(row, column))
        {
            // Check if the tile is not already revealed
            if (!board.isRevealed(row, column))
            {
                // Update the icon of the clicked tile
                revealCell(row, column);

                if (isGameWon())
                {
                    timer.stop();
                    // reveal all the tiles when the round ends and then restart the board
                    revealAllCells();
                    playSound("../resources/audio/Victory.wav"); // Play victory sound
                    JOptionPane.showMessageDialog(null, "Congratulations! You won the game without stepping on any mines!");
                    restartGame(board.getSize(), board.getMines());
                }
                else if (board.isMine(row, column))
                {
                    if (shieldsAvailable > 0)
                    {
                        playSound("../resources/audio/Shield_Triggered.wav"); // play shield sound effect
                        shieldsAvailable--; // Use a shield
                        board.setFlagged(row, column, true); // Auto-flag the mine so the user can no longer click on it
                        flagsLabel.setText("Flags remaining: " + flagsAvailable); // Update flags label
                        shieldsLabel.setText("Shields remaining: " + shieldsAvailable); // Update shields label
                    }
                }
                else
                    playBlockHitSound();
            }
        }
    }

    // handles the tile when clicked, the contents are revealed
    private void revealCell(int row, int column)
    {
        // if the board is already revealed or flagged, do nothing
        // also double checks if the board is within the array and not out of bounds, used as a double checker 
        if (!board.isInBounds(row, column) || board.isRevealed(row, column) || board.isFlagged(row, column))
            return;

        // reveal the tile
        board.setRevealed(row, column, true);

        // if the tile is a mine at pos, then change the icon to a mine icon
        if (board.isMine(row, column))
        {
            // however use shield icon if shield available
            if (shieldsAvailable > 0)
            {
                variableImage = shieldIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                buttons[row][column].setIcon(new ImageIcon(variableImage));
            }
            else
            {
                // handles game loss
                // highlight selected mine, and reveal all the board along with displaying message to the player
                variableImage = mineClickedIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                buttons[row][column].setIcon(new ImageIcon(variableImage));
                timer.stop();
                revealAllCells();
                playMineHitSound();
                JOptionPane.showMessageDialog(frame, "Game Over! You clicked on a mine.");
                restartGame(board.getSize(), board.getMines());
            }
        }
        else
        {
            // else calculate the remaining mine count
            // if the tile does not have any adjacent mines, it is an empty tile
            if (board.getMineCount(row, column) == 0)
            {
                // Set the icon to the revealed tile icon for empty tiles
                variableImage = revealedIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                buttons[row][column].setIcon(new ImageIcon(variableImage));

                // all adjacent directions are checked by 1, recursively, if there are more tiles without any mines next to them
                int[] directions = {-1, 0, 1};
                for (int direction_row : directions)
                {
                    for (int direction_column : directions)
                    {
                        if (direction_row != 0 || direction_column != 0)
                            revealCell(row + direction_row, column + direction_column); // Recursively reveal adjacent tiles
                    }
                }
            }
            else
            {
                // Display the number on the tile that is adjacent to the mine
                numberIcon = new ImageIcon("../resources/icons/number_" + board.getMineCount(row, column) + ".png");
                variableImage = numberIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                buttons[row][column].setIcon(new ImageIcon(variableImage));
            }
        }
    }

    // reveal all the tiles in the entire board when the round ends whether a win or lose
    // this function is responsible for that as well as handling the images that show for each respective tile
    private void revealAllCells()
    {
        for (int row = 0; row < board.getSize(); row++)
        {
            for (int column = 0; column < board.getSize(); column++)
            {
                if (!board.isRevealed(row, column))
                {
                    //if tile is a mine and isnt flagged, just reveal the mine at the end of the game
                    if (board.isMine(row, column))
                    {
                        if (!board.isFlagged(row, column))
                        {
                            variableImage = mineIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                            buttons[row][column].setIcon(new ImageIcon(variableImage));
                        }
                    }
                    else if (board.isFlagged(row, column))
                    {
                        // if the tile has a flag and is a mine, show the flag instead of the mine
                        if (board.isMine(row, column))
                        {
                            variableImage = flagIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                            buttons[row][column].setIcon(new ImageIcon(variableImage));
                        }
                        else
                        {
                            // Display the alternative flag icon if the tile is flagged but isnt a mine
                            variableImage = wrongFlagIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                            buttons[row][column].setIcon(new ImageIcon(variableImage));
                        }
                    }
                    else if (board.getMineCount(row, column) == 0)
                    {
                        // if the tile is empty, just replace the icon to the empty icon
                        variableImage = revealedIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                        buttons[row][column].setIcon(new ImageIcon(variableImage));
                    }
                    else
                    {
                        // depending on the number of adjacent mines, replace the tile texture with a proper number representing the mine count
                        numberIcon = new ImageIcon("../resources/icons/number_" + board.getMineCount(row, column) + ".png");
                        variableImage = numberIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                        buttons[row][column].setIcon(new ImageIcon(variableImage));
                    }
                    board.setRevealed(row, column, true);
                }
            }
        }
        frame.revalidate(); // Revalidate the frame
        frame.repaint(); // Repaint the frame
    }

    // if the tile has been flagged this function is called
    private void flagCell(int row, int column)
    {
        if (!board.isRevealed(row, column))
        {
            // if the tile is already flagged, and the user right clicks again on that tile
            if (board.isFlagged(row, column))
            {
                playSound("../resources/audio/Flag/FlagRemoved.wav"); // Play flag removed sound
                variableImage = unrevealedIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                buttons[row][column].setIcon(new ImageIcon(variableImage)); // Change icon back to unrevealed
                board.setFlagged(row, column, false);
                flagsAvailable++; // re-increase the num of flags available when removed
            }
            else
            {
                //check the flag counter
                //* Note: total flag counter = total mine counter */
                if (flagsAvailable > 0)
                {
                    // Add flag if tile was not already flagged
                    playSound("../resources/audio/Flag/FlagPlaced.wav"); // Play flag placed sound
                    variableImage = flagIcon.getImage().getScaledInstance(buttons[row][column].getWidth(), buttons[row][column].getHeight(), Image.SCALE_SMOOTH);
                    buttons[row][column].setIcon(new ImageIcon(variableImage));
                    board.setFlagged(row, column, true);
                    flagsAvailable--; // Decrease flags available when flag is placed
                }
                else
                // if flag counter is 0
                    JOptionPane.showMessageDialog(null, "You don't have any flags left!");
            }
            flagsLabel.setText("Flags remaining: " + flagsAvailable); // Update flags label
        }
    }

    //function that handles victory
    private boolean isGameWon()
    {
        // if the tiles left to reveal is equal to the total number of tiles - tiles that include mines
        // the more the tiles get revealed, the more the revealedCells counter gets increased until it reaches the limit, then declare victory
        
        //=============================================
        //if there is a scenario where the user manages to flag all the mines on the board
        //despite not revealing all the number/empty tiles, declare that as an alternate victory as well ------ done

        // alternate victory case bug: when triggering a shield, alternate victory scenario can no longer be achieved
        
        // rare case scenario when game isnt a victory despite all tiles being revealed except for the mines ------ FIXED
        //=============================================

        int cellsToReveal = board.getSize() * board.getSize() - board.getMines();
        int revealedCells = 0;
        int flaggedMines = 0;
        for (int row = 0; row < board.getSize(); row++)
        {
            for (int column = 0; column < board.getSize(); column++)
            {
                if (board.isRevealed(row, column) && !board.isMine(row, column))
                    revealedCells++;
                else if (board.isRevealed(row, column) && board.isMine(row, column))
                    continue;
                
                if (board.isMine(row, column) && board.isFlagged(row, column))
                    flaggedMines++;
            }
        }
        return revealedCells == cellsToReveal || flaggedMines == board.getMines();
    }

    private void restartGame(int newSize, int newMines) {
        // new instance of the Board
        board = new MinesweeperBoard(newSize, newMines);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel centerPanel = new JPanel(new GridLayout(newSize, newSize));
        firstClick = true;

        // Create new components
        shieldsAvailable = 2;
        flagsAvailable = newMines;
        flagsLabel = new JLabel("Flags remaining: " + flagsAvailable); // Reinitialize flags label
        shieldsLabel = new JLabel("Shields remaining: " + shieldsAvailable); // Initialize shields label
        topPanel.add(flagsLabel); // Add flags label to top panel
        topPanel.add(timerLabel); // Add time label
        topPanel.add(shieldsLabel); // Add shield label
        initializeButtons(newSize, centerPanel); // Pass both size and JPanel

        // Clear and update the frame
        frame.getContentPane().removeAll(); // Clear the frame
        frame.add(topPanel, BorderLayout.NORTH); // Add top panel back to frame
        frame.add(centerPanel, BorderLayout.CENTER); // Add center panel back to frame

        //reset the timer back to 0
        elapsedTime = 0;
        updateTimer();
        timer.stop();

        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        new MinesweeperGUI();
    }
}