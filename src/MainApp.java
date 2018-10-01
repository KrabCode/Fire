import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;

enum Collision {TOP, BOT, LEFT, RIGHT, NONE};

public class MainApp extends PApplet{

    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    private Map m;
    private PShader mainShader;
    int globalMaxHealth = 2;

    private float[] blockPositionsX;
    private float[] blockSizesX;
    private float[] blockPositionsY;
    private float[] blockSizesY;
    private float[] blockDeathframes;
    private float[] blockHealths;
    private float[] blockMaxHealths;
    private float[] blockLastHits;
    private float[] ballPositionsXs;
    private float[] ballPositionsYs;
    private float[] ballRs;
    private float[] platformPositionsX;
    private float[] platformPositionsY;
    private float[] platformSizesX;
    private float[] platformSizesY;
    private float[] platformLastHits;

    private ArrayList<Ball> balls = new ArrayList<Ball>();
    private ArrayList<Ball> ballsToRemove = new ArrayList<Ball>();
    private ArrayList<Block> blocks = new ArrayList<Block>();
    private ArrayList<Platform> platforms = new ArrayList<Platform>();
    private ArrayList<Platform> platformsToRemove = new ArrayList<Platform>();

    public void settings() {
        fullScreen(P2D);
//        size(1000, 1000, P2D);
    }

    /*
     *
     *       SETUP
     *
     * */

    public void setup() {
        background(0);
        colorMode(HSB, 1,1,1,1);
        ellipseMode(CENTER);
        rectMode(CENTER);
        mainShader = loadShader("main.glsl");
        mainShader.set("smoothstepOffset", .009f);
        float size = height;
        m = new Map(size);
        generatePlatforms();
        generateBall();
        generateBlocks();
    }

    private void generatePlatforms() {
        platforms.add(new Platform());
        platformPositionsX = new float[platforms.size()];
        platformPositionsY = new float[platforms.size()];
        platformSizesY = new float[platforms.size()];
        platformSizesX = new float[platforms.size()];
        platformLastHits = new float[platforms.size()];
    }

    private void generateBall() {
        balls.add(new Ball(platforms.get(0)));
        ballPositionsXs = new float[balls.size()];
        ballPositionsYs = new float[balls.size()];
        ballRs = new float[balls.size()];
    }

    private void generateBlocks(){
        float xscl = 15f;
        float yscl = 15f;
        for(float x = m.center.x-m.size/2+m.size/xscl; x < m.center.x+m.size/2; x+= m.size/xscl){
            for(float y = m.center.y-m.size/2+m.size/yscl; y <= m.center.y ; y+= m.size/yscl){
                PVector pos = new PVector(x, y);
                PVector size = new PVector(m.size/xscl*.5f, m.size/yscl*.5f);
                blocks.add(new Block(pos, size, globalMaxHealth));
            }
        }
        println(blocks.size());
        blockPositionsX =   new float[blocks.size()];
        blockSizesX     =   new float[blocks.size()];
        blockPositionsY =   new float[blocks.size()];
        blockSizesY      =  new float[blocks.size()];
        blockDeathframes =  new float[blocks.size()];
        blockHealths     =  new float[blocks.size()];
        blockMaxHealths =  new float[blocks.size()];
        blockLastHits =  new float[blocks.size()];
    }

    /*
     *
     *       DRAW
     *
     * */

    public void draw() {

        for(Ball ball : balls){
            ball.update();
        }
        for(Platform p : platforms){
            p.update();
        }
        boolean everyBlockCleared = true;
        for(Block b : blocks){
            b.update();
            if(b.deathFrame == 0){
                everyBlockCleared = false;
                break;
            }
        }
        if(everyBlockCleared){
            generateBlocks();
        }
        sendBallsToShader();
        sendBlocksToShader();
        sendPlatformsToShader();
        m.drawBackground();
        resetShader();
        /*
        for(Block b : blocks){
            if(b.deathFrame!=0)continue;
            rect(b.pos.x, b.pos.y, b.size.x,b.size.y);
        }
        rect(m.topleft.x, m.topleft.y, 20,20);
        ellipse(balls.get(0).pos.x,balls.get(0).pos.y, balls.get(0).size, balls.get(0).size);
        */
    }

    private void sendBlocksToShader() {
        for(Block b : blocks){
            int i = blocks.indexOf(b);

            blockPositionsX[i] = b.pos.x;
            blockPositionsY[i] = b.pos.y;
            blockSizesX[i] = b.size.x;
            blockSizesY[i] = b.size.y;

            blockDeathframes[i] = b.deathFrame;
            blockHealths[i] = b.health;
            blockMaxHealths[i] = b.maxHealth;
            blockLastHits[i] = b.lastHitFrame;
        }

        mainShader.set("blockPositionsX", blockPositionsX);
        mainShader.set("blockSizesX", blockSizesX);
        mainShader.set("blockPositionsY", blockPositionsY);
        mainShader.set("blockSizesY", blockSizesY);
        mainShader.set("blockDeathframes", blockDeathframes);
        mainShader.set("blockHealths", blockHealths);
        mainShader.set("blockMaxHealths", blockMaxHealths);
        mainShader.set("blockLastHits", blockLastHits);
    }

