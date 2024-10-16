import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.font.TextAttribute;
import javax.swing.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

//The Display is the region in the window where drawing occurs.
public class Display extends JComponent implements
    KeyListener, // need for keyboard input
    MouseListener // need for mouse input
{
  // main method for testing
  public static void main(String[] args) throws IOException {
    Display display = new Display();
    display.run();
  }

  private String[] villainNames = {
      "Wordle",
      "Glarbon the Unconquerable",
      "Bowser's Cousin",
      "Fear Itself",
      "Taxes",
      "Anime Character",
      "Garbanzo",
      "HA! GOTEEM",
      "Draco Malfoy",
      "I Need a One Dance",
      "Stylish Bean Bag",
      "Ninja Without Shurikens",
      "The Darkness from Without",
      "GOAT'ed on the Sticks",
      "Subscribe for More Content",
      "Imperialism",
      "Fortnight, not Fortnite",
      "They Call me the Gobbler",
      "Bad at Villainy",
      "Prime Number" };

  private Location[] villainLocations = { new Location(462, 200),
      new Location(550, 300),
      new Location(300, 200),
      new Location(475, 275),
      new Location(400, 200) };

  // Orange Guy: 462, 200
  // Robot: 550, 300
  // Elephant: 300, 200
  // Seal: 475, 275
  // Bowser: 400, 200

  private int screen;

  private ArrayList<String> ldEasy;
  private ArrayList<String> ldMedi;
  private ArrayList<String> ldHard;
  private boolean leaderboardCompiled = false;
  private boolean leaderboardDone = false;

  private Image image; // image to draw
  private int heroX; // position of left edge of image
  private int heroY; // position of top edge of image
  private int yourHealth;

  final Color GREEN = new Color(50, 225, 50);
  final Color RED = new Color(255, 40, 40);

  private String gameDifficulty;
  private int gameLength;
  private String toType = "";
  private String yourType = "";
  private String lineOfYourType = "";
  private String yourTypeGreen = "";
  private String yourTypeRed = "";
  private boolean inTheWrong;

  private Location cursor;
  private boolean cursorOn;
  private int cursorCount;
  private int lineNumber;
  private int deltaHealth;
  private int continuousDeltaHealth;
  private int changeInCont;
  private int changeInDE;

  private boolean finishedToType;

  private long timeTaken;

  private ArrayList<Sprite> bosses;
  private ArrayList<ArrayList<Image>> bossImage;
  private int bossIndex;

  private Color bossHealthColor;
  private Color yourHealthColor;

  private boolean alreadyReduced;

  public void addBoss() {
    if (bosses.size() - 1 == bossIndex) {
      bossImage.add(new ArrayList<Image>());
      int random = (int) (Math.random() * 5 + 1);
      bossImage.get(bossIndex + 1).add(new ImageIcon(getClass().getResource("Villain" + random + "-1.png")).getImage());
      bosses.add(new Sprite(villainLocations[random - 1], true, bossImage.get(bossIndex + 1),
          bossIndex * 100 + 50 + (int) (Math.random() * 100), 0,
          villainNames[(int) (Math.random() * villainNames.length)]));
    }

  }

  public Display() throws IOException {

    screen = 0;
    alreadyReduced = false;

    ldEasy = new ArrayList<String>();
    ldMedi = new ArrayList<String>();
    ldHard = new ArrayList<String>();

    bosses = new ArrayList<Sprite>();
    bossImage = new ArrayList<ArrayList<Image>>();

    continuousDeltaHealth = 1;
    deltaHealth = 10;
    yourHealth = 500;
    bossIndex = 0;
    heroX = 0;
    heroY = 200;
    gameLength = 1000;
    inTheWrong = false;
    toType = randomSentence();
    cursor = new Location(0, 0);
    cursorOn = true;
    cursorCount = 0;
    lineNumber = 1;

    finishedToType = false;

    bossHealthColor = Color.WHITE;
    yourHealthColor = Color.WHITE;

    timeTaken = System.currentTimeMillis();

    // load image
    String fileName = "Hero.PNG";
    URL url = getClass().getResource(fileName);
    if (url == null)
      throw new RuntimeException("Unable to load: " + fileName);
    image = new ImageIcon(url).getImage();

    JFrame frame = new JFrame(); // create window
    frame.setTitle("Type Battle"); // set title of window
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closing window will exit program
    setPreferredSize(new Dimension(gameLength, gameLength)); // set size of drawing region
    frame.getContentPane().setBackground(new Color(20, 20, 50));

    // need for keyboard input
    setFocusable(true); // indicates that Display can process key presses
    addKeyListener(this); // will notify Display when a key is pressed

    // need for mouse input
    addMouseListener(this); // will notify Display when the mouse is pressed

    frame.getContentPane().add(this); // add drawing region to window
    frame.pack(); // adjust window size to fit drawing region
    frame.setVisible(true); // show window

    bossImage.add(new ArrayList<Image>());
    int random = (int) (Math.random() * 5 + 1);
    bossImage.get(0).add(new ImageIcon(getClass().getResource("Villain" + random + "-1.png")).getImage());
    bosses.add(new Sprite(villainLocations[random - 1], true, bossImage.get(0),
        bossIndex * 100 + 50 + (int) (Math.random() * 100), 0,
        villainNames[(int) (Math.random() * villainNames.length)]));

  }

  public void drawText(Graphics g, String text, Color color, boolean underline) {

    if (!underline) {
      Font typeFont = new Font("MonoSpaced", Font.PLAIN, 30);
      g.setColor(color);
      g.setFont(typeFont);
    } else {
      // Underline Code TO-DO
      HashMap<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
      fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
      Font underlineFont = new Font("MonoSpaced", Font.PLAIN, 30).deriveFont(fontAttributes);
      g.setColor(color);
      g.setFont(underlineFont);
    }

    int toTypeWidth = g.getFontMetrics().stringWidth(text);

    int lines = 1;
    if (toTypeWidth > 2 * (gameLength - 30))
      lines = 3;
    else if (toTypeWidth > gameLength - 30)
      lines = 2;

    String line1 = "";
    String line2 = "";
    String line3 = "";

    int lineBreak = 0;

    if (lines > 1) {
      for (int i = 1; i <= gameLength - 30; i++) {
        if (Math.abs(g.getFontMetrics().stringWidth(text.substring(0, i)) - 970) <= 10) {
          lineBreak = i;
          break;
        }
      }

      line1 = text.substring(0, lineBreak);
      if (lines > 2) {
        line2 = text.substring(lineBreak, 2 * lineBreak);
        line3 = text.substring(2 * lineBreak);
      } else
        line2 = text.substring(lineBreak);
    } else
      line1 = text;

    for (int i = 0; i < lines; i++) {
      String lineToType = "";

      if (i == 0)
        lineToType = line1;
      else if (i == 1)
        lineToType = line2;
      else if (i == 2)
        lineToType = line3;
      g.drawString(lineToType, 15, 40 + i * 30);
    }
  }

  public String randomSentence() throws IOException {
    BufferedReader br = new BufferedReader(new FileReader("sentences.txt"));

    int rand = (int) (Math.random() * 198);

    String sentence = br.readLine();

    for (int i = 0; i < rand; i++) {
      sentence = br.readLine();
    }

    br.close();
    return sentence;
  }

  public void reduceBossHealth(int targetBossHealth, Graphics g) {
    bossHealthColor = RED;
    yourHealthColor = GREEN;
    yourHealth += bosses.get(bossIndex).getHealth() - targetBossHealth;
    while (bosses.get(bossIndex).getHealth() > targetBossHealth) {
      bosses.get(bossIndex).setHealth(bosses.get(bossIndex).getHealth() - 1);
      repaint();
      // try {
      // Thread.sleep(5);
      // } catch (Exception e) {

      // }
    }
  }

  // called automatically when Java needs to draw the Display
  public void paintComponent(Graphics g) {
    if (screen == 0) {
      Font boldFont = new Font("MonoSpaced", Font.BOLD, 30);

      g.setColor(Color.WHITE);
      g.setFont(boldFont);

      g.drawString("Welcome to TYPE BATTLE!", 30, 50);

      Font typeFont = new Font("MonoSpaced", Font.PLAIN, 30);
      g.setFont(typeFont);

      g.drawString("You are the magical top hat wizard Typegon the Third.", 30, 100);
      g.drawString("On your typing misadventures, you have stumbled upon", 30, 130);
      g.drawString("a syndicate of evil bosses. Your task, should you", 30, 160);
      g.drawString("accept, is to defeat them. ", 30, 190);

      g.setFont(boldFont);
      g.drawString("Rules: ", 30, 240);

      g.setFont(typeFont);
      g.drawString("Correctly type the sentence in the box to fight.", 30, 270);
      g.drawString("Correctly typed characters gain you health, while", 30, 300);
      g.drawString("incorrect characters take away from your health.", 30, 330);
      g.drawString("After completing a sentence, the characters per", 30, 360);
      g.drawString("minute that you typed will be added to your health", 30, 390);
      g.drawString("and taken from the boss's health.", 30, 420);
      g.setFont(boldFont);
      g.drawString("You constantly lose health due to a barrage of evil,", 30, 450);
      g.drawString("malicious, heinous, and unseen attacks.", 30, 480);
      g.drawString("Difficulty increases with stage number.", 30, 510);

      g.drawString("Defeat as many bosses as you can!", 30, 560);

      g.setFont(new Font("MonoSpaced", Font.BOLD, 50));
      g.drawString("Press the e-key for easy mode,", 30, 610);
      g.drawString("the m-key for medium mode, ", 30, 660);
      g.drawString("and the h-key for hard mode.", 30, 710);
      g.setFont(new Font("MonoSpaced", Font.ITALIC, 40));
      g.drawString("There is a leaderboard to get on.", 30, 750);

    }
    if (screen == 1 && yourHealth > 0) {
      if (yourType.length() == 0)
        timeTaken = System.currentTimeMillis();
      if (!finishedToType) {
        g.setColor(Color.WHITE);
        g.drawRect(10, 10, gameLength - 20, gameLength / 10);
        g.fillRect(10, 10, gameLength - 20, gameLength / 10);

        g.drawRect(10, 20 + gameLength / 10, gameLength - 20, 40);
        g.fillRect(10, 20 + gameLength / 10, gameLength - 20, 40);

        drawText(g, toType, Color.BLACK, false);
        drawText(g, yourTypeRed, RED, true);
        drawText(g, yourTypeGreen, GREEN, true);

        Font typeFont = new Font("MonoSpaced", Font.PLAIN, 30);
        g.setColor(Color.GRAY);
        g.setFont(typeFont);

        int yourTypeWidth = g.getFontMetrics().stringWidth(yourType);

        if (yourTypeWidth <= 970) {
          lineOfYourType = yourType;
          cursor.setX(g.getFontMetrics().stringWidth(lineOfYourType) + 15);
          lineNumber = 1;
        } else {
          int lineBreak = 0;
          for (int i = 1; i <= gameLength - 30; i++) {
            if (Math.abs(g.getFontMetrics().stringWidth(yourType.substring(0, i)) - 970) <= 10) {
              lineBreak = i;
              break;
            }
          }
          if (yourTypeWidth <= 2 * 970) {
            lineOfYourType = yourType.substring(lineBreak);
            cursor.setX(g.getFontMetrics().stringWidth(lineOfYourType) + 15);
            lineNumber = 2;
          } else {
            lineOfYourType = yourType.substring(2 * lineBreak);
            cursor.setX(g.getFontMetrics().stringWidth(lineOfYourType) + 15);
          }
        }
        cursor.setY(55 + gameLength / 10);
        g.drawString(lineOfYourType, 10, 55 + gameLength / 10);

        if (cursorOn) {
          if (lineNumber == 1) {

          }
          g.drawLine(cursor.getX(), cursor.getY() - 30, cursor.getX(), cursor.getY());
        }
        if (cursorCount == 0)
          cursorOn = !cursorOn;

      }

      else {

        g.setColor(Color.WHITE);
        g.drawRect(10, 10, gameLength - 20, (gameLength / 10) / 3);
        g.fillRect(10, 10, gameLength - 20, (gameLength / 10) / 3);

        int cpm = (int) ((double) (toType.length() / (((double) timeTaken / 1000) / 60)));
        drawText(g, "Characters Per Minute: " + cpm + "\t Press ENTER to Continue",
            Color.BLACK,
            false);

        int targetBossHealth = bosses.get(bossIndex).getHealth() - cpm;

        if (!alreadyReduced) {
          reduceBossHealth(targetBossHealth, g);
          alreadyReduced = true;
        }
      }

      g.drawImage(image, heroX, heroY, null); // draw image.gif at (heroX, heroY)

      g.setColor(yourHealthColor);
      g.drawString("Your Health: " + yourHealth, 100, 200);

      if (bosses.get(bossIndex).getHealth() > 0) {
        g.setColor(bossHealthColor);
        g.drawString("Health: " + bosses.get(bossIndex).getHealth(), 600, 200);
        g.drawImage(bosses.get(bossIndex).getState(), bosses.get(0).getLoc().getX(), bosses.get(0).getLoc().getY(),
            null);
      } else {
        Font boldFont = new Font("MonoSpaced", Font.BOLD, 30);
        g.setColor(RED);
        g.setFont(boldFont);
        g.drawString("Health: Terminated", 600, 200);
        g.setFont(new Font("MonoSpaced", Font.BOLD, 30));
        g.drawImage(bosses.get(bossIndex).getState(), bosses.get(0).getLoc().getX(), bosses.get(0).getLoc().getY(),
            null);
      }

      g.setColor(RED);
      Font boldFont = new Font("MonoSpaced", Font.BOLD, 30);
      g.setFont(boldFont);
      g.drawString("Stage " + (bossIndex + 1), 250, 720);
      g.drawString("Villain Name: " + bosses.get(bossIndex).getName(), 250, 750);
    } else if (screen == 1) {
      screen = 2;
      yourType = "";
    } else if (screen == 2) {
      g.drawImage(image, heroX, heroY + 10, null);
      Font boldFont = new Font("MonoSpaced", Font.BOLD, 30);
      Font typeFont = new Font("MonoSpaced", Font.PLAIN, 30);
      g.setColor(RED);
      g.setFont(boldFont);
      g.drawString("Health: Terminated", 100, 210);

      g.setColor(Color.WHITE);
      g.setFont(typeFont);
      g.drawString("NOOOOOOOOOOOOOOOOOOOOOO! Your glorious top hat!", 30, 50);
      g.drawString("The syndicate really defeated you, huh? A shame, ", 30, 80);
      g.drawString("as I just lost 20 grand betting you would beat", 30, 110);
      g.drawString("Stage " + (bossIndex + 1) + ". This is bad...", 30, 140);
      g.drawString("No matter. I'll just have to... don't worry about it.", 30, 170);

      g.setFont(new Font("MonoSpace", Font.BOLD, 50));
      g.drawString("Stages Beaten: " + (bossIndex), 500, 290);

      g.setFont(new Font("MonoSpace", Font.PLAIN, 30));
      g.drawString("Type username below (max 16", 500, 340);
      g.drawString("characters). Press ENTER to", 500, 370);
      g.drawString("enter it into the leaderboard.", 500, 400);
      g.drawString("Be school-appropriate please.", 500, 430);
      cursor.setX(g.getFontMetrics().stringWidth(yourType) + 500);
      cursor.setY(490);
      if (cursorOn) {
        g.drawLine(cursor.getX(), cursor.getY() - 30, cursor.getX(), cursor.getY());
      }
      if (cursorCount == 0)
        cursorOn = !cursorOn;
      g.drawString(yourType, 500, 490);
    } else if (screen == 3 && !leaderboardCompiled) {
      try (BufferedReader br = new BufferedReader(new FileReader("leaderboard.txt"))) {

        String line = br.readLine();
        while (line != null) {
          if (line.substring(0, 4).equals("Easy")) {
            ldEasy.add(line.substring(4));
          } else if (line.substring(0, 4).equals("Medi")) {
            ldMedi.add(line.substring(4));
          } else if (line.substring(0, 4).equals("Hard")) {
            ldHard.add(line.substring(4));
          }
          line = br.readLine();
        }
        leaderboardCompiled = true;
        br.close();
      } catch (IOException e) {
      }
    } else if (screen == 3) {
      Font boldFont = new Font("MonoSpaced", Font.BOLD, 30);
      Font typeFont = new Font("MonoSpaced", Font.PLAIN, 30);
      g.setColor(Color.WHITE);
      g.setFont(boldFont);
      g.drawString("EASY Leaderboard", 30, 50);
      g.setFont(typeFont);

      for (int i = 1; i <= 5; i++) {
        int max = -1;
        int index = 0;
        for (int j = 0; j < ldEasy.size(); j++) {
          if (Integer.parseInt(ldEasy.get(j).substring(0, 2)) > max) {
            index = j;
            max = Integer.parseInt(ldEasy.get(j).substring(0, 2));
          }
        }
        if (index < ldEasy.size()) {
          g.drawString(ldEasy.get(index).substring(2), 30, 50 + 30 * i);
          ldEasy.remove(index);
        }
      }

      g.setFont(boldFont);
      g.drawString("MEDIUM Leaderboard", 30, 260);
      g.setFont(typeFont);

      for (int i = 1; i <= 5; i++) {
        int max = -1;
        int index = 0;
        for (int j = 0; j < ldMedi.size(); j++) {
          if (Integer.parseInt(ldMedi.get(j).substring(0, 2)) > max) {
            index = j;
            max = Integer.parseInt(ldMedi.get(j).substring(0, 2));
          }
        }
        if (index < ldMedi.size()) {
          g.drawString(ldMedi.get(index).substring(2), 30, 260 + 30 * i);
          ldMedi.remove(index);
        }
      }

      g.setFont(boldFont);
      g.drawString("HARD Leaderboard", 30, 470);
      g.setFont(typeFont);

      for (int i = 1; i <= 5; i++) {
        int max = -1;
        int index = 0;
        for (int j = 0; j < ldHard.size(); j++) {
          if (Integer.parseInt(ldHard.get(j).substring(0, 2)) > max) {
            index = j;
            max = Integer.parseInt(ldHard.get(j).substring(0, 2));
          }
        }
        if (index < ldHard.size()) {
          g.drawString(ldHard.get(index).substring(2), 30, 470 + 30 * i);
          ldHard.remove(index);
        }
      }

      g.setColor(RED);
      g.setFont(boldFont);
      g.drawString("Press ENTER to restart", 30, 680);
      leaderboardDone = true;
    }
  }

  // need for keyboard input
  public void keyPressed(KeyEvent e) {
    // int key = e.getKeyCode(); // indicates which key was pressed
    // System.out.println("key pressed: " + key); // shows you key code values for
    // other keys
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
    if (screen == 0) {
      int key = e.getKeyChar();
      if (key == 69 || key == 101) // easy
      {
        continuousDeltaHealth = 1;
        changeInCont = 1;
        deltaHealth = 3;
        changeInDE = 2;
        gameDifficulty = "Easy";
        screen = 1;
      } else if (key == 77 || key == 109) // medium
      {
        continuousDeltaHealth = 2;
        changeInCont = 2;
        deltaHealth = 5;
        changeInDE = 3;
        gameDifficulty = "Medi";
        screen = 1;
      } else if (key == 72 || key == 104) // hard
      {
        continuousDeltaHealth = 3;
        changeInCont = 3;
        deltaHealth = 10;
        changeInDE = 4;
        gameDifficulty = "Hard";
        screen = 1;
      }
    } else if (screen == 1) {
      if (!finishedToType && (!(yourType.length() == toType.length()) || (e.getKeyChar() == 8))) {
        int key = e.getKeyChar(); // indicates which key was pressed
        // System.out.println("key typed: " + key);

        int index = yourType.length();

        // System.out.println(key);

        if (toType.substring(0, index).equals(yourType)) {
          inTheWrong = false;
        } else {
          inTheWrong = true;
        }

        if ((inTheWrong) || ((char) key) != (toType.charAt(index))) {
          if (key != 8)
            yourHealth -= deltaHealth;
          yourHealthColor = RED;
        } else {
          yourHealthColor = Color.WHITE;
        }

        if (key == 8) {
          if (yourType.length() > 0)
            yourType = yourType.substring(0, yourType.length() - 1);
          if (yourTypeGreen.length() > 0 && yourTypeGreen.length() == yourTypeRed.length())
            yourTypeGreen = yourTypeGreen.substring(0, yourTypeGreen.length() - 1);
          if (yourTypeRed.length() > 0)
            yourTypeRed = yourTypeRed.substring(0, yourTypeRed.length() - 1);
        } else {
          yourType += Character.toString((char) key);

          if (!inTheWrong && ((char) key) == (toType.charAt(index))) {
            yourTypeGreen += toType.substring(index, index + 1);
            yourTypeRed += toType.substring(index, index + 1);
            if (continuousDeltaHealth >= 5)
              yourHealth += continuousDeltaHealth / 5;
            else
              yourHealth += 1;
          } else {
            yourTypeRed += toType.substring(index, index + 1);
          }
        }

        if (yourType.equals(toType)) {
          finishedToType = true;
          timeTaken = System.currentTimeMillis() - timeTaken;
        }
      } else if (e.getKeyChar() == 10) {
        finishedToType = false;
        try {
          toType = randomSentence();
        } catch (IOException e1) {
        }
        yourType = "";
        yourTypeGreen = "";
        yourTypeRed = "";
        bossHealthColor = Color.WHITE;
        yourHealthColor = Color.WHITE;
        alreadyReduced = false;
        continuousDeltaHealth += changeInCont;
        deltaHealth += changeInDE;
        if (bosses.get(bossIndex).getHealth() <= 0)
          bossIndex++;
      }
    } else if (screen == 2) {
      int key = e.getKeyChar();
      if (key == 10) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("leaderboard.txt", true))) {
          if (bossIndex < 10)
            pw.println(gameDifficulty + "" + 0 + "" + bossIndex + "Player: " + yourType + "\t Score: " + bossIndex);
          else
            pw.println(gameDifficulty + "" + bossIndex + "Player: " + yourType + "\t Score: " + bossIndex);
          pw.close();
        } catch (IOException e1) {
        }

        screen = 3;
      } else if (key == 8) {
        if (yourType.length() > 0) {
          yourType = yourType.substring(0, yourType.length() - 1);
        }
      } else if (yourType.length() <= 15) {
        yourType += (char) key;
      }
    } else if (screen == 3) {

      int key = e.getKeyChar();
      if (key == 10) {
        screen = 0;

        ldEasy = new ArrayList<>();
        ldMedi = new ArrayList<>();
        ldHard = new ArrayList<>();

        leaderboardCompiled = false;
        leaderboardDone = false;

        alreadyReduced = false;

        bosses = new ArrayList<>();
        bossImage = new ArrayList<ArrayList<Image>>();

        yourHealth = 500;
        heroX = 0;
        heroY = 200;
        inTheWrong = false;
        try {
          toType = randomSentence();
        } catch (IOException e1) {
        }
        cursorOn = true;
        cursorCount = 0;
        lineNumber = 1;

        finishedToType = false;

        bossHealthColor = Color.WHITE;
        yourHealthColor = Color.WHITE;

        timeTaken = System.currentTimeMillis();

        bossImage.add(new ArrayList<Image>());
        int random = (int) (Math.random() * 5 + 1);
        bossImage.get(0).add(new ImageIcon(getClass().getResource("Villain" + random + "-1.png")).getImage());
        bosses.add(new Sprite(villainLocations[random - 1], true, bossImage.get(0),
            bossIndex * 100 + 50 + (int) (Math.random() * 100), 0,
            villainNames[(int) (Math.random() * villainNames.length)]));

        yourType = "";
        lineOfYourType = "";
        yourTypeGreen = "";
        yourTypeRed = "";

        bossIndex = 0;
      }
    }
  }

  // need for mouse input
  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  // need for automation (graphical changes not prompted by the keyboard or mouse)
  public void run() {
    while (true) {
      if (!leaderboardDone) {
        addBoss();
        repaint();
      }
      try {
        if (cursorCount < 5) {
          cursorCount++;

        } else {
          cursorCount = 0;
          if (!bossHealthColor.equals(RED) && screen == 1)
            yourHealth -= continuousDeltaHealth;
        }
        Thread.sleep(100);
      } catch (Exception e) {
      } // give Java 100ms to run paintComponent
    }
  }
}