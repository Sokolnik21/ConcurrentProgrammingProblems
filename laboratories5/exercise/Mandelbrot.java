/**
 * Based on code from: rosettacode.org
 * example: Mandelbrot set [Java]
 */
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import java.util.*;
import java.util.concurrent.*;

class Point {
  int x;
  int y;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
  public int getX() { return x; }
  public int getY() { return y; }
}
class PointWithIteration {
  Point p;
  int iter;
  public PointWithIteration(Point p, int iter) {
    this.p = p;
    this.iter = iter;
  }
  public Point getPoint() { return p; }
  public int getIter() { return iter; }
}

class CalculationProvider implements Callable<List<PointWithIteration>> {
  private List<Point> pointsList;
  private int MAX_ITER;
  private double ZOOM;
  private double zx, zy, cX, cY, tmp;

  public CalculationProvider(List<Point> pointsList, int MAX_ITER, double ZOOM) {
    this.pointsList = pointsList;
    this.MAX_ITER = MAX_ITER;
    this.ZOOM = ZOOM;
  }
  public List<PointWithIteration> call() {
    return executeTask(pointsList);
  }

  private List<PointWithIteration> executeTask(List<Point> pointsList) {
    List<PointWithIteration> result = new LinkedList<PointWithIteration>();
    for(Point p : pointsList)
      result.add(new PointWithIteration(p, calculateIterationForPoint(p, MAX_ITER)));
    return result;
  }
  private int calculateIterationForPoint(Point p, int MAX_ITER) {
    zx = zy = 0;
    cX = (p.getX() - 400) / ZOOM;
    cY = (p.getY() - 300) / ZOOM;
    int iter = MAX_ITER;
    while (zx * zx + zy * zy < 4 && iter > 0) {
      tmp = zx * zx - zy * zy + cX;
      zy = 2.0 * zx * zy + cY;
      zx = tmp;
      iter--;
    }
    return iter;
  }
}

class Parser {
  private int threadsQuan;
  private int tasksQuan;

  Parser(String[] args) {
    try {
      threadsQuan = Integer.parseInt(args[0]);
      tasksQuan   = Integer.parseInt(args[1]);
    } catch(Exception e) {
      System.out.println("There is an error with parsing");
      System.out.println("args[0] = threads quantity");
      System.out.println("args[1] = tasks quantity");
      System.exit(-1);
    }
  }
  public int getThreadsQuantity() { return threadsQuan; }
  public int getTasksQuantity()    { return tasksQuan; }
}

public class Mandelbrot extends JFrame {
  private final int MAX_ITER = 800;
  private final double ZOOM = 200;
  private BufferedImage I;
  private double zx, zy, cX, cY, tmp;
  private ExecutorService pool = Executors.newFixedThreadPool(4);

  public Mandelbrot(Parser p) {
    /**
     * Prepare canvas
     */
    super("Mandelbrot Set");
    setBounds(100, 100, 600, 600);
    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

    /**
     * Prepare data set
     */
    /** Create matrix to store point */
    List<List<Point>> taskPoints = new LinkedList<List<Point>>();
    for(int i = 0; i < p.getTasksQuantity(); i++)
      taskPoints.add(new LinkedList<Point>());
    /** Fill each vector of the matrix with points */
    int currentThread;
    for (int y = 0; y < getHeight(); y++)
      for (int x = 0; x < getWidth(); x++) {
        currentThread = ( ( y * getWidth() ) + x ) % p.getTasksQuantity();
        taskPoints.get(currentThread).add(new Point(x, y));
      }

    /**
     * Create pools, threads and make calculations
     */
    ExecutorService pool = Executors.newFixedThreadPool(p.getThreadsQuantity());
    List<Future<List<PointWithIteration>>> resultPoints = new LinkedList<Future<List<PointWithIteration>>>();
    for(List<Point> listOfPoints : taskPoints) {
      Callable<List<PointWithIteration>> callable = new CalculationProvider(listOfPoints, MAX_ITER, ZOOM);
      Future<List<PointWithIteration>> future = pool.submit(callable);
      resultPoints.add(future);
    }

    /**
     * Update image
     */
    try {
      for(Future<List<PointWithIteration>> future : resultPoints) {
        for(PointWithIteration pwi : future.get())
        I.setRGB(pwi.getPoint().getX(), pwi.getPoint().getY(), pwi.getIter() | (pwi.getIter() << 8));
      }
    } catch (Exception e) { System.out.println("No _, no no no no no _, no"); }
  }

  @Override
  public void paint(Graphics g) {
    g.drawImage(I, 0, 0, this);
  }

  public static void main(String[] args) {
    Parser p = new Parser(args);
    long timeStart;
    long timeEnd;
    long timeDiff;
    timeStart = System.nanoTime();
    /**
     * To see or not to see
     */
     // new Mandelbrot(p).setVisible(true);
    new Mandelbrot(p).setVisible(false);

    timeEnd = System.nanoTime();
    timeDiff = timeEnd - timeStart;
    /**
     * Data specification:
     * [Threads number],[Tasks quantity],[Time in nanosec]
     */
    System.out.println(
      p.getThreadsQuantity() + "," +
      p.getTasksQuantity() + "," +
      timeDiff);
    /**
     * For calculation time speed
     */
    System.exit(0);
  }
}
