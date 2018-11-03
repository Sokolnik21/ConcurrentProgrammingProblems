import java.util.concurrent.locks.*;
import java.util.LinkedList;
import java.util.Random;

class Cell {
  public enum CellState {
    WITH_PRODUCT,
    WITHOUT_PRODUCT;
  }
  private CellState value = CellState.WITHOUT_PRODUCT;

  Cell() { }
  Cell(CellState value) {
    this();
    this.value = value;
  }
  public void       setValue(CellState value) { this.value = value; }
  public CellState  getValue() { return value; }
}

class Factory {
  private class FactoryMonitor {
    final Lock lock = new ReentrantLock();
    final Condition notFree = lock.newCondition();
  }
  private LinkedList<Cell> cells = new LinkedList<Cell>();
  private FactoryMonitor fm;
  private int occupiedCells;

  Factory() {
    fm = new FactoryMonitor();
    occupiedCells = 0;
  }

  public void changeCellsState(int quan, Cell.CellState baseState, Cell.CellState nextState) throws InterruptedException {
    fm.lock.lock();
    try {
      while(quan > quanOfCellsWithSpecificState(baseState))
        fm.notFree.await();
      for(Cell c : cellsWithSpecificState(quan, baseState))
        c.setValue(nextState);
      fm.notFree.signal();
    } finally {
      fm.lock.unlock();
    }
  }
  public void addElement(Cell c) { cells.add(c); }

  private int quanOfCellsWithSpecificState(Cell.CellState state) {
    int quan = 0;
    for(Cell c : cells)
      if(c.getValue() == state)
        quan++;
    return quan;
  }
  private LinkedList<Cell> cellsWithSpecificState(int quan, Cell.CellState state) {
    LinkedList<Cell> result = new LinkedList<Cell>();
    for(int i = 0; i < quan;)
      for(Cell c : cells)
        if(c.getValue() == state) {
          i++;
          result.add(c);
        }
    return result;
  }
}

abstract class AbstractProducer implements Runnable {
  protected Cell.CellState baseState;
  protected Cell.CellState nextState;
  protected int cellsQuantity;
  protected Factory f;

  public void run() { work(f); }
  private void work(Factory f) {
    try {
      f.changeCellsState(cellsQuantity, baseState, nextState);
    } catch (Exception e) { e.printStackTrace(); }
  }
}

class Producer extends AbstractProducer {
  Producer(int cellsQuantity, Factory f) {
    baseState = Cell.CellState.WITHOUT_PRODUCT;
    nextState = Cell.CellState.WITH_PRODUCT;
    this.cellsQuantity = cellsQuantity;
    this.f = f;
  }
}

class Consumer extends AbstractProducer {
  Consumer(int cellsQuantity, Factory f) {
    baseState = Cell.CellState.WITH_PRODUCT;
    nextState = Cell.CellState.WITHOUT_PRODUCT;
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
