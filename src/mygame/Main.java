package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.PointLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import java.util.Random;

/**
 * The main class contain all the code and algorithm
 *
 * @extends SimpleApplication
 * @author jinwei.zhang ID:201219957 x6jz3
 */
public class Main extends SimpleApplication {
    //collision sound ball collide with table and paddle

    private AudioNode collisionSound;
    //collision sound ball collide with balls
    private AudioNode collisionSound2;
    //backgroud music
    private AudioNode background;
    //start picture, win picture, lose picture 
    private Picture p = new Picture("Picture");
    //speed of the ball rotation
    private int rotationSpeed;
    //text that showed on the screen
    private BitmapText text;
    //the score in each round
    private int score;
    //the level that user achieve
    private int level;
    //the ball in move and the still balls
    private Node ball;
    private Node ball2, ball3, ball4, ball5, ball6;
    //table field
    private Node table;
    private Node paddle;
    Vector3f velocity;// speed of ball
    //exact corner point in blender
    final Vector3f upVector = new Vector3f((float) -4.7, 0, (float) -5.9);
    final Vector3f downVector = new Vector3f((float) -4.7, 0, (float) 5.9);
    final Vector3f leftVector = new Vector3f((float) -4.7, 0, (float) -5.9);
    final Vector3f rightVector = new Vector3f((float) 4.7, 0, (float) -5.9);
    //since the table is one node so that there is four ray to judge the collision
    Ray up = new Ray(upVector, Vector3f.UNIT_X);
    Ray down = new Ray(downVector, Vector3f.UNIT_X);
    Ray left = new Ray(leftVector, Vector3f.UNIT_Z);//point of the left corner
    Ray right = new Ray(rightVector, Vector3f.UNIT_Z);//pointer of the right corner

    @Override
    public void simpleInitApp() {
        //set the camera as stabale 
        this.flyCam.setEnabled(false);
        this.setDisplayFps(false);
        this.setDisplayStatView(false);
        //background music source
        background = new AudioNode(assetManager, "Sounds/Computer-melody-80s-style.wav", false);
        background.setPositional(false);
        background.setLooping(true);
        background.setVolume(0.1f);
        background.play();
        //audio sourse.
        collisionSound = new AudioNode(assetManager, "Sounds/ballcollision.wav", false);
        collisionSound.setPositional(false);
        collisionSound.setLooping(false);
        collisionSound.setVolume(0.5f);
        //ball ball collision
        collisionSound2 = new AudioNode(assetManager, "Sounds/boom.wav", false);
        collisionSound2.setPositional(false);
        collisionSound2.setLooping(false);
        collisionSound2.setVolume(0.5f);


        //text in the field
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        text = new BitmapText(guiFont);
        text.setSize(guiFont.getCharSet().getRenderedSize());
        //set the position
        text.move( // x/y coordinates and z = depth layer 0
                settings.getWidth() / 30,
                text.getLineHeight() + 1000,
                0);

        text.setSize(48f);
        guiNode.attachChild(text);




        // load the table node
        table = (Node) assetManager.loadModel("Models/table.j3o");
        rootNode.attachChild(table);

        /*
         *
         * set up the ball position, ball2 is mercury ball3 is venus ball4 is
         * mars ball5 is jupiter ball6 is neptune
         *
         */
        ball = (Node) assetManager.loadModel("Models/earth.j3o");
        ball.scale(0.4f);
        rootNode.attachChild(ball);
        //load the other all node
        ball2 = (Node) assetManager.loadModel("Models/Mercury.j3o");
        ball3 = (Node) assetManager.loadModel("Models/Venus.j3o");
        ball4 = (Node) assetManager.loadModel("Models/mars.j3o");
        ball2.scale(0.5f);
        ball3.scale(0.35f);
        ball4.scale(0.7f);
        rootNode.attachChild(ball2);
        rootNode.attachChild(ball3);
        rootNode.attachChild(ball4);
        ball5 = (Node) assetManager.loadModel("Models/Jupiter.j3o");
        ball5.scale(0.35f);
        ball5.setLocalTranslation(10, 10, 10);//garbage position
        rootNode.attachChild(ball5);
        ball6 = (Node) assetManager.loadModel("Models/neptune.j3o");
        ball6.scale(0.4f);
        ball6.setLocalTranslation(10, 10, 10);//garbage position
        rootNode.attachChild(ball6);
        /*
         *
         * set up the paddle position
         *
         */

        paddle = (Node) assetManager.loadModel("Models/board.j3o");
        paddle.scale(1, 1, 0.7f);
        rootNode.attachChild(paddle);
        /*
         * input event handle there are four event
         * 1. move paddle left
         * 2. move paddle right
         * 3. restart key TAB
         * 4. gravity function Key
         */
        inputManager.addMapping("Move right",
                new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Move left",
                new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Restart",
                new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("Function",
                new KeyTrigger(KeyInput.KEY_SPACE));
        // Test multiple listeners per mapping
        inputManager.addListener(analogListener, "Move right");
        inputManager.addListener(analogListener, "Move left");
        inputManager.addListener(analogListener, "Restart");
        inputManager.addListener(analogListener, "Function");
        // Set up the lights
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1, -1, -1));
        rootNode.addLight(sun);
        //set up a point light
        PointLight myLight = new PointLight();
        myLight.setColor(ColorRGBA.White);
        myLight.setPosition(new Vector3f(0, 2, 2));
        myLight.setRadius(20);
        rootNode.addLight(myLight);


