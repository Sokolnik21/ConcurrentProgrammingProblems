## laboratories7
- Introduction to exercise
    - Depending on non-deterministic circumstances, the output can be either 1 or 0;
- Dining philosophers problem
    - Version 1 [Naive]
        1. Philosophers takes fork on their left.
        2. Philosophers takes fork on their right.
    - Version 2 [Asynchronous]
        - Philosophers that seats on odd seats
            1. Takes fork on their left.
            2. Takes fork on their right.
        - Philosophers that seats on even seats
            1. Takes fork on their right.
            2. Takes fork on their left.
    - Version 3 [With conductor]
        - There is a conductor that provides forks for philosophers.
        - Conductor can serve only one philosopher at once.
        - Each philosopher must wait for conductor to start to eat.
