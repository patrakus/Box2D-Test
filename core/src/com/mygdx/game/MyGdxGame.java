package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Generator.ShapeGenerator;

public class MyGdxGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture greenSquareWithContourForSquares;
	private Texture ground;
	private World simulatingWorld;
	private OrthographicCamera camera;
	//private Stage stage;
	private Box2DDebugRenderer debugRenderer;
	private ShapeGenerator shapeGenerator;
	private float timer;
	private Array<Body> bodies;
	private Array<Sprite> greenSquareSprites;
	private Array<Sprite> redRectangleSprites;
	private RandomXS128 random;
	private Sprite background;

	private final int PPM = 100;

	@Override
	public void create () {
		initVariables();
		initObjects();
		setupCamera();
		Box2D.init();
		createStageForStaticGround();
		setSpritesForGround();
	}

	private void initVariables() {
		timer = 0;
	}

	private void initObjects() {
		bodies = new Array<Body>();
		greenSquareSprites = new Array<Sprite>();
		redRectangleSprites = new Array<Sprite>();
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth()/PPM,
				(Gdx.graphics.getHeight()/ PPM) * Gdx.graphics.getWidth()/Gdx.graphics.getHeight());

		simulatingWorld = new World( new Vector2(0f, -10), true);
		debugRenderer = new Box2DDebugRenderer();
		shapeGenerator = new ShapeGenerator(simulatingWorld);

		greenSquareWithContourForSquares = new Texture("greenBoxWithContour.png");
		ground = new Texture("ground.png");
		random = new RandomXS128();

		background = new Sprite(new Texture("bg.png"));
		background.setPosition(0f,0f);
		background.setSize(6.4f,8f);
	}

	private void setupCamera(){
		camera.position.set( camera.viewportWidth/2, camera.viewportHeight/2, 0);
		camera.update();
	}

	private void createStageForStaticGround() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		bodyDef.position.set(3.2f,0.1f);
		Body bodyGround = simulatingWorld.createBody(bodyDef);
		PolygonShape square = new PolygonShape();
		square.setAsBox(3.2f,0.1f);
		bodyGround.createFixture(square, 0f);
		square.dispose();

		bodyDef.position.set(5.9f, 3f);
		Body bodyRightWall = simulatingWorld.createBody(bodyDef);
		square = new PolygonShape();
		square.setAsBox(0.1f, 3f);
		bodyRightWall.createFixture(square,0);
		square.dispose();

		bodyDef.position.set(0.1f, 3f);
		Body bodyLeftWall = simulatingWorld.createBody(bodyDef);
		square = new PolygonShape();
		square.setAsBox(0.1f, 3f);
		bodyLeftWall.createFixture(square,0);
		square.dispose();
	}

	private void setSpritesForGround() {
		Sprite tmp = new Sprite(ground);
		tmp.setSize(6.4f, 0.2f);
		//tmp.setPosition(0, 0);
		tmp.setOriginCenter();
		redRectangleSprites.add(tmp);

		tmp = new Sprite(ground);
		tmp.setSize(6f, 0.2f);
		tmp.setOriginCenter();
		tmp.setPosition(-2.9f, 2.9f);
		tmp.rotate(90f);
		redRectangleSprites.add(tmp);

		tmp = new Sprite(ground);
		tmp.setSize(6f, 0.2f);
		tmp.setOriginCenter();
		tmp.setPosition(2.9f, 2.9f);
		tmp.rotate(90f);
		redRectangleSprites.add(tmp);

	}

	@Override
	public void resize(int width, int height) {
		//camera.viewportHeight = (30 / width) * height;
		//camera.update();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		moveCamera();
		spawnFigure(3.0f);
		setSpritesByBodiesPosition();

		//debugRenderer.render(simulatingWorld, camera.combined);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		background.draw(batch);

		for(Sprite square: redRectangleSprites) {
			square.draw(batch);
		}

		for(Sprite square: greenSquareSprites) {
			square.draw(batch);
		}

		batch.end();

		simulatingWorld.step(1/45f, 6, 2);
	}

	@Override
	public void dispose() {
		batch.dispose();
		simulatingWorld.dispose();
		greenSquareWithContourForSquares.dispose();
		background.getTexture().dispose();
	}

	private void moveCamera() {
		Vector3 cameraPos = new Vector3(camera.position);
		if (Gdx.input.isKeyPressed(Input.Keys.A))
			cameraPos.add( -10f * Gdx.graphics.getDeltaTime(), 0 ,0);
		else if(Gdx.input.isKeyPressed(Input.Keys.D))
			cameraPos.add(10f * Gdx.graphics.getDeltaTime(), 0, 0);

		if (Gdx.input.isKeyPressed(Input.Keys.W))
			cameraPos.add( 0, 10f * Gdx.graphics.getDeltaTime() ,0);
		else if(Gdx.input.isKeyPressed(Input.Keys.S))
			cameraPos.add(0, -10f * Gdx.graphics.getDeltaTime(), 0);

		if (!cameraPos.equals( new Vector3( camera.position))) {
			camera.position.set(cameraPos);
			camera.update();
			//System.out.println(camera.position.toString());
		}

	}

	private void spawnFigure( float timeToNextAppear) {
		timer += Gdx.graphics.getDeltaTime();
		Body tempHolder;
		if(timer >= timeToNextAppear) {
			timer = 0;

			tempHolder = shapeGenerator.generateShape( random.nextLong(2) + 2 + random.nextFloat(), 7.0f);
			tempHolder.setUserData(generateNextSprite());
			bodies.add(tempHolder);
		}
	}

	private Sprite generateNextSprite() {

		Sprite sprite = new Sprite(greenSquareWithContourForSquares);
		sprite.setSize(0.416f, 0.416f);
		sprite.setOriginCenter();
		greenSquareSprites.add( sprite);

		return sprite;
	}

	private void setSpritesByBodiesPosition() {

		/*for( int i = 0; i < greenSquareSprites.size; ++i)
		{
			greenSquareSprites.get(i).setPosition(bodies.get(i).getPosition().x - 0.2f, bodies.get(i).getPosition().y - 0.2f);
			// We need to convert our angle from radians to degrees
			greenSquareSprites.get(i).setRotation(MathUtils.radiansToDegrees * bodies.get(i).getAngle());

			//System.out.println(greenSquareSprites.get(i).getX() + " " + greenSquareSprites.get(i).getY());
		}*/
		for (Body b : bodies) {
			// Get the body's user data - in this example, our user
			// data is an instance of the Entity class
			Sprite e = (Sprite) b.getUserData();

			if (e != null) {
				// Update the entities/sprites position and angle
				e.setPosition(b.getPosition().x - 0.208f , b.getPosition().y - 0.208f);
				// We need to convert our angle from radians to degrees
				e.setRotation(MathUtils.radiansToDegrees * b.getAngle());
			}
		}
	}
}
