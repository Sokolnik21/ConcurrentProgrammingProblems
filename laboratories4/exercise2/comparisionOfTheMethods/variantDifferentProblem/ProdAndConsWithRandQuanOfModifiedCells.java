import java.util.concurrent.locks.*;
import java.util.LinkedList;
import java.util.Random;

class Cell {
  private enum CellState {
    FREE,
    OCCUPIED;
  };
  private class CellMonitor {
    final Lock lock = new ReentrantLock();
    final Condition notFree = lock.newCondition();
  }
  private CellMonitor cm;
  private CellState state;
  private int value = 0;

  Cell() {
    cm = new CellMonitor();
    state = CellState.FREE;
  }
  Cell(int value) {
    this();
    this.value = value;
  }
  public void takeCell() throws InterruptedException {
    cm.lock.lock();
    try {
      while(state != CellState.FREE)
        cm.notFree.await();
      state = CellState.OCCUPIED;
    } finally {
      cm.lock.unlock();
    }
  }
  public void freeCell() {
    cm.lock.lock();
    try {
      state = CellState.FREE;
      cm.notFree.signal();
    } finally {
      cm.lock.unlock();
    }
  }
  public void setValue(int value) { this.value = value; }
  public int  getValue() { return value; }
}

class Factory {
  private LinkedList<Cell> cells = new LinkedList<Cell>();
  public void addElement(Cell c) { cells.add(c); }
  public int  maxCellsQuan() { return cells.size(); }
  public Cell getCell(int index) { return cells.get(index); }
}

abstract class AbstractProducer implements Runnable {
  protected int baseInt;
  protected int nextInt;
  protected int cellsQuantity;
  protected Factory f;

  public void run() { work(f); }
  private void work(Factory f) {
    long timeStart;
    long timeEnd;
    long timeDiff;
    int baseCellsQuantity = cellsQuantity;

    int currentFactoryCell = 0;
    Cell c;
    timeStart = System.nanoTime();
    while(cellsQuantity > 0) {
      c = getNextCell(f, currentFactoryCell);
      if(changeCellValue(c))
        cellsQuantity--;
      currentFactoryCell = (currentFactoryCell + 1) % f.maxCellsQuan();
    }
    timeEnd = System.nanoTime();
    timeDiff = timeEnd - timeStart;
    System.out.println(baseCellsQuantity + " " + timeDiff);
  }
  private Cell getNextCell(Factory f, int index) {
    return f.getCell(index);
  }
  private Boolean changeCellValue(Cell c) {
    Boolean result = false;
    try {
      c.takeCell();
      if(c.getValue() == baseInt) {
        c.setValue(nextInt);
        result = true;
      } else {
        result = false;
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      c.freeCell();
      return result;
    }
  }
}

class Producer extends AbstractProducer {
  Producer(int cellsQuantity, Factory f) {
    baseInt = 0;
    nextInt = 1;
    this.cellsQuantity = cellsQuantity;
    this.f = f;
  }
}

class Consumer extends AbstractProducer {
  Consumer(int cellsQuantity, Factory f) {
    baseInt = 1;
    nextInt = 0;
    this.cellsQuantity = cellsQuantity;
    this.f = f;
  }
}

class Parser {
  private int M;
  private int K;

  Parser(String[] args) {
    try {
      M = Integer.parseInt(args[0]);
      K = Integer.parseInt(args[1]);
    } catch(Exception e) {
      System.out.println("There is an error with parsing");
      System.out.println("args[0] = M (half of the buffor size)");
      System.out.println("args[1] = K (quantity of producers and consumers)");
      System.exit(-1);
    }
  }
  public int getM() { return M; }
  public int getK() { return K; }
}

public class ProdAndConsWithRandQuanOfModifiedCells {
  public static void main(String[] args) {
    Parser p = new Parser(args);
    Random r = new Random();

    Factory f = new Factory();
    for(int i = 0; i < (2 * p.getM()); i++)
      f.addElement(new Cell());

    for(int i = 0; i < p.getK(); i++) {
      int quan = r.nextInt(p.getM()) + 1;
      new Thread(new Producer(quan, f)).start();
      new Thread(new Consumer(quan, f)).start();
    }
  }
}
