import java.util.concurrent.locks.*;
import java.util.LinkedList;

class Printer {
  public enum PrinterStatus {
    FREE,
    OCCUPIED;
  };
  private static int counter = 0;
  private int printerId;
  private PrinterStatus printerStatus;

  Printer() {
    printerId = counter++;
    printerStatus = PrinterStatus.FREE;
  }
  public void setState(PrinterStatus printerStatus) {
    this.printerStatus = printerStatus;
  }
  public PrinterStatus getState() {
    return printerStatus;
  }
  public void printText(Consumer c, String txt) {
    System.out.println(this.toString() + " printed for " + c.toString() + ": " + txt);
  }
  public void finishJob() {
    System.out.println(this.toString() + " finished job");
  }
  public String toString() { return "[Printer " + printerId + "]"; }
}

class Consumer implements Runnable {
  private static int counter = 0;
  private int consumerId;
  private PrintersMonitor pm;

  Consumer(PrintersMonitor pm) {
    consumerId = counter++;
    this.pm = pm;
  }
  public void run() {
    makeAnOrder();
  }
  public String toString() { return "[Consumer " + consumerId + "]"; }
  private void makeAnOrder() {
    String txt = createText();
    try {
      Printer p = pm.reserve();
      p.printText(this, txt);
      pm.release(p);
    }
    catch (Exception e) { }
  }
  private String createText() {
    return "SimpleText";
  }
}

class PrintersMonitor {
  final Lock lock = new ReentrantLock();
  final Condition notFreePrinter  = lock.newCondition();

  LinkedList<Printer> printers = new LinkedList<Printer>();
  public void addElement(Printer p) { printers.add(p); }

  public Printer reserve() throws InterruptedException {
    lock.lock();
    try {
      while(notExistFreePrinter())
        notFreePrinter.await();
      Printer p = firstFreePrinter();
      p.setState(Printer.PrinterStatus.OCCUPIED);
      return p;
    } finally {
      lock.unlock();
    }
  }
  public void release(Printer p) {
    lock.lock();
    try {
      p.finishJob();
      p.setState(Printer.PrinterStatus.FREE);
      notFreePrinter.signal();
    } finally {
      lock.unlock();
    }
  }

  private Boolean notExistFreePrinter() {
    for(Printer p : printers) {
      if(p.getState() == Printer.PrinterStatus.FREE)
        return false;
    }
    return true;
  }
  private Printer firstFreePrinter() {
    for(Printer p : printers) {
      if(p.getState() == Printer.PrinterStatus.FREE) {
        return p;
      }
    }
    // Not possible result
    // Maybe some exception would be better, but they don't pay me to do this
    return null;
  }
}

public class PrintersProblem {
  public static void main(String[] args) {
    PrintersMonitor pm = new PrintersMonitor();
    for(int i = 0; i < 5; i++)
      pm.addElement(new Printer());

    for(int i = 0; i < 900; i++)
      new Thread(new Consumer(pm)).start();
  }
}
