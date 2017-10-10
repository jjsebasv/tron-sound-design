//
// TRON game
//
// (C)2000
// Brian Postma
// b.postma@hetnet.nl
//

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

public class Tron extends Applet implements Runnable
{
  Dimension	d;
  Font 		largefont = new Font("Helvetica", Font.BOLD, 24);
  Font		smallfont = new Font("Helvetica", Font.BOLD, 14);

  FontMetrics	fmsmall, fmlarge;  
  Graphics	goff;
  Image		ii;
  Thread	thethread;

    private final String SOUNDS_PATH = "sounds/";
    private final String SOUND_EXT = ".wav";

    private boolean isIntroPlaying = false;
    private Clip clip = AudioSystem.getClip();
    private boolean heartbeat = false;

    private int INVERSE_VEL = 100;

  boolean[][]	matrix;
  boolean	oneplayer=false;
  boolean	ingame=false;

  int		player1score;
  int		player2score;
  int		xsize;
  int		ysize;

  int		p1pos[] = { 0,0,0,0 };
  int		p2pos[] = { 0,0,0,0 };
  final int	blocksize=10;
  final int     screendelay=35;

  int		count;
  boolean	showtitle=false;
  Color		p1color;
  Color		p2color;
  private int musicLevel = 0;

  public Tron() throws LineUnavailableException {
    }

    public String getAppletInfo()
  {
    return("TRON classic game");
  }

  public void init()
  {
    Graphics g;
    int i;
    d = size();
    g=getGraphics();
    g.setFont(smallfont);
    fmsmall = g.getFontMetrics();
    g.setFont(largefont);
    fmlarge = g.getFontMetrics();

    p1color=new Color(255,192,128);
    p2color=new Color(192,255,128);

    xsize=d.width/blocksize;
    ysize=(d.height-16)/blocksize;

    matrix=new boolean[xsize][ysize];
    player1score=0;
    player2score=0;
    GameInit();
  }

  public void GameInit()
  { 
    int i,j;

    for (i=0; i<xsize; i++)
      for (j=0; j<ysize; j++)
        matrix[i][j]=false;
    for (i=0; i<xsize; i++)
    {
        matrix[i][0]=true;
        matrix[i][ysize-1]=true;
    }
    for (i=0; i<ysize; i++)
    {
        matrix[0][i]=true;
        matrix[xsize-1][i]=true;
    }
 
    p1pos[2]=0;
    p1pos[3]=-1;
    p2pos[2]=0;
    p2pos[3]=1;
    p1pos[0]=xsize/4;
    p1pos[1]=ysize/2;
    p2pos[0]=xsize/4+xsize/2;
    p2pos[1]=p1pos[1];
  }

  public void DrawBlock(int x, int y)
  {
    goff.fillRect(x*blocksize,y*blocksize,blocksize,blocksize);
  }

  public boolean keyDown(Event e, int key)
  {
    if (ingame)
    {
        if (key == 'w' || key == 'W') // UP
        {
            p1pos[2] = 0;
            p1pos[3] = -1;
          playSound("car", false);
        } else if (key == 's' || key == 'S') // DOWN
        {
            p1pos[2] = 0;
            p1pos[3] = 1;
          playSound("car", false);
        } else if (key == 'a' || key == 'A') // LEFT
        {
            p1pos[2] = -1;
            p1pos[3] = 0;
          playSound("car", false);
        } else if (key == 'd' || key == 'D') // RIGHT
        {
            p1pos[2] = 1;
            p1pos[3] = 0;
          playSound("car", false);
        }

      if(!oneplayer) {
         if (key == Event.UP) // UP
        {
          p2pos[2]=0;
          p2pos[3]=-1;
          playSound("car", false);
        }
        else if (key == Event.DOWN) // DOWN
        {
          p2pos[2]=0;
          p2pos[3]=1;
          playSound("car", false);
        }
        else if (key == Event.LEFT) // LEFT
        {
          p2pos[2]=-1;
          p2pos[3]=0;
          playSound("car", false);
        }
        else if (key == Event.RIGHT) // RIGHT
        {
          p2pos[2]=1;
          p2pos[3]=0;
          playSound("car", false);
        }

      }
      if (key == Event.ESCAPE) {
        gameOver();
      }

    }
    else
    {
      if (key == '1')
      {
          playSoundOver("1player-game");
          oneplayer=true;
        GameInit();
        ingame=true;
        player1score=0;
        player2score=0;
          playSound("game-back", true);
      } else if (key == '2') {
          playSoundOver("2player-game");
        oneplayer=false;
        GameInit();
        ingame=true;
        player1score=0;
        player2score=0;
          playSound("game-back", true);
      } else {
          playSound("error", false);
      }
    }
    return true;
  }

