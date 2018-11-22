#!/bin/bash
> Results.dat
for threads in {1..10}
do
  for tasks in {1..100..10}
  do
    # 10 iterations to get different results
    for i in {1..10}
    do
      echo "done Threads: $threads Tasks: $tasks"
      java Mandelbrot $threads $tasks >> Results.dat
    done
  done
done
