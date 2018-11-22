'use strict';

var async = require("async");

var Fork = function() {
    this.state = 0;
    return this;
}

// !!!: https://stackoverflow.com/questions/44603958/variable-change-by-inner-function
// !!!: https://stackoverflow.com/questions/6847697/how-to-return-value-from-an-asynchronous-callback-function
Fork.prototype.acquire = function(callback) {
    var self = this
    var binaryBackoff = function(self, time) {
        if(self.state == 1) {
            console.log(time)
            setTimeout(function() { return binaryBackoff(self, time * 2); }, time);
        } else {
            // console.log("inner Self: " + self.state)
            self.state = 1
            // console.log("inner Self: " + self.state)
            // return self.state
            return 1
        }
    }
    var tasks = []
    tasks.push(function(cb) {
      console.log("This: " + self.state)
      cb()
    })
    tasks.push(function(cb) {
      self.state = binaryBackoff(self, 1)
      cb()
    })
    tasks.push(function(cb) {
      console.log("This: " + self.state)
      cb()
    })
    tasks.push(function(cb) {
      callback()
      cb()
    })
    async.waterfall(tasks)
}

Fork.prototype.release = function() {
    this.state = 0;
}

var Philosopher = function(id, forks) {
    this.id = id;
    this.forks = forks;
    this.f1 = id % forks.length;
    this.f2 = (id + 1) % forks.length;
    return this;
}

Philosopher.prototype.startNaive = function(count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    /**
     * It is important to pass callback as an argument
     * due to waterfall method that I want to use
     */
    var eat = function(callback) {
      forks[f1].acquire(function() {
          console.log("Philosopher " + id + " took one fork")
          forks[f2].acquire(function() {
              console.log("Philosopher " + id + " satisfied");
              forks[f1].release();
              forks[f2].release();

              /** I hope that I don't need waterfall here */
              // var secondForkTasks = []
              // secondForkTasks.push(function(cb) {
              //   console.log("Philosopher " + id + " satisfied")
              //   cb()
              // })
              // secondForkTasks.push(function(cb) {
              //   forks[f1].release()
              //   cb()
              // })
              // secondForkTasks.push(function(cb) {
              //   forks[f2].release()
              //   cb()
              // })
              // async.waterfall(secondForkTasks)
          })
      })
      callback()
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
}

Philosopher.prototype.startAsym = function(count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    // zaimplementuj rozwiazanie asymetryczne
    // kazdy filozof powinien 'count' razy wykonywac cykl
    // podnoszenia widelcow -- jedzenia -- zwalniania widelcow
}

Philosopher.prototype.startConductor = function(count) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id;

    // zaimplementuj rozwiazanie z kelnerem
    // kazdy filozof powinien 'count' razy wykonywac cykl
    // podnoszenia widelcow -- jedzenia -- zwalniania widelcow
}


var N = 5;
var forks = [];
var philosophers = []
for (var i = 0; i < N; i++) {
    forks.push(new Fork());
}

for (var i = 0; i < N; i++) {
    philosophers.push(new Philosopher(i, forks));
}

for (var i = 0; i < N; i++) {
    philosophers[i].startNaive(10);
}
