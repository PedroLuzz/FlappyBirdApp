package com.flappygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MyGdxGame extends ApplicationAdapter {


	private final float VIRTUAL_WIDTH = 720;
	private static final float VIRTUAL_HEIGHT = 1280;
	// Textos e sons
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	// Preferências e câmera
	Preferences preferencias;
	// Texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	// Renderização
	private ShapeRenderer shapeRenderer;
	// Formas e Colisões
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	// Dimensões do dispositivo
	private float larguraDispositivo;
	private float alturaDispositivo;
	// Variáveis de controle
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;
	private OrthographicCamera camera;
	private Viewport viewport;

	private static final float ALTURA_MINIMA_PASSARO = 0;
	private static final float ALTURA_MAXIMA_PASSARO = VIRTUAL_HEIGHT;


	private Texture silverCoin;
	private Texture goldCoin;
	private List<Coin> coins;

	@Override
	public void create() {
		inicializarTexturas();
		inicializaObjetos();
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	private void inicializarTexturas() {
		// Carrega as texturas
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		silverCoin = new Texture("silvercoin.png");
		goldCoin = new Texture("goldcoin.png");
	}

	private void inicializaObjetos() {
		// Inicializa objetos necessários
		coins = new ArrayList<>();
		random = new Random();

		batch = new SpriteBatch();
		random = new Random();
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	private void verificarEstadoJogo() {
		// Verifica o estado atual do jogo
		boolean toqueTela = Gdx.input.justTouched();
		if (estadoJogo == 0) {
			if (toqueTela) {
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		} else if (estadoJogo == 1) {

			// Verifica se ainda não há moedas e cria uma antes do primeiro cano
			if (coins.isEmpty()) {
				int coinValue = random.nextInt(2) == 0 ? 5 : 10; // Valor da moeda (5 ou 10)
				Texture coinTexture = coinValue == 5 ? silverCoin : goldCoin; // Textura da moeda

				Coin coin = new Coin(coinTexture, posicaoCanoHorizontal, posicaoInicialVerticalPassaro, coinValue);
				coins.add(coin);
			}
			if (toqueTela) {
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if (posicaoCanoHorizontal < -canoTopo.getWidth()) {
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
				// Spawn de moedas
				if (random.nextInt(10) == 0) {
					int coinValue = random.nextInt(2) == 0 ? 5 : 10; // Valor da moeda (5 ou 10)
					Texture coinTexture = coinValue == 5 ? silverCoin : goldCoin; // Textura da moeda

					Coin coin = new Coin(coinTexture, larguraDispositivo, random.nextInt((int) alturaDispositivo), coinValue);
					coins.add(coin);
				}
			}
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;
		} else if (estadoJogo == 2) {
			if (pontos > pontuacaoMaxima) {
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if (toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	private void detectarColisoes() {
		// Verifica colisões entre os objetos
		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2
		);

		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		if (colidiuCanoCima || colidiuCanoBaixo || posicaoInicialVerticalPassaro <= ALTURA_MINIMA_PASSARO || posicaoInicialVerticalPassaro >= ALTURA_MAXIMA_PASSARO) {
			if (estadoJogo == 1) {
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas() {
		// Renderiza as texturas na tela

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao],
				50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 110);

		for (Coin coin : coins) {
			batch.draw(coin.getTexture(), coin.getX(), coin.getY());
		}

		if (estadoJogo == 2) {
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		batch.end();
	}

	public void validarPontos() {
		// Verifica a pontuação e a variação do passaro
		if (posicaoCanoHorizontal < 50 - passaros[0].getWidth()) {
			if (!passouCano) {
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3) {
			variacao = 0;
		}

		// Verifica a colisão entre o passaro e as moedas
		for (int i = 0; i < coins.size(); i++) {
			Coin coin = coins.get(i);
			if (Intersector.overlaps(circuloPassaro, new Rectangle(coin.getX(), coin.getY(), coin.getTexture().getWidth(), coin.getTexture().getHeight()))) {
				pontos += coin.getValue();
				coins.remove(i);
				somPontuacao.play();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose() {

	}


	public class Coin {
		private final Texture texture;
		private final float x;
		private final float y;
		private final int value;

		public Coin(Texture texture, float x, float y, int value) {
			this.texture = texture;
			this.x = x;
			this.y = y;
			this.value = value;
		}

		public Texture getTexture() {
			return texture;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

		public int getValue() {
			return value;
		}
	}
}
