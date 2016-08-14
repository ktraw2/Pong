import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Kevin on 8/9/2016.
 */
public class PongCanvas extends Canvas implements Runnable, KeyListener {
    private final boolean DEBUG = true;

    private final int BOX_HEIGHT = 100;
    private final int ELEMENT_WIDTH = 25;
    private final int BOX_SPACE_FROM_WALL = 10;
    private final int MOVE_OFFSET = 10;
    private final int FRAME_DELAY = 50;
    private final int ABS_DEFAULT_DELTA_Y_FULL = 6;

    private Thread runThread;
    private int leftBoxY = 0;
    private int rightBoxY = 0;
    private int deltaVariation = 1;
    private Point ball;
    private Point deltaBall;
    private Set<Integer> keysPressed = new HashSet<Integer>();
    private boolean twoPlayer = true;
    private boolean moveBall = true;
    private boolean leftMoving = false;
    private boolean rightMoving = false;
    private boolean[] noCollide = {false, false, false, false, false, false};
    private int p1Score = 0;
    private int p2score = 0;

    public void resetGame()
    {
        Dimension d = this.getSize();
        leftBoxY = (this.getSize().height / 2) - (BOX_HEIGHT / 2);
        rightBoxY = leftBoxY;
        ball = new Point((d.width / 2) - (ELEMENT_WIDTH / 2), (d.height / 2) - (ELEMENT_WIDTH / 2) - 100);
        deltaBall = new Point(-12, 6);
        for (int i = 0; i < CollisionType.TOTAL; i++)
            noCollide[i] = false;
    }

    /**
     * to avoid a bug, speed must be varied, tack this on to any movement of the ball98 to vary it and avoid the bug
     * @return the variation in speed
     */
    public int getDeltaVariation()
    {
        int newVariation = deltaVariation;
        if(deltaVariation == 1)
            deltaVariation = 0;
        else
            deltaVariation = 1;
        return newVariation;
    }

    /**
     * determine the Y position of the ball relative to the paddle it collided with
     * @param boxY send in leftBoxY or rightBoxY depending on the collision
     * @return a new delta Y for the ball
     */
    public int getNewDeltaY(int boxY)
    {
        int middleY = ball.y + (ELEMENT_WIDTH / 2);
        if (middleY < boxY + (BOX_HEIGHT / 2)) //is in top half
        {
            if(DEBUG)
                System.out.println("TOP HALF");
            if (middleY < boxY + (BOX_HEIGHT / 3)) //is in top third
                return -ABS_DEFAULT_DELTA_Y_FULL;
            else //is in top half of middle third
                return -(ABS_DEFAULT_DELTA_Y_FULL / 2);
        }
        else if (middleY > boxY + (BOX_HEIGHT / 2)) //is in bottom half
        {
            if(DEBUG)
                System.out.println("BOTTOM HALF");
            if (middleY > boxY + (BOX_HEIGHT - (BOX_HEIGHT / 3))) //is in bottom third
                return ABS_DEFAULT_DELTA_Y_FULL;
            else //is in bottom half of middle third
                return ABS_DEFAULT_DELTA_Y_FULL / 2;
        }
        else //is in exact middle
            return 0;
    }

    public void update(Graphics g)
    {
        //set up double buffering
        Graphics doubleBufferGraphics;
        BufferedImage doubleBuffer;
        Dimension d = this.getSize();
        doubleBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        doubleBufferGraphics = doubleBuffer.getGraphics();
        doubleBufferGraphics.setColor(this.getBackground());
        doubleBufferGraphics.fillRect(0, 0, d.width, d.height);
        doubleBufferGraphics.setColor(this.getForeground());
        paint(doubleBufferGraphics);

        //flip
        g.drawImage(doubleBuffer, 0, 0, this);
    }

    public void paint(Graphics g)
    {
        Dimension d = this.getSize();
        if (runThread == null) //if runThread = null, then there is no active thread, so init + start game loop
        {
            this.addKeyListener(this);
            resetGame();
            runThread = new Thread(this);
            runThread.start();
        }

        g.fillRect(BOX_SPACE_FROM_WALL, leftBoxY, ELEMENT_WIDTH, BOX_HEIGHT); //draw left paddle
        g.fillRect(d.width - ELEMENT_WIDTH - BOX_SPACE_FROM_WALL, rightBoxY, ELEMENT_WIDTH, BOX_HEIGHT); //draw right paddle
        g.drawLine(d.width / 2, 0, d.width / 2, d.height); //draw dividing line
        g.fillOval(ball.x, ball.y, ELEMENT_WIDTH - 5, ELEMENT_WIDTH - 5); //draw pong ball
        int p1TextX = d.width / 4;
        int p2TextX = (d.width /2) + (d.width / 4);
        g.drawString("" + p1Score, p1TextX, 10); //player 1 score
        g.drawString("" + p2score, p2TextX, 10); //player 2 score
        String p1KeyPrompt = "W = Up; S = Down";
        String p2KeyPrompt = "Up Arrow = Up; Down Arrow = Down";
        g.drawString(p1KeyPrompt, p1TextX - (g.getFontMetrics().stringWidth(p1KeyPrompt) / 2), 25); //player 1 key prompt
        if (twoPlayer)
            g.drawString(p2KeyPrompt, p2TextX - (g.getFontMetrics().stringWidth(p2KeyPrompt) / 2), 25); //player 2 key prompt, only draw if 2 player game
    }

