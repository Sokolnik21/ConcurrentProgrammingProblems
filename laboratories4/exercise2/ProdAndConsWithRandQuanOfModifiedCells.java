import java.util.concurrent.locks.*;
import java.util.LinkedList;
import java.util.Random;

class Cell {
  private int value = 0;

  Cell() { }
  Cell(int value) {
    this();
    this.value = value;
  }
  public void setValue(int value) { this.value = value; }
  public int  getValue() { return value; }
}

class Factory {
  // don't need this state
  private enum FactoryState {
    FREE,
    OCCUPIED;
  };
  private class FactoryMonitor {
    final Lock lock = new ReentrantLock();
    final Condition notFree = lock.newCondition();
  }
  private LinkedList<Cell> cells = new LinkedList<Cell>();
  private FactoryState state;
  private FactoryMonitor fm;
  private int occupiedCells;

  Factory() {
    fm = new FactoryMonitor();
    state = FactoryState.FREE;
    occupiedCells = 0;
  }

  private int quanCellsThatAreLeft(int indicator) {
    int quan = 0;
    for(Cell c : cells)
      if(c.getValue() == indicator)
        quan++;
    return quan;
    // cells.stream().filter(c -> c.getValue() == 0).collect(Collectors.toList());
  }

  public LinkedList<Cell> takeCells(int quan, int indicator) throws InterruptedException {
    fm.lock.lock();
    try {
      while(state != FactoryState.FREE && quan >= quanCellsThatAreLeft(indicator))
        fm.notFree.await();
      state = FactoryState.OCCUPIED;
    } finally {
      fm.lock.unlock();
    }
  }
  public void freeCells(LinkedList<Cell> cells) {
    fm.lock.lock();
    try {
      state = FactoryState.FREE;
      fm.notFree.signal();
    } finally {
      fm.lock.unlock();
    }
  }

  public void addElement(Cell c) { cells.add(c); }
  public int  maxCellsQuan() { return cells.size(); }
  public Cell getCell(int index) { return cells.get(index); }
}

abstract class AbstractProducer implements Runnable {
  protected int baseInt;
  protected int nextInt;
  protected int chunk;
  protected Factory f;

  public void run() { work(f); }
  private void work(Factory f) {
    LinkedList<Cell> cells = new LinkedList<Cell>();
    int currentFactoryCell = 0;
    Cell c;
    while(chunk > 0) {
      cells = f.getChunkOfCells();
      c = getNextCell(f, currentFactoryCell);
      if(changeCellValue(c))
        chunk--;
      currentFactoryCell = (currentFactoryCell + 1) % f.maxCellsQuan();
    }
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
  Producer(int chunk, Factory f) {
    baseInt = 0;
    nextInt = 1;
    this.chunk = chunk;
    this.f = f;
  }
}

class Consumer extends AbstractProducer {
  Consumer(int chunk, Factory f) {
    baseInt = 1;
    nextInt = 0;
    this.chunk = chunk;
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
      int quan = r.nextInt() % p.getM();
      new Thread(new Producer(quan, f)).start();
      new Thread(new Consumer(quan, f)).start();
    }
  }
}
