## laboratories4
- Stream processing with buffer
    - One buffer (size doesn't matter) with cells, one consumer (possible variant: multiple consumers), multiple producers
    - Each cell starts with base state
        - To simplify:
            - base cell value: -1
    - Each producer can change the value of a cell only if current state of the cell matches the requirements of the producer
        - To simplify:
            - Cell's values: [-1..(N - 1)] where N = number of producers
            - Each producer is unique and has id in [0..(N - 1)]
            - Each producer changes value from (x - 1) to x where x equals id
    - Consumer is an another "producer"
        - To simplify:
            - Consumer changes value from (N - 1) (N = the number of producers) to base cell's state (in this example: -1)
    - Producers and consumers with random quantity of cells that will modify
        - One buffer (with 2M size) with cells, multiple consumers, multiple producers
        - Each cell starts with base state
        - Each producer changes (at once!) random quantity of cells from [base state] to [modified state]
        - Each producer changes (also at once) random quantity of cells from [modified state] to [base state]
