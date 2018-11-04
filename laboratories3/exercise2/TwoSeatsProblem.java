import java.util.concurrent.locks.*;
import java.util.LinkedList;

class Seat {
  public enum SeatStatus {
    FREE,
    OCCUPIED;
  };
  private static int counter = 0;
  private int seatId;
  private SeatStatus seatStatus;

  Seat() {
    seatId = counter++;
    seatStatus = SeatStatus.FREE;
  }
  public void setState(SeatStatus state) {
    seatStatus = state;
  }
  public SeatStatus getState() {
    return seatStatus;
  }
  public String toString() {
    return "[Seat " + seatId + "]";
  }
}

class Client implements Runnable {
  private static int counter = 0;
  private int clientId;
  private Waiter w;

  Client(Waiter w) {
    clientId = counter++;
    this.w = w;
  }
  public void run() {
    eatInRestaurant();
  }
  private void eatInRestaurant() {
    try {
      // COMING
      w.waitForOthers(this);
      // TAKING_SEAT
      Seat s = w.takeASeat(this);
      // EATING
      w.eatAtRestaurant(this);
      // LEAVING
      w.leaveTable(this, s);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  public String toString() {
    return "[Consumer " + clientId + "]";
  }
}

class Waiter {
  private enum ClientsState {
    COMING,
    TAKING_SEAT,
    EATING,
    LEAVING;
  }
  private ClientsState clientsState;
  private int clientsInRestaurant;
  private int clientsEating;

  final Lock lock = new ReentrantLock();
  final Condition notComing = lock.newCondition();
  final Condition notTakingSeat = lock.newCondition();
  final Condition notEating = lock.newCondition();
  final Condition notLeaving = lock.newCondition();

  private LinkedList<Seat> seats = new LinkedList<Seat>();
  public void addElement(Seat s) { seats.add(s); }

  Waiter() {
    clientsState = ClientsState.COMING;
    clientsInRestaurant = 0;
    clientsEating = 0;
  }

  public void waitForOthers(Client c) throws InterruptedException {
    lock.lock();
    try {
      while(clientsState != ClientsState.COMING) {
        System.out.println(c.toString() + " is waiting");
        notComing.await();
      }
      clientsInRestaurant++;
      notComing.signal(); // for queued clients
    } finally {
      if(clientsInRestaurant == seats.size()) {
        clientsState = ClientsState.TAKING_SEAT;
        notTakingSeat.signal();
      }
      lock.unlock();
    }
  }
  public Seat takeASeat(Client c) throws InterruptedException {
    lock.lock();
    try {
      while(clientsState != ClientsState.TAKING_SEAT)
        notTakingSeat.await();
      Seat s = firstFreeSeat();
      s.setState(Seat.SeatStatus.OCCUPIED);
      System.out.println(c.toString() + " took a seat " + s.toString());
      notTakingSeat.signal(); // for queued clients
      clientsEating++;
      return s;
    } finally {
      if(clientsEating == seats.size()) {
        clientsState = ClientsState.EATING;
        notEating.signal();
      }
      lock.unlock();
    }
  }
  public void eatAtRestaurant(Client c) throws InterruptedException {
    lock.lock();
    try {
      while(clientsState != ClientsState.EATING)
        notEating.await();
      System.out.println(c.toString() + " is eating now");
      clientsEating--;
      notEating.signal(); // for queued clients
    } finally {
      if(clientsEating == 0) {
        clientsState = ClientsState.LEAVING;
        notLeaving.signal();
      }
      lock.unlock();
    }
  }
  public void leaveTable(Client c, Seat s) throws InterruptedException {
    lock.lock();
    try {
      while(clientsState != ClientsState.LEAVING)
        notLeaving.await();
      System.out.println(c.toString() + " left restaurant");
      s.setState(Seat.SeatStatus.FREE);
      clientsInRestaurant--;
      notLeaving.signal(); // for queued clients
    } finally {
      if(clientsInRestaurant == 0) {
        System.out.println("Waiter cleaned table");
        clientsState = ClientsState.COMING;
        notComing.signal();
      }
      lock.unlock();
    }
  }
  private Seat firstFreeSeat() {
    for(Seat s : seats) {
      if(s.getState() == Seat.SeatStatus.FREE) {
        return s;
      }
    }
    // Not possible result
    // Maybe some exception would be better, but they don't pay me to do this
    return null;
  }
}

public class TwoSeatsProblem {
  public static void main(String[] args) {
    Waiter w = new Waiter();
    for(int i = 0; i < 2; i++)
      w.addElement(new Seat());

    for(int i = 0; i < 360; i++)
      new Thread(new Client(w)).start();
  }
}