    private void sendBallsToShader() {
        balls.remove(ballsToRemove);
        ballsToRemove.clear();
        for(Ball b : balls){
            int i = balls.indexOf(b);
            ballPositionsXs[i] = b.pos.x;
            ballPositionsYs[i] = b.pos.y;
            ballRs[i] = b.size;
        }
        mainShader.set("ballPositionsX", ballPositionsXs);
        mainShader.set("ballPositionsY", ballPositionsYs);
        mainShader.set("ballRs", ballRs);
    }

    private void sendPlatformsToShader() {
        platforms.removeAll(platformsToRemove);
        platformsToRemove.clear();
        for(Platform p : platforms){
            int i = platforms.indexOf(p);
            platformPositionsX[i] = p.pos.x;
            platformPositionsY[i] = p.pos.y;
            platformSizesX[i] = p.size.x;
            platformSizesY[i] = p.size.y;
            platformLastHits[i] = p.lastHitFrame;
        }
        mainShader.set("platformPositionsX", platformPositionsX);
        mainShader.set("platformPositionsY", platformPositionsY);
        mainShader.set("platformSizesX", platformSizesX);
        mainShader.set("platformSizesY", platformSizesY);
        mainShader.set("platformLastHits", platformLastHits);

    }

    /*
     *
     *       MAP
     *
     * */

    class Map{
        float size;
        PVector topleft;
        PVector center;

        Map(float size){
            this.size = size;
            center = new PVector(width/2, height/2);
            topleft = new PVector(center.x-size/2, center.y-size/2);
        }

        void drawBackground(){
            mainShader.set("time", (float)frameCount);
            mainShader.set("size", size, size);
            mainShader.set("topleft", topleft.x, topleft.y);
            shader(mainShader);
            noStroke();
            fill(1);
            rect(topleft.x+size/2f, topleft.y+size/2f,size,size);
        }
    }

    /*
     *
     *       PLATFORM
     *
     * */

    class Platform{
        PVector pos;
        PVector size;
        int stepsPerFrame;
        float dirNormalized;
        float lastHitFrame;
        ArrayList<Ball> snappedToThis = new ArrayList<Ball>();
        Platform(){
            pos = new PVector(m.size/2f, height-m.size/16f);
            size = new PVector(m.size/10f, m.size/60f);
            stepsPerFrame = 8;
            lastHitFrame = -5;
        }

        void update(){
            if(keyPressed || mousePressed){
                if(snappedToThis.size() > 0){
                    Ball b = snappedToThis.get(0);
                    b.release();
                    snappedToThis.remove(b);
                }
                dirNormalized = 0;
                if(key == 'a' || (mousePressed && mouseX < m.center.x)){
                    dirNormalized = -1;
                }
                if(key == 'd' || (mousePressed && mouseX > m.center.x)){
                    dirNormalized = 1;
                }
                pos.x += dirNormalized*stepsPerFrame;
            }else{

                dirNormalized = 0;
            }
            checkBoundsCollision();
        }

        void onHit() {
            lastHitFrame = frameCount;
        }

        private void checkBoundsCollision() {
            if(pos.x > m.center.x +m.size/2-size.x/2){
                pos.x = m.center.x +m.size/2-size.x/2;
            }
            if(pos.x < m.center.x -m.size/2+size.x/2){
                pos.x = m.center.x -m.size/2+size.x/2;
            }
        }
    }

    /*
    *
    *       BALL
    *
    * */

    class Ball {
        PVector pos;
        PVector dirNormalized;
        int stepsPerFrame;
        float size;
        Platform snappedTo;

        Ball(){
            stepsPerFrame = 5;
            size = m.size*.02f;
            pos = new PVector(m.center.x,height/8);
        }

        Ball(Platform snapToThis){
            this();
            if(snapToThis!=null){
                snappedTo = snapToThis;
                snapToThis.snappedToThis.add(this); //remember this ball on the platform for release purposes
                dirNormalized = new PVector();
            }
        }

        void release(){
            snappedTo = null;
            dirNormalized = PVector.fromAngle(random(0f,PI));
        }

