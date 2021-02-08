package com.cursoandroid.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Flappybird extends ApplicationAdapter {

	//SpriteBack se utiliza para crear animaciones con texturas!
	private SpriteBatch batch;

	private Texture[] birds;
	private Texture bg;
	private Texture bottomTube;
	private Texture topTube;
	private Texture gameOver;
	private Random randomNumber;
	private BitmapFont font;
	private BitmapFont msg;
	private Circle birdCircle;
	private Rectangle topTubeRec;
	private Rectangle bottomTubeRec;
	//private ShapeRenderer shape;

	//Atributos de configuraciones
	private float widthDevice;
	private float heightDevice;
	private int gamestate=0;
	private int score=0;

	private float variation = 0;
	private float speedfall = 0;
	private float initVertPosition;
	private float tubeMovPositionH;
	private float tubeSpace;
	private float deltaTime;
	private float tubesHight;
	private boolean scored=false;

	//Ajuste de Camara y Viewport
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_HIGHT=1024;
	private final float VIRTUAL_WIDTH=768;

	@Override
	public void create() {
		batch = new SpriteBatch();
		randomNumber = new Random();
		birdCircle = new Circle();
		//shape = new ShapeRenderer();

		//Display de puntaje
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(6);

		//Display info reiniciar el juego
		msg = new BitmapFont();
		msg.setColor(Color.WHITE);
		msg.getData().setScale(3);



		//Define Texturas
		birds = new Texture[3];
		birds[0] = new Texture("bird1.png");
		birds[1] = new Texture("bird2.png");
		birds[2] = new Texture("bird3.png");
		bg = new Texture("background.png");
		bottomTube = new Texture("bottomTube.png");
		topTube = new Texture("topTube.png");
		gameOver = new Texture("game_over.png");

		//Configurar camara
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2,VIRTUAL_HIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HIGHT,camera);

		//Utilice FitViewport si quieres que la pantalla corte las bordas para ajustar tamaño
		//viewport = new FitViewport(VIRTUAL_WIDTH,VIRTUAL_HIGHT,camera);

		//Define posiciones de texturas
		widthDevice = VIRTUAL_WIDTH;
		heightDevice = VIRTUAL_HIGHT;
		initVertPosition = heightDevice / 2;
		tubeMovPositionH = widthDevice;
		tubeSpace = 300;

	}

	@Override
	public void render() {

		//Actualiza camara
		camera.update();

		//Limpiar frames anteriores y ajusta colores (Optimizacion de memoria)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		//Obtiene escala de tiempo de la lib Gdx
		deltaTime = Gdx.graphics.getDeltaTime();

		//Variacion de imagenes para la animación del pajaro
		variation += deltaTime * 10;
		if (variation > birds.length) variation = 0;


		//Condicion del estado del juego
		if(gamestate==0){
			if(Gdx.input.justTouched()){
				gamestate = 1;
			}
		}else {

			//Incremento de velocidad de queda del pajaro
			speedfall++;

			//Si el pajaro llega al borde inferior de la pantalla o recibe un salto deja de caer
			if (initVertPosition > 0 || speedfall < 0)
				initVertPosition = initVertPosition - speedfall;

			if(gamestate==1){

				//Decremento horizontal para la animacion de los tubos
				tubeMovPositionH -= deltaTime * 200;

				//Si la pantalla es tocada, decrementa la velocidad de queda para producir un salto
				if (Gdx.input.justTouched())
					speedfall = -15;

				//Vuelve a generar los tubos al llegar al final de la pantalla alternando su pocision
				if (tubeMovPositionH < -topTube.getWidth()) {
					tubeMovPositionH = widthDevice;
					tubesHight = randomNumber.nextInt(400) - 200;
					scored = false;
				}

				//Conteo de puntaje
				if(tubeMovPositionH<120) {
					if(!scored) {
						score++;
						scored = true;
					}
				}

			}else{ // Game Over
				if(Gdx.input.justTouched()){
					gamestate=0;
					score=0;
					speedfall=0;
					initVertPosition=heightDevice/2;
					tubeMovPositionH = widthDevice;
				}
			}



		}

		//Configurar datos de proyeccion de camara
		batch.setProjectionMatrix(camera.combined);

		//Renderización de las texturas
		batch.begin();
		batch.draw(bg,0,0,widthDevice,heightDevice);
		batch.draw(topTube,tubeMovPositionH,(heightDevice / 2) + (tubeSpace/2) + tubesHight);
		batch.draw(bottomTube,tubeMovPositionH,(heightDevice / 2) - bottomTube.getHeight() - (tubeSpace/2) + tubesHight);
		batch.draw(birds[(int)variation], 120,initVertPosition);
		font.draw(batch, String.valueOf(score),(widthDevice / 2)-(((int)font.getScaleX())/2),heightDevice - 50);
		if(gamestate==2) {
			batch.draw(gameOver, (widthDevice / 2) - (((int) gameOver.getWidth()) / 2), heightDevice / 2);
			msg.draw(batch, "Toque para reiniciar",(widthDevice / 2) - (((int) gameOver.getWidth()) / 2),(heightDevice / 2)- gameOver.getHeight()/2);
		}
		batch.end();

		//Define areas de colision
		birdCircle.set(120 + (birds[0].getWidth()/2), initVertPosition+(birds[0].getHeight()/2), birds[0].getWidth()/2);
		bottomTubeRec = new Rectangle(
				tubeMovPositionH,
				(heightDevice / 2) - bottomTube.getHeight() - (tubeSpace/2) + tubesHight,
				bottomTube.getWidth(),
				bottomTube.getHeight());
		topTubeRec = new Rectangle(
				tubeMovPositionH,
				(heightDevice / 2) + (tubeSpace/2) + tubesHight,
				topTube.getWidth(),
				topTube.getHeight());

		//Dibujar formas (apenas para pruebas)
		/*shape.begin(ShapeRenderer.ShapeType.Filled);
		shape.circle(birdCircle.x, birdCircle.y, birdCircle.radius);
		shape.rect(bottomTubeRec.x,bottomTubeRec.y,bottomTubeRec.width,bottomTubeRec.height);
		shape.rect(topTubeRec.x,topTubeRec.y,topTubeRec.width,topTubeRec.height);
		shape.setColor(Color.RED);
		shape.end();*/


		//Detecta colision del pajaro
		if(Intersector.overlaps(birdCircle,bottomTubeRec) || Intersector.overlaps(birdCircle,topTubeRec)
		|| initVertPosition <= 0 || initVertPosition >= heightDevice){
			gamestate = 2;
		}

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}