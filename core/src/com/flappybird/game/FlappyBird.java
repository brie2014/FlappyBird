package com.flappybird.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	/* UTILITIES */
	SpriteBatch batch;
	Random randomGenerator;
	BitmapFont font;
	float screenHeight;
	float screenWidth;


	/* IMAGES */
	Texture background;
	Texture gameOver;


	/* GAME STATE */
	// 0 not playing, 1 playing, 2 game over
	int gameState = 0;
	int score = 0;
	int highScore = 0;
	int scoringTube = 0;

	/* BIRD */
	Texture [] birds;
	Circle birdCircle;
	int birdFrame = 0;
	float birdY = 0;
	float velocity = 0;
	float gravity = 2;

	/* TUBES */
	Texture topTube;
	Texture bottomTube;
	float tubeGap = 500;
	float maxTubeOffset;
	int tubeVelocity = 4;
	int numberOfTubes = tubeVelocity;
	float [] tubeX = new float[numberOfTubes];
	float [] tubeOffset = new float[numberOfTubes];
	float distanceBetweenTubes;
	Rectangle [] topTubeRectangles;
	Rectangle [] bottomTubeRectangles;


	/* Method to create and setup an instance of the game */
	@Override
	public void create() {
		/* UTILITIES */
		batch = new SpriteBatch();
		randomGenerator = new Random();
		screenHeight = Gdx.graphics.getHeight();
		screenWidth = Gdx.graphics.getWidth();

		/* IMAGES */
		background = new Texture("background.png");
		gameOver = new Texture("gameover.png");

		/* BIRDS */
		// Bird images
		birds = new Texture[4];
		birds [0] = new Texture("bird_frame1.png");
		birds [1] = new Texture("bird_frame2.png");
		birds [2] = new Texture("bird_frame3.png");
		birds [3] = new Texture("bird_frame4.png");
		// Bird shape, used to check if bird collides into the tubes
		birdCircle = new Circle();

		/* TUBES */
		maxTubeOffset = screenHeight/2 - tubeGap/2 - 100;
		distanceBetweenTubes = screenWidth * 3/4;
		// Tube images
		topTube = new Texture("toptube.png");
		bottomTube = new Texture("bottomtube.png");
		// Tube shapes, used to check if bird collides into their space
		topTubeRectangles = new Rectangle[numberOfTubes];
		bottomTubeRectangles = new Rectangle[numberOfTubes];

		// Fonts
		font = new BitmapFont();
		font.setColor(Color.WHITE);

		// Start the game
		startGame();
	}

	/* Method to render the game screens */
	@Override
	public void render() {
		/* SETUP GAME */
		batch.begin();
		batch.draw(background, 0, 0, screenWidth, screenHeight);
		updateBirdAnimation();

		/* DECIDE WHAT TO DO BASED ON gameState */
		// gameState 1: Game is not started
		if (gameState == 0) {
			font.getData().setScale(6);
			font.draw(batch, "Tap to Start", screenWidth/2-200, birdY + 300);
			// Start the game on a touch event
			if(Gdx.input.justTouched()){
				gameState = 1;
			}
		}
		// gameState 1: Game is running
		else if(gameState == 1){
			updateScore();
			if(Gdx.input.justTouched()){
				velocity = -30;
			}
			renderTubes();
			checkCollisions();
			checkBirdY();
		}
		// gameState 2: Game is over
		else if (gameState == 2) {
			// Draw the game over screen
			batch.draw(gameOver, screenWidth/2-gameOver.getWidth()/2f, screenHeight/2 - gameOver.getHeight()/2f);
			// Restart the game on a touch event
			if(Gdx.input.justTouched()){
				restartGame();
			}
		}

		/* DRAW BIRD AND SCORE */
		renderBird();
		font.getData().setScale(12);
		font.draw(batch, String.valueOf(score), 100, 250);
		font.draw(batch, String.valueOf(highScore), screenWidth - 200, 250);
		font.getData().setScale(4);
		font.draw(batch, "Score", 100, 100);
		font.draw(batch, "High Score", screenWidth - 350, 100);


		batch.end();
	}


	/* PRIVATE METHODS */
	/* Method to check if the bird is still above the bottom of the screen and adjust velocity */
	private void checkBirdY(){
		if( birdY > 0){
			// Fall faster and faster
			velocity = velocity + gravity;
			birdY -= velocity;
		}
		// Game over if we fall to the bottom of the screen
		else {
			gameOver();
		}
	}
	/* Method to check if the bird hits a tube */
	private void checkCollisions(){
		for (int i = 0; i < numberOfTubes; i++){
			// Check if the bird hits a tube
			if(Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
				gameOver();
				break;
			}
		}
	}
	private void gameOver(){
		gameState = 2;
		if(score > highScore){
			highScore = score;
		}
	}
	/* Method to render the bird and bird circle (for checking collisions) on the game screen */
	private void renderBird(){
		birdCircle.set(screenWidth/2, birdY + birds[birdFrame].getHeight()/2f, birds[birdFrame].getWidth()/2f);
		batch.draw(birds[birdFrame], screenWidth/ 2 -birds[birdFrame].getWidth()/2f,  birdY, 136, 96);
	}
	/* Method to render the tubes and rectangles (for checking collisions) on the game screen */
	private void renderTubes(){
		// We loop through and render the same group of tubes continually
		for (int i = 0; i < numberOfTubes; i++){
			// If the tube is off to the left of the screen, move it to the right so it can be used again
			if(tubeX[i] < -topTube.getWidth()){
				tubeX[i] = numberOfTubes * distanceBetweenTubes;
			}
			// Else, move the tube to the left
			else{
				tubeX[i] = tubeX[i] - tubeVelocity;
			}
			// Draw the tubes
			batch.draw(topTube, tubeX[i], screenHeight/2 + tubeGap/2 + tubeOffset[i]);
			batch.draw(bottomTube, tubeX[i], screenHeight/2 - tubeGap/2 - bottomTube.getHeight() + tubeOffset[i]);

			// Render the rectangle shapes that we use to detect collisions with the bird circle
			topTubeRectangles[i] = new Rectangle(tubeX[i], screenHeight/2 + tubeGap/2 + tubeOffset[i], topTube.getWidth(), topTube.getHeight()-100);
			bottomTubeRectangles[i] = new Rectangle(tubeX[i], screenHeight/2 - tubeGap/2 - bottomTube.getHeight() + tubeOffset[i], bottomTube.getWidth(), bottomTube.getHeight() -100);
		}
	}
	/* Method to reset variables and restart the game */
	private void restartGame(){
		gameState = 1;
		score = 0;
		scoringTube = 0;
		velocity = 0;
		startGame();
	}
	/* Method to start the game */
	private void startGame(){
		birdY = screenHeight/2 - (float) birds[0].getHeight() /2;

		for (int i = 0; i < numberOfTubes; i++){
			tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (screenHeight - tubeGap - 200);
			tubeX[i] = screenWidth/2 - topTube.getWidth()/2f + screenWidth + i * distanceBetweenTubes;
			topTubeRectangles[i] = new Rectangle();
			bottomTubeRectangles[i] = new Rectangle();
		}
	}
	/* Method to update which bird image we use to get the animation effect */
	private void updateBirdAnimation(){
		if(birdFrame < 3){
			birdFrame++;
		}
		else {
			birdFrame = 0;
		}
	}
	/* Method to update the score when the bird passes through a pipe */
	private void updateScore(){
		if(tubeX[scoringTube] < screenWidth/2){
			score++;
			if(scoringTube < numberOfTubes - 1){
				scoringTube++;
			}else {
				scoringTube = 0;
			}
		}
	}

}
