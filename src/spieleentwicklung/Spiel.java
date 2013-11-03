package spieleentwicklung;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Spiel {

	public static void main(String[] args) {

		List<Bullet> bullets = new LinkedList<Bullet>();
		List<Enemy> enemies = new LinkedList<Enemy>();
		Player player = new Player(300, 300, 800, 600, bullets, enemies);
		Background bg = new Background(50);
		new Frame(player, bg, bullets, enemies);

	}
}

class Frame extends JFrame {

	final Player player;
	final Background bg;

	private BufferStrategy strat;
	private List<Bullet> bullets;
	private List<Enemy> enemies;
	private static Random r = new Random();


	public Frame(Player player, Background bg, List<Bullet> bullets,
			List<Enemy> enemies) {
		super("Don Traing");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setUndecorated(true);
		setVisible(true);
		setResizable(false);
		setLocationRelativeTo(null);

		addKeyListener(new Keyboard());

		this.player = player;
		this.bg = bg;
		this.bullets = bullets;
		this.enemies = enemies;

		makeStrat();

		final float ENEMYSPAWNTIME = 1f;
		float timeSinceLastEnemySpawn = 0;

		long lastFrame = System.currentTimeMillis();

		while (true) {
			if (Keyboard.isKeyDown(KeyEvent.VK_ESCAPE)) {
				System.exit(0);
			}

			long thisFrame = System.currentTimeMillis();
			float timeSinceLastFrame = ((float) (thisFrame - lastFrame)) / 1000f;
			lastFrame = thisFrame;

			timeSinceLastEnemySpawn += timeSinceLastFrame;
			if (timeSinceLastEnemySpawn > ENEMYSPAWNTIME) {
				timeSinceLastEnemySpawn -= ENEMYSPAWNTIME;
				spawnEnemy();
			}

	
			player.update(timeSinceLastFrame);

			bg.update(timeSinceLastFrame);

			repaintScreen();



			for (Iterator<Bullet> iter = bullets.iterator(); iter.hasNext();) {
				Bullet b = iter.next();
				if (b.isReadyToDie()) {
					iter.remove();
				}
			}

			for (int i = 0; i < bullets.size(); i++) {
				bullets.get(i).update(timeSinceLastFrame);
			}

			for (int i = 0; i < enemies.size(); i++) {
				enemies.get(i).update(timeSinceLastFrame);
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void spawnEnemy() {
		enemies.add(new Enemy(800, r.nextInt(600 - Enemy.getHeight()), bullets));
	}

	public void makeStrat() {
	
		createBufferStrategy(2);
		strat = getBufferStrategy();
	}

	public void repaintScreen() {
		Graphics g = strat.getDrawGraphics();
		draw(g);

		g.dispose();
		strat.show();
	}

	private void draw(Graphics g) {
		g.setColor(Color.RED);
	
		g.drawImage(bg.getLook(), bg.getX(), 0, null);

		g.drawImage(bg.getLook(), bg.getX() + bg.getLook().getWidth(), 0, null);

		for (int i = 0; i < enemies.size(); i++) {
			Enemy e = enemies.get(i);
			g.drawImage(e.getLook(), e.getBounding().x, e.getBounding().y, null);

		}
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);

			g.drawImage(Bullet.getLook(), b.getBounding().x, b.getBounding().y,
					null);

		}

		g.drawImage(player.getLook(), player.getBounding().x,
				player.getBounding().y, null);
	}

}

class Player {

	Rectangle bounding;
	private float f_posX, f_posY;
	private int worldSizeX, worldSizeY;
	private BufferedImage look;
	private BufferedImage lookDead;
	private List<Bullet> bullets;
	private List<Enemy> enemies;
	private float timeSinceLastShot;
	private final float shotInterval = 0.1f;
	private boolean alive = true;

