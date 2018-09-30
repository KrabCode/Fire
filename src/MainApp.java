import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;

enum Collision {TOP, BOT, LEFT, RIGHT, NONE};

public class MainApp extends PApplet{

    Map m;
    PShader mainShader;

    float[] blockPositionsX;
    float[] blockSizesX;
    float[] blockPositionsY;
    float[] blockSizesY;
    float[] blockDeathframes;
    float[] blockHealths;
    float[] blockMaxHealths;
    float[] blockLastHits;
    float[] ballPositionsXs;
    float[] ballPositionsYs;
    float[] ballRs;
    float[] platformPositionsX;
    float[] platformPositionsY;
    float[] platformSizesX;
    float[] platformSizesY;
    float[] platformLastHits;

    ArrayList<Ball> balls = new ArrayList<>();
    ArrayList<Ball> ballsToRemove = new ArrayList<>();
    ArrayList<Block> blocks = new ArrayList<>();
    ArrayList<Block> blocksToRemove = new ArrayList<>();
    ArrayList<Platform> platforms = new ArrayList<>();
    ArrayList<Platform> platformsToRemove = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main("MainApp");
    }

    public void settings() {
//        fullScreen(P2D);
        size(1000, 1000, P2D);
    }

    /*
     *
     *       SETUP
     *
     * */

    public void setup() {
        colorMode(HSB, 1,1,1,1);
        ellipseMode(CENTER);
        rectMode(CENTER);
        mainShader = loadShader("main.glsl");
        float size = min(width, height);
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
        for(float x = 1f; x <= xscl-1; x++){
            for(float y = 1; y <= yscl*.5; y++){
                PVector pos = new PVector(m.topleft.x + x*m.size/xscl, m.topleft.y + y*m.size/yscl);
                PVector size = new PVector(m.size/xscl*.7f, m.size/yscl*.7f);
                blocks.add(new Block(pos, size, 2));
            }
        }
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
        if(blocks.size()==0){
            generateBlocks();
        }
        for(Ball ball : balls){
            ball.update();
        }
        for(Platform p : platforms){
            p.update();
        }
        for(Block b : blocks){
            b.update();
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
        blocks.removeAll(blocksToRemove);
        blocksToRemove.clear();
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
        ArrayList<Ball> snappedToThis = new ArrayList<>();
        Platform(){
            pos = new PVector(m.size/2f, height-m.size/16f);
            size = new PVector(m.size/10f, m.size/60f);
            stepsPerFrame = 5;
            lastHitFrame = -5;
        }

        void update(){
            if(keyPressed || mousePressed){
            for(int i = 0; i < stepsPerFrame; i++){
                dirNormalized = 0;
                    if(key == 'a' || (mousePressed && mouseX < m.center.x)){
                        dirNormalized = -1;
                    }
                    if(key == 'd' || (mousePressed && mouseX > m.center.x)){
                        dirNormalized = 1;
                    }
                    if(key == ' '){
                        if(snappedToThis.size() > 0){
                            Ball b = snappedToThis.get(0);
                            b.release();
                            snappedToThis.remove(b);
                        }
                    }
                    pos.x += dirNormalized;
                }
            }else{
                dirNormalized = 0;
            }
            checkBoundsCollision();
        }

        public void onHit() {
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
        PVector maxSpd;
        int stepsPerFrame;
        float size;
        Platform snappedTo;

        Ball(){
            stepsPerFrame = 5;
            size = m.size*.02f;
            pos = new PVector(m.center.x,height/8);
            float maxSpd = 1.f-random(2f);
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
                        dirNormalized.x += p.dirNormalized/4f;

                    }
                }else if(colResult == Collision.BOT){
                    p.onHit();
                    if(dirNormalized.y < 0){
                       // stepsPerFrame.y *= -1; go straight through instead
                    }
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