        void update(){
            if(snappedTo != null){
                pos.x = snappedTo.pos.x;
                pos.y = snappedTo.pos.y - snappedTo.size.y/2 - size/2;
            }
            for(int i = 0; i < stepsPerFrame; i++){
                checkBoundsCollision();
                checkBlocksCollision();
                checkPlatformCollision();
                pos.add(dirNormalized);
            }
        }

        private void checkPlatformCollision() {
            for(Platform p : platforms){
                Collision colResult = getCircleRectCollision(pos.x, pos.y, size/2,
                        p.pos.x-p.size.x/2, p.pos.y-p.size.y/2, p.size.x, p.size.y);
                if(colResult == Collision.LEFT){
                    p.onHit();
                    if(dirNormalized.x > 0){
                        dirNormalized.x *= -1;
                    }
                    dirNormalized.x *= -1;
                }else if(colResult == Collision.RIGHT){
                    p.onHit();
                    if(dirNormalized.x < 0){
                        dirNormalized.x *= -1;
                    }
                }else if(colResult == Collision.TOP){
                    p.onHit();
                    if(dirNormalized.y > 0){
                        dirNormalized.y *= -1;
                        dirNormalized.y = min(dirNormalized.y, -0.5f);
                        dirNormalized.x += p.dirNormalized/2f + random(-.2f,.2f);

                    }
                }else if(colResult == Collision.BOT){
                    p.onHit();
                }
                dirNormalized.limit(1.f);
            }
        }

        private void checkBoundsCollision() {
            if(pos.x > m.center.x +m.size/2-size/2){
                pos.x = m.center.x +m.size/2-size/2;
                dirNormalized.x *= -1;
            }
            if(pos.x < m.center.x -m.size/2+size/2){
                pos.x = m.center.x -m.size/2+size/2;
                dirNormalized.x *= -1;
            }
            if(pos.y > m.center.y+m.size/2-size/2){
                pos.y = m.center.y+m.size/2-size/2;
                dirNormalized.y *= -1;
            }
            if(pos.y < m.center.y-m.size/2+size/2){
                pos.y = m.center.y-m.size/2+size/2;
                dirNormalized.y *= -1;
                println("death @ " + frameCount);
            }
        }

        private void checkBlocksCollision() {
            for(Block b : blocks){
                if(b.deathFrame == 0){
                    Collision colResult = getCircleRectCollision(pos.x, pos.y, size,
                            b.pos.x-b.size.x/2, b.pos.y-b.size.y/2, b.size.x, b.size.y);
                    if(colResult == Collision.BOT || colResult == Collision.TOP){
                        b.onHit();
                        dirNormalized.y*= -1;
                    }else if(colResult == Collision.LEFT || colResult == Collision.RIGHT){
                        b.onHit();
                        dirNormalized.x*= -1;
                    }
                }
            }
        }

        /**
         * My everlasting thanks to Jeffrey Thompson
         * http://www.jeffreythompson.org/collision-detection/circle-rect.php
         */
        Collision getCircleRectCollision(float cx, float cy, float radius, float rx, float ry, float rw, float rh) {
            Collision result = Collision.NONE;
            // temporary variables to set edges for testing
            float testX = cx;
            float testY = cy;

            // which edge is closest?
            if (cx < rx){
                testX = rx;      // test left edge
                result = Collision.LEFT;
            }
            else if (cx > rx+rw){
                testX = rx+rw;   // right edge
                result = Collision.RIGHT;
            }
            if (cy < ry){
                testY = ry;      // top edge
                result = Collision.TOP;
            }
            else if (cy > ry+rh){
                testY = ry+rh;   // bottom edge
                result = Collision.BOT;
            }

            // get distance from closest edges
            float distX = cx-testX;
            float distY = cy-testY;
            float distance = sqrt( (distX*distX) + (distY*distY) );

            // if the distance is less than the radius, collision!
            if (distance <= radius) {
                return result;
            }
            return Collision.NONE;
        }
    }

    /*
     *
     *       BLOCK
     *
     * */

    class Block{
        PVector pos;
        PVector size;
        int health;
        int maxHealth;
        float deathFrame;
        float lastHitFrame;
        Block(PVector pos, PVector size, int maxHealth){
            this.health = maxHealth;
            this.maxHealth = maxHealth;
            this.pos = pos;
            this.size = size;
        }

        public void onHit() {
            health--;
            lastHitFrame = (float) frameCount;
            if(health <= 0){
                deathFrame = frameCount;
            }
        }

        public void update() {
            float elapsed = frameCount - lastHitFrame;

        }
    }

    PVector getPointAtAngle(float cx, float cy, float radius, float angle) {
        return new PVector(cx + radius * cos(angle), cy + radius * sin(angle));
    }

    float getAngle(float x0,float  y0,float  x1,float  y1) {
        return atan2(y1 - y0, x1 - x0);
    }
}