	public Player(int f_posX, int f_posY, int worldSizeX, int worldSizeY,
			List<Bullet> bullets, List<Enemy> enemies) {
		
		try {
			look = ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("gfx/raumschiffchen.png"));
			lookDead = ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("gfx/raumschiff_kaputt.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		bounding = new Rectangle(f_posX, f_posY, look.getWidth(),
				look.getHeight());


		this.f_posX = f_posX;
		this.f_posY = f_posY;
		this.worldSizeX = worldSizeX;
		this.worldSizeY = worldSizeY;
		this.bullets = bullets;
		this.enemies = enemies;

	}


	public void update(float timeSinceLastFrame) {
		if (!alive)
			return;


		timeSinceLastShot += timeSinceLastFrame;


		if (Keyboard.isKeyDown(KeyEvent.VK_W)) {
			f_posY -= 300 * timeSinceLastFrame;
			System.out.println(KeyEvent.VK_W);
		}
		if (Keyboard.isKeyDown(KeyEvent.VK_S)) {
			f_posY += 300 * timeSinceLastFrame;
			System.out.println(KeyEvent.VK_S);
		}
		if (Keyboard.isKeyDown(KeyEvent.VK_A)) {
			f_posX -= 300 * timeSinceLastFrame;
			System.out.println(KeyEvent.VK_A);
		}
		if (Keyboard.isKeyDown(KeyEvent.VK_D)) {
			f_posX += 300 * timeSinceLastFrame;
			System.out.println(KeyEvent.VK_D);
		}

		if (timeSinceLastShot > shotInterval
				&& Keyboard.isKeyDown(KeyEvent.VK_SPACE)) {
			timeSinceLastShot = 0;
			System.out.println(KeyEvent.VK_SPACE);


			bullets.add(new Bullet(f_posX + look.getWidth() / 2
					- Bullet.getLook().getWidth() / 2, f_posY
					+ look.getHeight() / 2 - Bullet.getLook().getHeight() / 2,
					500, 0, bullets));

		}
		// bouncing bla bla

		if (f_posX < 0) {
			f_posX = 0;
		}
		if (f_posX > worldSizeX - look.getWidth()) {
			f_posX = worldSizeX - look.getHeight();
		}
		if (f_posY < 0) {
			f_posY = 0;
		}
		if (f_posY > worldSizeY - look.getHeight()) {
			f_posY = worldSizeY - look.getHeight();
		}

		bounding.x = (int) f_posX;
		bounding.y = (int) f_posY;

		for (int i = 0; i < enemies.size(); i++) {
			Enemy e = enemies.get(i);

			if (e.isAlive() && bounding.intersects(e.getBounding())) {
				alive = false;
			}
		}

	}

	public Rectangle getBounding() {
		return bounding;
	}

	public BufferedImage getLook() {
		if (alive) {
			return look;
		} else
			return lookDead;
	}

}

class Background {

	private float f_posX;
	private float f_speedY;
	private BufferedImage look;

