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
    final Condition notFreeProducer = lock.newCondition();
    final Condition notFreeConsumer = lock.newCondition();
    final Condition waitingProducer = lock.newCondition();
    final Condition waitingConsumer = lock.newCondition();
  }
  private LinkedList<Cell> cells = new LinkedList<Cell>();
  private FactoryMonitor fm;
  private int producersNeed;
  private int consumersNeed;
  private Boolean producerWaiting;
  private Boolean consumerWaiting;

  Factory() {
    fm = new FactoryMonitor();
    producerWaiting = false;
    consumerWaiting = false;
  }

  public void changeCellsState(int quan, Cell.CellState baseState, Cell.CellState nextState) throws InterruptedException {
    System.out.println("hi");
    switch(baseState) {
      // Producer's case
      case WITHOUT_PRODUCT :
        fm.lock.lock();
        producersNeed = quan;
        try {
          while(producerWaiting == true)
            fm.waitingProducer.await();
          while(quan > quanOfCellsWithSpecificState(baseState)) {
            producerWaiting = true;
            fm.notFreeProducer.await();
          }
          fm.waitingProducer.signal();
          producerWaiting = false;
          for(Cell c : cellsWithSpecificState(quan, baseState))
            c.setValue(nextState);
          if(consumersNeed < quanOfCellsWithSpecificState(new Consumer().getBaseState()))
            fm.notFreeConsumer.signal();
        } finally {
          fm.lock.unlock();
        }
        break;
      // Consumer's case
      case WITH_PRODUCT :
        fm.lock.lock();
        consumersNeed = quan;
        try {
          while(consumerWaiting == true)
            fm.waitingConsumer.await();
          while(quan > quanOfCellsWithSpecificState(baseState)) {
            consumerWaiting = true;
            fm.notFreeConsumer.await();
          }
          fm.waitingConsumer.signal();
          consumerWaiting = false;
          for(Cell c : cellsWithSpecificState(quan, baseState))
            c.setValue(nextState);
          if(consumersNeed < quanOfCellsWithSpecificState(new Producer().getBaseState()))
            fm.notFreeProducer.signal();
        } finally {
          fm.lock.unlock();
        }
        break;
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
  public Cell.CellState getBaseState() { return baseState; }
  private void work(Factory f) {
    try {
      f.changeCellsState(cellsQuantity, baseState, nextState);
    } catch (Exception e) { e.printStackTrace(); }
  }
}

class Producer extends AbstractProducer {
  Producer() {
    baseState = Cell.CellState.WITHOUT_PRODUCT;
    nextState = Cell.CellState.WITH_PRODUCT;
  }
  Producer(int cellsQuantity, Factory f) {
    this();
    this.cellsQuantity = cellsQuantity;
    this.f = f;
  }
}

class Consumer extends AbstractProducer {
  Consumer() {
    baseState = Cell.CellState.WITH_PRODUCT;
    nextState = Cell.CellState.WITHOUT_PRODUCT;
  }
  Consumer(int cellsQuantity, Factory f) {
    this();
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