  public boolean keyUp(Event e, int key)
  {
    if (key == '1' || key == '3')
       ;
    return true;
  }

  public void paint(Graphics g, Color c)
  {
    String s;
    Graphics gg;

    if (goff==null && d.width>0 && d.height>0)
    {
      ii = createImage(d.width, d.height);
      goff = ii.getGraphics();
    }
    if (goff==null || ii==null)
      return;

    goff.setColor(c);
    goff.fillRect(0, 0, d.width, d.height);

    DrawScreen();
    if (ingame)
      PlayGame();
    else
    {
      ShowIntroScreen();
      goff.setColor(p1color);
      AutoPlay(p1pos);
      goff.setColor(p2color);
      AutoPlay(p2pos);
      CheckPlayers();
    }
    g.drawImage(ii, 0, 0, this);
  }


  public void DrawScreen()
  {
    int i,j;

    goff.setColor(Color.white);
    for (i=0; i<xsize; i++)
      for (j=0; j<ysize; j++)
        if (matrix[i][j])
          DrawBlock(i,j);
  }

  public void AutoPlay(int[] pos)
  {
    int optdx[]= {0,0,0,0};
    int optdy[]= {0,0,0,0};
    int options=0;
    int i;
    int x=pos[0];
    int y=pos[1];
    int dx=pos[2];
    int dy=pos[3];
    if (matrix[x+dx][y+dy]==true)
    {
      if (matrix[x-1][y]==false)
      {
        optdx[options]=-1;
        optdy[options]=0;
        options++;
      }          
      if (matrix[x+1][y]==false)
      {
        optdx[options]=1;
        optdy[options]=0;
        options++;
      } 
      if (matrix[x][y-1]==false)
      {
        optdx[options]=0;
        optdy[options]=-1;
        options++;
      } 
      if (matrix[x][y+1]==false)
      {
        optdx[options]=0;
        optdy[options]=1;
        options++;
      }
      if (options!=0)
      {
        i=(int)(Math.random()*(float)(options-1)+0.490);
        dx=optdx[i];
        dy=optdy[i];
      }
    }
    pos[0]=x+dx;
    pos[1]=y+dy;
    pos[2]=dx;
    pos[3]=dy;
    DrawBlock(x,y);
  }


  public void CheckPlayers()
  {
    if (p1pos[0]==p2pos[0] && p1pos[1]==p2pos[1])
    {
      // both players hit the same spot simultaneously
      if (ingame)
      {
        player1score++;
        player2score++;
      }
      GameInit();
    }
    else
    {
        // points for player 2
      if (matrix[p1pos[0]][p1pos[1]])
      {
          if (ingame) {
              if (oneplayer) {
                  playSound("p1loses", false);
              } else {
                playSound("anyloses", false);
              }

            player2score++;
          }

          GameInit();
      }
        // points for player 1
      if (matrix[p2pos[0]][p2pos[1]])
      {
        if (ingame) {
          if (oneplayer) {
            playSoundOver("p1wins");
          } else {
            playSound("anyloses", false);
          }
          player1score++;
        }
        GameInit();
      }
    }
    if (player1score==5 || player2score==5) {
      gameOver();;
    }
    matrix[p1pos[0]][p1pos[1]]=true;
    matrix[p2pos[0]][p2pos[1]]=true;
  }

  private void gameOver() {
    player1score = 0;
    player2score = 0;
    playSoundOver("game-over");
    playSound("game-intro", true);
    this.heartbeat = false;
    ingame=false;
  }

  public void PlayGame()
  {
    p1pos[0]+=p1pos[2];
    p1pos[1]+=p1pos[3];
    goff.setColor(p1color);
    DrawBlock(p1pos[0],p1pos[1]);

    goff.setColor(p2color);
    if (oneplayer)
    {
      AutoPlay(p2pos);
    }
    else
    {
      p2pos[0]+=p2pos[2];
      p2pos[1]+=p2pos[3];
      DrawBlock(p2pos[0],p2pos[1]);
     }
    CheckPlayers();
    ShowScore();
  }


