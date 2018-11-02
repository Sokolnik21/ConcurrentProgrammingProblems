import java.util.concurrent.locks.*;
import java.util.LinkedList;

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
  private int value = -1;

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
  public void describeCells() {
    String result = "";
    for(Cell c : cells) {
      result += c.getValue() + " ";
    }
    System.out.println(result);
  }
}

abstract class AbstractProducer implements Runnable {
  protected int baseInt;
  protected int nextInt;
  protected Factory f;

  public void run() { work(f); }
  private void work(Factory f) {
    int currentFactoryCell = 0;
    Cell c;
    while(true) {
      c = getNextCell(f, currentFactoryCell);
      changeCellValue(c);
      currentFactoryCell = (currentFactoryCell + 1) % f.maxCellsQuan();
    }
  }
  private Cell getNextCell(Factory f, int index) {
    return f.getCell(index);
  }
  private void changeCellValue(Cell c) {
    try {
      c.takeCell();
    } catch (InterruptedException e) { e.printStackTrace(); }
    if(c.getValue() == baseInt) {
      c.setValue(nextInt);
      // To delete: start
      f.describeCells();
      // To delete: end
    }
    c.freeCell();
  }
}

class Producer extends AbstractProducer {
  private static int counter = -1;
  public static int getCounter() { return counter; }

  Producer(Factory f) {
    baseInt = counter;
    nextInt = counter + 1;
    counter++;
    this.f = f;
  }
}

class Consumer extends AbstractProducer {
  Consumer(Factory f) {
    baseInt = Producer.getCounter();
    nextInt = -1;
    this.f = f;
  }
}

public class StreamProcessingWithBuffer {
  public static void main(String[] args) {
    Factory f = new Factory();
    for(int i = 0; i < 10; i++)
      f.addElement(new Cell());

    for(int i = 0; i < 10; i++)
      new Thread(new Producer(f)).start();

    new Thread(new Consumer(f)).start();
  }
}