        // Casting shadows
        // The monkey can only cast shadows
        ball.setShadowMode(RenderQueue.ShadowMode.Cast);
        // The table can both cast and receive
        table.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        // setting up the renderers. Every kind of light needs a separate one
        PointLightShadowRenderer plsr = new PointLightShadowRenderer(assetManager, 512);

        plsr.setLight(myLight);
        plsr.setFlushQueues(false); // should be false for all but the last renderer

        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 512, 2);
        dlsr.setLight(sun);

        // adding them to the view port (what we see)
        viewPort.addProcessor(plsr);
        viewPort.addProcessor(dlsr);

        /*
         *
         * set up the camera and look at the original position
         *
         */
        cam.setLocation(new Vector3f(0, 17, 6));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        cam.clearViewportChanged();

        // load the sky background
        rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        //all the picture that is going to use
        p.move(0, 0, 1); // make it appear behind stats view(behend the text)
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/start.png", false);

        // attach geometry to orthoNode
        guiNode.attachChild(p);
        velocity = new Vector3f(0, 0, 0);
        ball.setLocalTranslation(0, 0, 3);

    }
    /**
     * AnalogListener anonymous class handles all the keyboard input SPACE key
     * for the gravity LEFT key to move the paddle left RIGHT key to move the
     * paddle right TAB key to restart the game
     */
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            if (name.equals("Move right")) {
                paddle.move(7 * tpf, 0, 0);


            } else if (name.equals("Move left")) {
                paddle.move(-7 * tpf, 0, 0);
            } else if (name.equals("Restart")) {
                //set all the ball to a garbage position
                ball2.setLocalTranslation(10, 10, 10);//garbage position
                ball3.setLocalTranslation(10, 10, 10);//garbage position
                ball4.setLocalTranslation(10, 10, 10);//garbage position
                ball5.setLocalTranslation(10, 10, 10);//garbage position
                ball6.setLocalTranslation(10, 10, 10);//garbage position
                //let the picture dispear
                p.setWidth(0);
                p.setHeight(0);
                //restart the level 1
                Level_1();
            } else if (name.equals("Function")) {
                // if user press the SPACE key velocity of Z will be added by .2f
                velocity.setZ(velocity.getZ() + 0.2f);
                Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                boxMat.setBoolean("UseMaterialColors", true);
                boxMat.setColor("Diffuse", ColorRGBA.White);
                //paddle change color to give player feedback

                paddle.setMaterial(boxMat);
            }
        }
    };

    /**
     * when the ball is out of the field the stop method will be triggered
     */
    protected void Stop() {
        rotationSpeed = 0;
        velocity = new Vector3f(0, 0, 0);
        //set it below the paddle
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 2.5));
        //move the paddle down make sure it wont collide with the ball 
        paddle.setLocalTranslation(0, 0, 10);
        // make it appear in front of stats view
        p.move(0, 0, 1);
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/GameOver.png", false);



    }

    /**
     * The first level of the game set up the ball set up the initial speed of
     * ball and the roation speed of
     *
     */
    protected void Level_1() {

        level = 1;
        score = 0;
        rotationSpeed = 2;
        //ball start at random angle 
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        velocity = velocity.mult(5f);
        ball.setLocalTranslation(0, 0, (float) (6 - 0.35 * 2));
        //also set up the position of  ball2 ball3 and ball4 in level1
        ball2.setLocalTranslation(-2f, 0, -2f);
        ball3.setLocalTranslation(2f, 0, -2f);
        ball4.setLocalTranslation(0, 0, -2f);
        paddle.setLocalTranslation(0, 0, 6);






    }

    /**
     * The second level of the game, increase the initial speed of the ball
     * change the postion of the other balls are place in different position
     *
     */
    protected void Level_2() {



        level = 2;
        score = 0;

        rotationSpeed = 3;
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        //add the speed of the earth (red ball)
        velocity = velocity.mult(7f);
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 3));
        //set up new position of the balls
        ball2.setLocalTranslation(-2f, 0, -2f);
        ball3.setLocalTranslation(2f, 0, -2f);
        ball4.setLocalTranslation(-2f, 0, 2f);
        ball5.setLocalTranslation(2f, 0, 2f);
        paddle.setLocalTranslation(0, 0, 6);

    }

    /**
     * The third level of the game, increase the initial speed of the ball
     * change the postion of the other balls are place in different position
     *
     */
    protected void Level_3() {


        level = 3;
        score = 0;


        rotationSpeed = 4;
        float num = new Random().nextFloat() * 2 - 1;
        velocity = new Vector3f(num, 0, -1);
        velocity = velocity.mult(12f);
        //set up all the position of the ball
        ball.setLocalTranslation(0, 0, (float) (6 - 0.3 * 3));
        ball2.setLocalTranslation(0, 0, 3f);
        ball3.setLocalTranslation(-2f, 0, 0);
        ball4.setLocalTranslation(2f, 0, 0);
        ball5.setLocalTranslation(-1f, 0, -3f);
        ball6.setLocalTranslation(1f, 0, -3f);

        paddle.setLocalTranslation(0, 0, 6);

    }

    protected void Win() {


        p.move(0, 0, 1); // make it appear behind stats view
        p.setPosition(0, 0);
        p.setWidth(settings.getWidth());
        p.setHeight(settings.getHeight());
        p.setImage(assetManager, "Interface/winning.jpg", false);

        velocity = velocity.mult(0f);


    }

    /**
     * update loop of the game, simpleUpdate will be executed throughout the
     * whole game
     *
     */
    @Override
    public void simpleUpdate(float tpf) {


        text.setText("Level " + level + "\n" + "Your Score: " + (score) + "\n \nSPACE--gravity");
        ball.rotate(0, rotationSpeed * FastMath.PI * tpf, 0);
        //initial move!
        ball.move(velocity.mult(tpf));
        /*if paddle collision with the table
         */
        CollisionResults resultsPaddle = new CollisionResults();
        BoundingVolume bvBord = paddle.getWorldBound();
        table.collideWith(bvBord, resultsPaddle);

        if (resultsPaddle.size() > 0) {
            //Restrict the move range of the paddle 
            if (paddle.getLocalTranslation().x < 0) {
                //board.move((float) 0.02, 0, 0);
                paddle.setLocalTranslation((float) -3.65, 0, 6);
            } else {
                paddle.setLocalTranslation((float) 3.65, 0, 6);
            }
        }



        /*ball collision with the table
         
         
         */
        CollisionResults resultUp = new CollisionResults();
        CollisionResults resultDown = new CollisionResults();
        CollisionResults resultLeft = new CollisionResults();
        CollisionResults resultRight = new CollisionResults();
        ball.collideWith(up, resultUp);
        ball.collideWith(down, resultDown);
        ball.collideWith(left, resultLeft);
        ball.collideWith(right, resultRight);
        if (resultUp.size() > 0) {


            velocity.setZ(FastMath.abs(velocity.getZ()));
            velocity = velocity.mult(1.005f);
            collisionSound.playInstance();
        } else if (resultDown.size() > 0) {
            //if the ball fly out of field
            Stop();


        } else if (resultLeft.size() > 0) {

            velocity.setX(FastMath.abs(velocity.getX()));
            velocity = velocity.mult(1.005f);
            collisionSound.playInstance();
        } else if (resultRight.size() > 0) {

            velocity.setX(-FastMath.abs(velocity.getX()));
            velocity = velocity.mult(1.005f);
            collisionSound.playInstance();
        }

        /**
         * ball collision with paddle
         *
         *
         */
        CollisionResults resultBallPaddle = new CollisionResults();
        BoundingVolume bvBall = ball.getWorldBound();
        paddle.collideWith(bvBall, resultBallPaddle);

        if (resultBallPaddle.size() > 0) {


            collisionSound.playInstance();
            velocity.setZ(-FastMath.abs(velocity.getZ()));
            //friction on x axis
            if (paddle.getLocalTranslation().getX() < 0) {
                velocity.setX(velocity.getX() + 0.5f);

            } else if (paddle.getLocalTranslation().getX() > 0) {
                velocity.setX(velocity.getX() - 0.5f);

            }

           //change the colour the paddle --feedback
            Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            boxMat.setBoolean("UseMaterialColors", true);

            boxMat.setColor("Diffuse", ColorRGBA.randomColor());


            paddle.setMaterial(boxMat);

        }
        /**
         * ball collision with ball2
         *
         *
         */
        CollisionResults resultBallBall_2 = new CollisionResults();
        BoundingVolume bvBall_2 = ball2.getWorldBound();
        ball.collideWith(bvBall_2, resultBallBall_2);

        if (resultBallBall_2.size() > 0) {

            collisionSound2.playInstance();
            score += 1;
            //normal vector from ball to ball2
            Vector3f norm = new Vector3f((ball2.getLocalTranslation().subtract(ball.getLocalTranslation())).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            ball2.move(10, 10, 10);// move all ball here after collision 

        }
        /**
         * ball collision with ball3
         *
         *
         */
        CollisionResults resultBallBall_3 = new CollisionResults();
        BoundingVolume bvBall_3 = ball3.getWorldBound();
        ball.collideWith(bvBall_3, resultBallBall_3);

        if (resultBallBall_3.size() > 0) {

            collisionSound2.playInstance();
            score += 1;
            //normal vector from ball to ball2
            Vector3f norm = new Vector3f(ball3.getLocalTranslation().subtract(ball.getLocalTranslation()).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            ball3.move(10, 10, 10);// move all ball here after collision 

        }
        /**
         * ball collision with ball4
         *
         *
         */
        CollisionResults resultBallBall_4 = new CollisionResults();
        BoundingVolume bvBall_4 = ball4.getWorldBound();
        ball.collideWith(bvBall_4, resultBallBall_4);

        if (resultBallBall_4.size() > 0) {

            collisionSound2.playInstance();
            score += 1;
            //normal vector from ball to ball2
            Vector3f norm = new Vector3f(ball4.getLocalTranslation().subtract(ball.getLocalTranslation()).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            ball4.move(10, 10, 10);// move all ball here after collision 

        }

        /**
         * ball collision with ball5
         *
         *
         */
        CollisionResults resultBallBall_5 = new CollisionResults();
        BoundingVolume bvBall_5 = ball5.getWorldBound();
        ball.collideWith(bvBall_5, resultBallBall_5);

        if (resultBallBall_5.size() > 0) {

            collisionSound2.playInstance();
            score += 1;
            //normal vector from ball to ball2
            Vector3f norm = new Vector3f(ball5.getLocalTranslation().subtract(ball.getLocalTranslation()).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            ball5.move(10, 10, 10);// move all ball here after collision 

        }

        /**
         * ball collision with ball6
         *
         *
         */
        CollisionResults resultBallBall_6 = new CollisionResults();
        BoundingVolume bvBall_6 = ball6.getWorldBound();
        ball.collideWith(bvBall_6, resultBallBall_6);

        if (resultBallBall_6.size() > 0) {

            collisionSound2.playInstance();
            score += 1;
            //normal vector from ball to ball2
            Vector3f norm = new Vector3f(ball6.getLocalTranslation().subtract(ball.getLocalTranslation()).normalize());
            //length of projection on norm
            float projVal = velocity.dot(norm);
            //vector projrction
            Vector3f projection = norm.mult(projVal);
            //parall vector
            Vector3f parall = velocity.subtract(projection);
            velocity = parall.subtract(projection);
            ball6.move(10, 10, 10);// move all ball here after collision 

        }

        if (score == 3 && level == 1) {

            Level_2();
        }
        if (score == 4 && level == 2) {
            Level_3();
        }
        if (score == 5 && level == 3) {
            Win();

        }


    }

    /**
     * The main method simply set up the initial screen and setting then run the
     * game the setting menu will not appear screen resolution is set to
     * 1920X1080
     *
     *
     */
    public static void main(String[] args) {

        Main app = new Main();
        app.showSettings = false;
        AppSettings screenSet = new AppSettings(true);
        screenSet.put("Width", 1920);
        screenSet.put("Height", 1080);
        //title of the game
        screenSet.put("Title", "jinwei's space ball game!");
        app.setSettings(screenSet);
        //start the game.
        app.start();


    }
}