    @Override
    public void run()
    {
        while(true)
        {
            Dimension d = this.getSize();
            for (int k : keysPressed) //move paddles if keys are pressed
            {
                if (DEBUG)
                    System.out.println(k);
                switch (k)
                {
                    case KeyEvent.VK_W:
                        if (!(leftBoxY - MOVE_OFFSET <= 0))
                        {
                            leftMoving = true;
                            leftBoxY -= MOVE_OFFSET;
                        }
                        else
                            leftBoxY = 0;
                        break;
                    case KeyEvent.VK_S:
                        if (!(leftBoxY + BOX_HEIGHT + MOVE_OFFSET >= d.height))
                        {
                            leftMoving = true;
                            leftBoxY += MOVE_OFFSET;
                        }
                        else
                            leftBoxY = d.height - BOX_HEIGHT;
                        break;
                    case KeyEvent.VK_UP:
                        if (!(rightBoxY - MOVE_OFFSET <= 0) && twoPlayer)
                        {
                            rightMoving = true;
                            rightBoxY -= MOVE_OFFSET;
                        }
                        else
                            rightBoxY = 0;
                        break;
                    case KeyEvent.VK_DOWN:
                        if (!(rightBoxY + BOX_HEIGHT + MOVE_OFFSET >= d.height) && twoPlayer)
                        {
                            rightMoving = true;
                            rightBoxY += MOVE_OFFSET;
                        }
                        else
                            rightBoxY = d.height - BOX_HEIGHT;
                        break;
                }
            }

            //move ball
            if (moveBall)
            {
                int newX = deltaBall.x;
                int newY = deltaBall.y;
                if (DEBUG)
                {
                    System.out.println("x: " + newX);
                    System.out.println("y: " + newY);
                }
                ball.x += newX;
                ball.y += newY;
            }

            //check collisions
            if (ball.x <= ELEMENT_WIDTH + BOX_SPACE_FROM_WALL && ball.y + ELEMENT_WIDTH >= leftBoxY && ball.y <= leftBoxY + BOX_HEIGHT && noCollide[CollisionType.LEFT_PADDLE] == false) //check collision with left box
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Left Box");
                deltaBall.x = -deltaBall.x + getDeltaVariation();
                deltaBall.y = getNewDeltaY(leftBoxY) + getDeltaVariation();
                System.out.println("new X: " + deltaBall.x + " new Y: " + deltaBall.y);
                //make sure the ball doesn't get stuck by disallowinga double collision, but also reallow all other collisions
                noCollide[CollisionType.LEFT_PADDLE] = true;
                noCollide[CollisionType.RIGHT_PADDLE] = false;
                noCollide[CollisionType.TOP] = false;
                noCollide[CollisionType.BOTTOM] = false;
            }
            else if (ball.x + ELEMENT_WIDTH >= d.width - ELEMENT_WIDTH - BOX_SPACE_FROM_WALL && ball.y + ELEMENT_WIDTH >= rightBoxY && ball.y <= rightBoxY + BOX_HEIGHT && noCollide[CollisionType.RIGHT_PADDLE] == false) //check collision with right box
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Right Box");
                deltaBall.x = -deltaBall.x + getDeltaVariation();
                deltaBall.y = getNewDeltaY(rightBoxY) + getDeltaVariation();
                System.out.println("new X: " + deltaBall.x + " new Y: " + deltaBall.y);
                //make sure the ball doesn't get stuck by disallowinga double collision, but also reallow all other collisions
                noCollide[CollisionType.LEFT_PADDLE] = false;
                noCollide[CollisionType.RIGHT_PADDLE] = true;
                noCollide[CollisionType.TOP] = false;
                noCollide[CollisionType.BOTTOM] = false;
            }
            else if (ball.x <= 0) //player 1 loses
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Left Wall");
                p2score++;
                resetGame();
            }
            else if (ball.x + ELEMENT_WIDTH >= d.width) //player 2 loses
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Right Wall");
                p1Score++;
                resetGame();
            }
            else if (ball.y <= 0 && noCollide[CollisionType.TOP] == false) //check colisions with top
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Top Or Bottom");
                deltaBall.y = -deltaBall.y + getDeltaVariation();
                //make sure the ball doesn't get stuck by disallowinga double collision, but also reallow all other collisions
                noCollide[CollisionType.LEFT_PADDLE] = false;
                noCollide[CollisionType.RIGHT_PADDLE] = false;
                noCollide[CollisionType.TOP] = true;
                noCollide[CollisionType.BOTTOM] = false;
            }
            else if (ball.y + ELEMENT_WIDTH >= d.height && noCollide[CollisionType.BOTTOM] == false) //check collision with bottom
            {
                if (DEBUG)
                    System.out.println("Ball Collide With Top Or Bottom");
                deltaBall.y = -deltaBall.y + getDeltaVariation();
                //make sure the ball doesn't get stuck by disallowinga double collision, but also reallow all other collisions
                noCollide[CollisionType.LEFT_PADDLE] = false;
                noCollide[CollisionType.RIGHT_PADDLE] = false;
                noCollide[CollisionType.TOP] = false;
                noCollide[CollisionType.BOTTOM] = true;
            }



            repaint();
            try
            {
                Thread.currentThread();
                Thread.sleep(FRAME_DELAY);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (DEBUG)
            System.out.println("KeyPressed");
        keysPressed.add(e.getKeyCode());

    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (DEBUG)
            System.out.println("KeyReleased: " + e.getKeyCode());
        keysPressed.remove(e.getKeyCode());
        leftMoving = false;
        rightMoving = false;
    }
}
