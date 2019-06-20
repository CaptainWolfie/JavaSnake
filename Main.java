import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

/*
 * Created by CaptainWolfie
 * 20-6-2019
*/
public class Main implements Runnable,KeyListener {
	
	private enum directions {
		DOWN,
		UP,
		LEFT,
		RIGHT
	}
	
	private final int WIDTH,HEIGHT,FPS=15;
	private final String TITLE;
	private boolean running;
	private Thread thread;
	private BufferStrategy bs;
	private Graphics g;
	private Canvas canvas;
	private JFrame frame;
	private List<Integer> x = new ArrayList<Integer>();
	private List<Integer> y = new ArrayList<Integer>();
	directions direction;
	int appleX, appleY;
	int size = 20;
	int best = 0;
	boolean lost = false;

	public Main() {
		WIDTH = 600;
		HEIGHT = 500;
		TITLE = "Simple Snake Game";
		createDisplay();
	}
		
	int max = 3;
	private void init() {
		direction = directions.DOWN;
		x = new ArrayList<Integer>();
		y = new ArrayList<Integer>();
		for (int i = 0; i < max; i++) {
			x.add(0);
			y.add(-i);
		}
		Random r = new Random();
		appleX = r.nextInt(WIDTH / size);
		appleY = r.nextInt(HEIGHT / size);
	}
	
	private void render() {
		bs = canvas.getBufferStrategy();
		if (bs == null) {
			canvas.createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		g.clearRect(0, 0, WIDTH, HEIGHT);
		// Start
		g.setColor(new Color(62, 97, 155));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		for (int i = 0; i < x.size(); i++) {
			g.setColor(Color.white);
			g.fillOval(x.get(i) * size, y.get(i) * size, size - 5, size - 5);
		}
		g.setColor(new Color(219, 46, 30));
		g.fillOval(appleX * size, appleY * size, size - 5, size - 5);
		g.setColor(Color.black);
		((Graphics2D) g).setFont(new Font("SansSerif", Font.PLAIN, 30));
		g.drawString("SCORE", 10, 40);
		int width = g.getFontMetrics().stringWidth("SCORE");
		int width1 = g.getFontMetrics().stringWidth((x.size() - max) + "");
		g.drawString((x.size() - max) + "", 10 + (width / 2) - (width1 / 2), 70);
		
		width = g.getFontMetrics().stringWidth("BEST");
		g.drawString("BEST", WIDTH - 10 - width, 40);
		width1 = g.getFontMetrics().stringWidth(best + "");
		g.drawString(best + "", WIDTH - 10 - (width / 2) - (width1 / 2), 70);
		
		if (lost) {
			width = g.getFontMetrics().stringWidth("You Lost");
			g.drawString("You Lost", WIDTH / 2 - width / 2, 100);
			width1 = g.getFontMetrics().stringWidth("Press enter to continue");
			g.drawString("Press enter to continue", WIDTH / 2 - width1 / 2, 140);
		}
		// End
		bs.show();
		g.dispose();
	}
		
	private void tick() {
		if (!lost) {
			for (int i = x.size() - 1; i > 0; i--) {
				x.set(i, x.get(i-1));
			}
			for (int i = y.size() - 1; i > 0; i--) {
				y.set(i, y.get(i-1));
			}
			
			if (direction == directions.DOWN) {
				if (y.get(0) * size >= HEIGHT)
					y.set(0, 0);
				else
					y.set(0, y.get(0) + 1);
			}
			else if (direction == directions.UP) {
				if (y.get(0) * size <= 0)
					y.set(0, HEIGHT / size - 1);
				else
					y.set(0, y.get(0) - 1);
			}
			else if (direction == directions.LEFT) {
				if (x.get(0) * size <= 0)
					x.set(0, WIDTH / size - 1);
				else
					x.set(0, x.get(0) - 1);
			}
			else if (direction == directions.RIGHT) {
				if (x.get(0) * size >= WIDTH)
					x.set(0, 0);
				else
					x.set(0, x.get(0) + 1);
			}
		}
		for (int i = 1; i < x.size(); i++) {
			if (x.get(0) == x.get(i) && y.get(0) == y.get(i)) {
				lost = true;
				return;
			}
		}
		if (x.get(0) == appleX && y.get(0) == appleY && !lost) {
			x.add(x.get(x.size()-1));
			y.add(y.get(y.size()-1));
			Random r = new Random();
			appleX = r.nextInt(WIDTH / size);
			appleY = r.nextInt(HEIGHT / size);
		}
	}
	
	@Override
	public void run() {
		init();
		render();
		
		double delta = 0;
		long now;
		long lastTime = System.nanoTime();
		while(running) {
			double timePerTick = 1000000000 / FPS;
			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			lastTime = now;

			if (delta >= 1) {
				tick();
				render();
				delta = 0;
			}
		}
		
		stop();
	}

	public synchronized void start() {
		thread = new Thread(this);
		if (running)
			return;
		thread.start();
		running = true;
	}
	
	public synchronized void stop() {
		if (!running)
			return;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void createDisplay() {
		Dimension size = new Dimension(WIDTH,HEIGHT);
		frame = new JFrame(TITLE);
		frame.setSize(size);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.addKeyListener(this);
		
		canvas = new Canvas();
		canvas.setPreferredSize(size);
		canvas.setMaximumSize(size);
		canvas.setMinimumSize(size);
		canvas.addKeyListener(this);
		frame.add(canvas);
		frame.pack();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (direction != directions.RIGHT && (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT))
			direction = directions.LEFT;
		if (direction != directions.LEFT && (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT))
			direction = directions.RIGHT;
		if (direction != directions.DOWN && (key == KeyEvent.VK_W || key == KeyEvent.VK_UP))
			direction = directions.UP;
		if (direction != directions.UP && (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN))
			direction = directions.DOWN;
		if (key == KeyEvent.VK_SPACE && lost) {
			lost = false;
			if (x.size() - max > best)
				best = x.size() - max;
			init();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