  public void ShowIntroScreen()
  {
    String s;
    ShowScore();
      playIntroMusic();

    goff.setFont(largefont);
    goff.setColor(new Color(96,128,255));

    if (showtitle)
    {
      s="** TRON **";
      goff.drawString(s,(d.width-fmlarge.stringWidth(s)) / 2, d.height/2 - 30);
      goff.setFont(smallfont);
      s="An ancient arcade game";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,d.height/2);
      goff.setColor(new Color(255,160,64));
    }
    else
    {
      goff.setFont(smallfont);
      goff.setColor(new Color(96,128,255));
      s="'1' for 1 player game, '2' for 2 players";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,d.height/2 - 20);
      goff.setColor(new Color(255,160,64));
      s="Player 1 use 'w', 's', 'a' and 'd'";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,d.height/2 + 10);
      s="Player 2 use cursor keys";
      goff.drawString(s,(d.width-fmsmall.stringWidth(s))/2,d.height/2 + 30);
    }
    count--;
    if (count<=0)
    { count=screendelay; showtitle=!showtitle; }
  }


  public void ShowScore()
  {
    String s;
    goff.setFont(smallfont);
    goff.setColor(Color.white);

    s="Player 1: "+player1score;
    goff.drawString(s,blocksize,d.height-5);
    s="Player 2: "+player2score;
    goff.drawString(s,d.width-blocksize-fmsmall.stringWidth("Player 2: 0"),d.height-5);
  }


  public void run()
  {
    long  starttime;
    Graphics g;

    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    g=getGraphics();

    while(true)
    {
      starttime=System.currentTimeMillis();
      try
      {
        if (oneplayer) {
          int i = 2*player2score + 1;
          paint(g, new Color(16 * i,24,64));
          starttime += INVERSE_VEL - 10*player1score;
          if (player2score == 4) {
            playHeartbeat();
          }
          if (player1score == 4) {
            changeBackgroundMusic(player2score);
          }
        } else {
          paint(g, new Color(16,24,64));
          starttime += INVERSE_VEL;
        }

        Thread.sleep(Math.max(0, starttime-System.currentTimeMillis()));
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
  }

  public void start()
  {
    if (thethread == null) {
      thethread = new Thread(this);
      thethread.start();
    }
  }

  public void stop()
  {
    if (thethread != null) {
      thethread.stop();
      thethread = null;
    }
  }

    private void playSound(String soundName, boolean isBackground) {
        soundName = soundName == null ? "sound" : soundName;
      Clip toUseClip = null;
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(SOUNDS_PATH + soundName + SOUND_EXT));

            if(isBackground) {
                toUseClip = this.clip;
                if (toUseClip.isOpen())
                    toUseClip.close();
                toUseClip.open(audioIn);
                toUseClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                toUseClip = AudioSystem.getClip();
                toUseClip.open(audioIn);
            }
            toUseClip.start();
        } catch (Exception err) {
            err.printStackTrace();
            if (toUseClip != null)
              toUseClip.close();
            JOptionPane.showMessageDialog(null, "SoundPlayer: "+err,null,0);
        }
    }

    private void playSoundOver(String soundName) {
      soundName = soundName == null ? "sound" : soundName;
      try {
        InputStream in = getClass().getResourceAsStream(SOUNDS_PATH + soundName + SOUND_EXT);
        AudioStream as = new AudioStream(in);
        AudioPlayer.player.start(as);
      } catch (Exception err) {
        err.printStackTrace();
        JOptionPane.showMessageDialog(null, "SoundPlayer: "+err,null,0);
      }
    }

    private void playIntroMusic() {

        if(!isIntroPlaying) {
            isIntroPlaying = true;
            playSound("game-intro", true);
        }
    }

    private void playMassiveBackground() {
      isIntroPlaying = false;
      if(!isIntroPlaying) {
        isIntroPlaying = true;
        playSound("massive-background", true);
      }
    }

    private void changeBackgroundMusic(int musicLevel) {
      if (this.musicLevel != musicLevel) {
        this.musicLevel = musicLevel;
        playSound("massive-background", true);
      }
    }

    private void playHeartbeat() {
      if (!this.heartbeat) {
        this.heartbeat = true;
        try {
          AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(SOUNDS_PATH + "heartbeat2" + SOUND_EXT));

          if (this.clip.isOpen())
            this.clip.close();

          this.clip.open(audioIn);
          this.clip.loop(Clip.LOOP_CONTINUOUSLY);
          this.clip.start();

        } catch (Exception err) {
          err.printStackTrace();
          if (this.clip != null)
            this.clip.close();
          JOptionPane.showMessageDialog(null, "SoundPlayer: "+err,null,0);
        }
      }
    }
    
}
