## laboratories3
- Printers
  - multiple printers, multiple consumers
  - how does it work?
    1. consumer enters factory (in this example: PrintersMonitor)
    2. he checks whether there is free printer
      - if there is, then he reserves it and prints his text
      - if not, then he waits until any printer becomes free
    3. at the end consumer releases printer
- Two seat table
  - one table with two seats, multiple clients
  - how does it work?
    1. client enters restaurant
    2. he checks whether the table is free (no one occupies it)
      - if it is, then he goes to point 3.
      - if not, he waits
    3. he checks whether there is enough clients to make reservation (and take a seat at that table)
      - if there is, then he takes a seat and goes to point 4.
      - if not, he waits
    4. he checks whether everyone made reservations (and took a seat)
      - if everyone did it, then he goes to point 5.
      - if not, he waits
    5. he eats
    6. he waits for everyone to finish eating
      - if everyone finished, then he leaves restaurant
      - if not, he waits
