import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;


public class MainApp extends PApplet{

    ArrayList<Ball> balls = new ArrayList<>();
    ArrayList<Block> ballsToRemove = new ArrayList<>();
    Map m;

    ArrayList<Block> blocks = new ArrayList<>();
    ArrayList<Block> blocksToRemove = new ArrayList<>();

    PShader mainShader;

    float[] blockPositionsX;
    float[] blockSizesX;
    float[] blockPositionsY;
    float[] blockSizesY;
    float[] blockDeathframes;
    float[] ballPositionsXs;
    float[] ballPositionsYs;
    float[] ballRs;


    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    public void settings() {
//        fullScreen(P2D);
        size(800, 800, P2D);
    }

    public void setup() {
        colorMode(HSB, 1,1,1,1);
        ellipseMode(CENTER);
        rectMode(CENTER);
        mainShader = loadShader("main.glsl");
        m = new Map(min(width, height));
        generateBall();
        generateBlocks();
    }

    private void generateBall() {
        balls.add(new Ball());
        ballPositionsXs = new float[balls.size()];
        ballPositionsYs = new float[balls.size()];
        ballRs = new float[balls.size()];
    }

    void generateBlocks(){

        float xscl = 15f;
        float yscl = 15f;
        for(float x = 1f; x <= xscl-1; x++){
            for(float y = 1f; y <= yscl*.5; y++){
                PVector pos = new PVector(m.topleft.x + x*m.size/xscl, m.topleft.y + y*m.size/yscl);
                PVector size = new PVector(m.size/xscl*.7f, m.size/yscl*.7f);
                blocks.add(new Block(pos, size));
            }
        }
        blockPositionsX =   new float[blocks.size()];
        blockSizesX =       new float[blocks.size()];
        blockPositionsY =   new float[blocks.size()];
        blockSizesY =       new float[blocks.size()];
        blockDeathframes =  new float[blocks.size()];
    }

    public void draw() {
        noCursor();
        background(0);
        for(Ball ball : balls){
            ball.update();
        }
        sendBallsToShader();
        sendBlocksToShader();
        m.drawBackground();
    }


    private void sendBlocksToShader() {
        blocks.removeAll(blocksToRemove);
        blocksToRemove.clear();
        for(Block b : blocks){
            int i = blocks.indexOf(b);
            blockPositionsX[i] = b.pos.x;
            blockPositionsY[i] = b.pos.y;
            blockSizesX[i] = b.size.x;
            blockSizesY[i] = b.size.y;
            blockDeathframes[i] = b.deathFrame;
        }
        mainShader.set("blockPositionsX", blockPositionsX);
        mainShader.set("blockSizesX", blockSizesX);
        mainShader.set("blockPositionsY", blockPositionsY);
        mainShader.set("blockSizesY", blockSizesY);
        mainShader.set("blockDeathframes", blockDeathframes);
    }

    private void sendBallsToShader() {
        balls.remove(ballsToRemove);
        ballsToRemove.clear();
        for(Ball b : balls){
            int i = balls.indexOf(b);
            ballPositionsXs[i] = b.pos.x;
            ballPositionsYs[i] = b.pos.y;
            ballRs[i] = map(b.size, 0.f, m.size, 0.f, 1.f);
        }
        mainShader.set("ballPositionsX", ballPositionsXs);
        mainShader.set("ballPositionsY", ballPositionsYs);
        mainShader.set("ballRs", ballRs);
    }

    class Ball {
        PVector pos;
        PVector spd;
        float size;

        Ball(){
            size = m.size*.02f;
            pos = new PVector(width/2, height/2);
            spd = new PVector(random(-size/2f,size/2f), -size/2f);
        }

        void update(){
//            pos.x = mouseX;
//            pos.y = height-mouseY;
            checkBounds();
            checkBlocks();
            pos.add(spd);
        }

        private void checkBounds() {
            if(pos.x > width/2 +m.size/2-size/2){
                pos.x = width/2 +m.size/2-size/2;
                spd.x *= -1;
            }
            if(pos.x < width/2 -m.size/2+size/2){
                pos.x = width/2 -m.size/2+size/2;
                spd.x *= -1;
            }
            if(pos.y > height/2+m.size/2-size/2){
                pos.y = height/2+m.size/2-size/2;
                spd.y *= -1;
            }
            if(pos.y < height/2-m.size/2+size/2){
                pos.y = height/2-m.size/2+size/2;
                spd.y *= -1;
                println("death @ " + frameCount);
            }
        }

        private void checkBlocks() {
            for(Block b : blocks){
                if(b.deathFrame == 0){
                    int colResult = circleRect(pos.x-size/2, height-pos.y-size/2, size,
                            b.pos.x-b.size.x/2, b.pos.y-b.size.y/2, b.size.x, b.size.y);
                    if(colResult > 0 && colResult <= 2){
                        b.deathFrame = frameCount;
                        spd.x *= -1;
                    }else if(colResult >2){
                        b.deathFrame = frameCount;
                        spd.y*= -1;
                    }
                }
            }
        }


        int circleRect(float cx, float cy, float radius, float rx, float ry, float rw, float rh) {

            int result = 0;
            // temporary variables to set edges for testing
            float testX = cx;
            float testY = cy;

            // which edge is closest?
            if (cx < rx){
                testX = rx;      // test left edge
                result = 1;
            }
            else if (cx > rx+rw){
                testX = rx+rw;   // right edge
                result = 2;
            }
            if (cy < ry){
                testY = ry;      // top edge
                result = 3;
            }
            else if (cy > ry+rh){
                testY = ry+rh;   // bottom edge
                result = 4;
            }

            // get distance from closest edges
            float distX = cx-testX;
            float distY = cy-testY;
            float distance = sqrt( (distX*distX) + (distY*distY) );

            // if the distance is less than the radius, collision!
            if (distance <= radius) {
                return result;
            }
            return 0;
        }
    }

    class Map{
        float size;
        PVector topleft;

        Map(float size){
            this.size = size;
            topleft = new PVector(width/2-size/2, height/2-size/2);
        }

        void drawBackground(){
            mainShader.set("time", radians(frameCount));
            mainShader.set("size", size, size);
            mainShader.set("topleft", topleft.x, topleft.y);
            shader(mainShader);

            noStroke();
            fill(1);
            rect(width/2, height/2,size,size);
        }
    }

    class Block{
        PVector pos;
        PVector size;
        int health;
        int maxhealth;
        float deathFrame;
        Block(PVector pos, PVector size){
            this.pos = pos;
            this.size = size;
        }
    }
}
