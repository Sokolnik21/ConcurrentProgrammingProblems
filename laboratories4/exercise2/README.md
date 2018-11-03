## Variants
- different problem
  - the locks are connected with cells
  - it works, but:
    - it's not a solution for this problem (it is not provided that every update operation is atomic)
    - it's also not optimal (there is A LOT of excessive operations connected with locking and unlocking locks)
  - You can compare this solution with others
- non fair
  - there are one lock connected with cells' aggregate (in this example: Factory) with two conditions
    - one connected with producers
    - and second one with consumers
  - why it's non fair variant?
    - whenever producer or consumer updates Factory, others (Prod and Cons) checks whether their condition is fulfilled or not - and that's the problem
      - big Persons (Producers and Consumers) may starve, because it's harder for them to fulfill their requirements
- fair
  - works similar to non fair solution, however, it's more fair for big Persons
  - how does it work?
    - let's focus on producers
      1. producer enters factory
      2. he checks whether there is enough resources to work with
        - if there is, then he does normal work
        - if not, he changes factory's variable producersNeed and waits
      3. at the end factory checks whether there is enough resources to wake up consumer (based on consumersNeed)
    - consumers are similar to producers