	public Background(float f_speed) {

		this.f_speedY = f_speed;

		try {
			look = ImageIO.read(getClass().getClassLoader()
					.getResourceAsStream("gfx/weltraum.png"));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public void update(float timeSinceLastFrame) {
		
		f_posX -= f_speedY * timeSinceLastFrame;


		if (f_posX < -look.getWidth()) {
			f_posX += look.getWidth();
		}

	}

	public int getX() {
	
		return (int) f_posX;
	}

	public BufferedImage getLook() {
		return look;
	}

}

class Keyboard implements KeyListener {

	private static boolean[] keys = new boolean[1024];


	public static boolean isKeyDown(int keyCode) {
	
		if (keyCode >= 0 && keyCode < keys.length) {
			System.out.println("class Keyboard" + keys[keyCode]);
			return keys[keyCode];
		} else {
			return false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		if (keyCode >= 0 && keyCode < keys.length) {
			keys[keyCode] = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		int KeyCode = e.getKeyCode();

		if (KeyCode >= 0 && KeyCode < keys.length) {
			keys[KeyCode] = false;
		}
	}


	@Override
	public void keyTyped(KeyEvent e) {

	}

}

class Bullet {

	private static BufferedImage look;

	static {

		try {
			look = ImageIO.read(Bullet.class.getClassLoader()
					.getResourceAsStream("gfx/schuss.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private float f_posX;
	private float f_posY;
	private float f_speedX;
	private float f_speedY;
	private Rectangle bounding;
	private float timeAlive;
	private final float timeToLive = 3;
	private boolean readyToDie;

	public Bullet(float posX, float posY, float speedX, float speedY,
			List<Bullet> bullets) {
		this.f_posX = posX;
		this.f_posY = posY;
		this.f_speedX = speedX;
		this.f_speedY = speedY;
		bounding = new Rectangle((int) posX, (int) posY, look.getWidth(),
				look.getHeight());
	}

	public void update(float timeSinceLastFrame) {
		timeAlive += timeSinceLastFrame;
		if (timeAlive > timeToLive) {
			readyToDie = true;

		}
		f_posX += f_speedX * timeSinceLastFrame;
		f_posY += f_speedY * timeSinceLastFrame;
		bounding.x = (int) f_posX;
		bounding.y = (int) f_posY;
	}

	public Rectangle getBounding() {
		return bounding;
	}

	public static BufferedImage getLook() {
		return look;
	}

	public boolean isReadyToDie() {
		return readyToDie;
	}
}

class Enemy {

	private static BufferedImage[] look = new BufferedImage[4];

	private static BufferedImage lookDead;
	private final static float NEEDEDANITIME = 0.5f;

	private float aniTime = 0;
	private final static Random r = new Random();
	private float f_posX;
	private float f_posY;
	private Rectangle bounding;
	private List<Bullet> bullets;
	private boolean alive = true;

	static {
		try {
			look[0] = ImageIO.read(Enemy.class.getClassLoader()
					.getResourceAsStream("gfx/enemy1.png"));
			look[1] = ImageIO.read(Enemy.class.getClassLoader()
					.getResourceAsStream("gfx/enemy2.png"));
			look[2] = ImageIO.read(Enemy.class.getClassLoader()
					.getResourceAsStream("gfx/enemy3.png"));

			look[3] = look[1];

			lookDead = ImageIO.read(Enemy.class.getClassLoader()
					.getResourceAsStream("gfx/enemy_kaputt.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Enemy(float f_posX, float f_posY, List<Bullet> bullets) {
		this.f_posX = f_posX;
		this.f_posY = f_posY;
		this.bullets = bullets;
		bounding = new Rectangle((int) f_posX, (int) f_posY,
				look[0].getWidth(), look[0].getHeight());
	}

	public void update(float timeSinceLastFrame) {
		aniTime += timeSinceLastFrame;
		if (aniTime > NEEDEDANITIME) {
			aniTime = 0;
	
		}

		f_posX -= 100 * timeSinceLastFrame;// 100 pixel/sek.
		if (f_posX <= -getLook().getWidth()) {
			f_posX = 800;
			f_posY = r.nextInt(600 - getLook().getHeight());
			alive = true;

		}

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);

			if (alive && bounding.intersects(b.getBounding())) {
				alive = false;
				bullets.remove(b);
			}
		}
		bounding.x = (int) f_posX;
		bounding.y = (int) f_posY;
	}

	public Rectangle getBounding() {
		return bounding;
	}


	public BufferedImage getLook() {
		if (!alive)
			return lookDead;
		else {
			
			if (look.length == 0)
				return null;
			for (int i = 0; i < look.length; i++) {
		
				if (aniTime < (float) (NEEDEDANITIME / look.length * (i + 1))) {
					System.out.println((float) NEEDEDANITIME / look.length
							* (i + 1));
					return look[i];
				}

			}
		}


		return look[look.length - 1];
	}

	public boolean isAlive() {
		return alive;
	}

	public static int getWidth() {
		return look[0].getWidth();
	
	}

	public static int getHeight() {
		return look[0].getHeight();
	}

}

