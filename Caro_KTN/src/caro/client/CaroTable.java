/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package caro.client;

import caro.common.State;
import caro.common.Coordinate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class CaroTable extends JPanel {

    static final int TOP = 10;
    static final int LEFT = 10;
    static final int BOARDSIZE = 25;
    static final int PIECESIZE = 20;
    static final int MAXPIECENUM = BOARDSIZE * BOARDSIZE;
    static final int GC_OK = 0;
    static final int GC_ILLEGAL = 1;
    static final int GC_CANNOT = 2;
    static final int GC_FILLED = 3;
    static final int GC_WIN = 4;
    public State stateses[][];
    public int Area[][];
    static final int AREASIZE = 2;
    public int numPiece;
    Graphics bufferGraphics;
    Image offscreen;
    Dimension dim;

    Image blackImage;
    Image whiteImage;
    
    public CaroTable() {

        resize(310, 310);

        stateses = new State[BOARDSIZE][BOARDSIZE];

        for (int i = 0; i < BOARDSIZE; i++) {
            for (int j = 0; j < BOARDSIZE; j++) {
                stateses[i][j] = new State();
            }
        }
        Area = new int[BOARDSIZE][BOARDSIZE];

        try {
            //File file = new File("image/cross.png");
            //blackImage = ImageIO.read(new File("image/cross.png"));
            //whiteImage = ImageIO.read(new File("image/circle.png"));
            
            URL url = this.getClass().getResource("/image/cross.png");
            
            blackImage = ImageIO.read(url);
            url = this.getClass().getResource("/image/circle.png");
            whiteImage = ImageIO.read(url);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    //    public void initialize(CComputer com) {

    public void initialize() {
        //Computer = com;

        for (int x = 0; x < BOARDSIZE; x++) {
            for (int y = 0; y < BOARDSIZE; y++) {
                stateses[x][y].State = State.EMPTY;
                Area[x][y] = 0;
            }
        }
        numPiece = 0;
    }

    public void init(int width, int height) {
        //dim = getSize();
        offscreen = createImage(width, height);
        bufferGraphics = offscreen.getGraphics();
    }

    public void put(int color, int x, int y) {

        if (stateses[x][y].State == State.EMPTY) {
            stateses[x][y].State = color;
            numPiece++;

            int x1, x2, y1, y2;
            x1 = (x - AREASIZE < 0) ? 0 : x - AREASIZE;
            x2 = (x + AREASIZE >= BOARDSIZE) ? BOARDSIZE - 1 : x + AREASIZE;
            y1 = (y - AREASIZE < 0) ? 0 : y - AREASIZE;
            y2 = (y + AREASIZE >= BOARDSIZE) ? BOARDSIZE - 1 : y + AREASIZE;
            for (; x1 <= x2; x1++) {
                for (y = y1; y <= y2; y++) {
                    Area[x1][y]++;
                }
            }

        }

    }

    public boolean getPosition(int x, int y, Coordinate pos) {

        if (x < LEFT - (PIECESIZE / 2) || x > LEFT + (PIECESIZE * (BOARDSIZE - 1)) + (PIECESIZE / 2)) {
            return false;
        }
        if (y < TOP - (PIECESIZE / 2) || y > TOP + (PIECESIZE * (BOARDSIZE - 1)) + (PIECESIZE / 2)) {
            return false;
        }
        pos.x = (x - (LEFT - (PIECESIZE / 2))) / PIECESIZE;
        pos.y = (y - (TOP - (PIECESIZE / 2))) / PIECESIZE;
        return true;
    }

    public void draw() {

        this.repaint();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x, y;

        bufferGraphics.setColor(Color.white);
        bufferGraphics.clearRect(0, 0, offscreen.getWidth(this), offscreen.getHeight(this));
        bufferGraphics.setColor(Color.black);
        for (x = 0; x < BOARDSIZE; x++) {
            bufferGraphics.drawLine(x * PIECESIZE + LEFT, TOP, x * PIECESIZE + LEFT, TOP + (BOARDSIZE - 1) * PIECESIZE);
        }
        for (y = 0; y < BOARDSIZE; y++) {
            bufferGraphics.drawLine(LEFT, y * PIECESIZE + TOP, LEFT + (BOARDSIZE - 1) * PIECESIZE, y * PIECESIZE + TOP);
        }


        if (blackImage == null || whiteImage==null)
        {
            for(x = 0; x < BOARDSIZE; x++) {
                for(y = 0; y < BOARDSIZE; y++) {
                    switch(stateses[x][y].State) {
                        case State.BLACK:
                            bufferGraphics.setColor(Color.red);

                            bufferGraphics.drawLine(x*PIECESIZE+2, y*PIECESIZE+2, (x+1)*PIECESIZE-2, (y+1)*PIECESIZE-2);
                            bufferGraphics.drawLine(x*PIECESIZE+2, (y+1)*PIECESIZE-2, (x+1)*PIECESIZE-2, y*PIECESIZE+2);

                            break;
                        case State.WHITE:
                            bufferGraphics.setColor(Color.blue);
                            bufferGraphics.drawOval(x*PIECESIZE,y*PIECESIZE,PIECESIZE-2,PIECESIZE-2);
                            break;
                    }
                }
            }
        }
        else
        {
            for(x = 0; x < BOARDSIZE; x++) {
                for(y = 0; y < BOARDSIZE; y++) {
                    switch(stateses[x][y].State) {
                        case State.WHITE:
                            bufferGraphics.drawImage(whiteImage, x*PIECESIZE, y*PIECESIZE, this);

                            //bufferGraphics.drawLine(x*PIECESIZE+2, y*PIECESIZE+2, (x+1)*PIECESIZE-2, (y+1)*PIECESIZE-2);
                            //bufferGraphics.drawLine(x*PIECESIZE+2, (y+1)*PIECESIZE-2, (x+1)*PIECESIZE-2, y*PIECESIZE+2);

                            break;
                        case State.BLACK:
                            bufferGraphics.drawImage(blackImage, x*PIECESIZE, y*PIECESIZE, this);
                            //bufferGraphics.setColor(Color.blue);
                            //bufferGraphics.drawOval(x*PIECESIZE,y*PIECESIZE,PIECESIZE-2,PIECESIZE-2);
                            break;
                    }
                }
            }
        
        }

        g.drawImage(offscreen, 0, 0, this);
    }
}
