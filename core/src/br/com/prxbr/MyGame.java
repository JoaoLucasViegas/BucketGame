package br.com.prxbr;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGame extends ApplicationAdapter {

	//Configuration Settings
	GameState gameplayCurrentState;

	//Movement Settings
	private int bucketSpeedMovement = 400;
	private int dropSpeedMovement = 200;

	//Rendering Settings
	private Vector3 touchPos = new Vector3();
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private int screenWidth;
	private int screenHeight;

	//Assets
	private Texture dropImage;
	private Texture bucketImage;
	private Texture pauseMenuImage;
	private Sound dropSound;
	private Music rainMusic;

	/** NODES **/
	//Bucket
	private Rectangle bucket;
	private int bucketWidth = 64;
	private int bucketHeight = 64;

	//Raindrops
	private Array<Rectangle> raindropsList;
	private long lastDropTime;
	private int raindropWidth = 64;
	private int raindropHeight = 64;
	/** END_NODES **/

	@Override
	public void create() {

		//Definition
		gameplayCurrentState = GameState.GAMEPLAY;
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		//Textures
		dropImage = new Texture("drop.png");
		bucketImage = new Texture("bucket.png");
		pauseMenuImage = new Texture("pauseScreen.png");

		//Sounds and Musics
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		rainMusic.setLooping(true);
		rainMusic.play();

		//Camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, screenWidth, screenHeight);

		//batch
		batch = new SpriteBatch();

		//bucket initializing
		bucket = new Rectangle();
		bucket.x = (screenWidth >> 1) - (bucketWidth >> 1);
		bucket.y = 20;
		bucket.width = bucketWidth;
		bucket.height = bucketHeight;

		//raindrops initializing
		raindropsList = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render() {

		if (gameplayCurrentState == GameState.GAMEPLAY)
			mainLoop();

		if (gameplayCurrentState == GameState.MENU)
			pauseMenu();

	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	@Override
	public void pause() {
		gameplayCurrentState = GameState.MENU;
	}

	private void pauseMenu() {
		batch.begin();
		batch.draw(pauseMenuImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();

		rainMusic.pause();
	}

	private void mainLoop() {
		if (gameplayCurrentState == GameState.GAMEPLAY) {

			if (!rainMusic.isPlaying())
				rainMusic.play();

			if (TimeUtils.millis() - lastDropTime > 1000) spawnRaindrop();

			ScreenUtils.clear(0, 0, 0.2f, 1);
			camera.update();
			batch.setProjectionMatrix(camera.combined);
			batch.begin();

			//draw Bucket
			batch.draw(bucketImage, bucket.x, bucket.y);

			//draw Current-Drop
			for (Rectangle raindrop : raindropsList) {
				batch.draw(dropImage, raindrop.x, raindrop.y);
			}

			batch.end();

			moveRaindrop();
			moveBucket();

			if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
				gameplayCurrentState = GameState.MENU;
			}
		}
	}

	private void moveBucket() {
		//Mouse or TouchScreen Inputs
		if (Gdx.input.isTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - (bucketWidth >> 1);
		}

		//Keyboard Inputs
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
			bucket.x -= bucketSpeedMovement * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
			bucket.x += bucketSpeedMovement * Gdx.graphics.getDeltaTime();
		}

		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > screenWidth - bucketWidth) bucket.x = screenWidth - bucketWidth;
	}

	private void moveRaindrop() {
		for (Iterator<Rectangle> iter = raindropsList.iterator(); iter.hasNext();) {
			Rectangle raindrop = iter.next();
			raindrop.y -= dropSpeedMovement * Gdx.graphics.getDeltaTime();;

			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}

			if (raindrop.y + raindropHeight < 0) iter.remove();
		}
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, screenWidth - bucketWidth);
		raindrop.y = screenHeight;
		raindrop.width = raindropWidth;
		raindrop.height = raindropHeight;
		raindropsList.add(raindrop);
		lastDropTime = TimeUtils.millis();
	}
}
