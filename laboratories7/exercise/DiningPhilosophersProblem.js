'use strict';
var async = require("async");

var Fork = function() {
    this.state = 0;
    return this;
}

Fork.prototype.acquire = function(cb) {
    var binaryBackoff = function(cb, fork, time) {
        if(fork.state == 1) {
            console.log(time)
            setTimeout(function() { return binaryBackoff(cb, fork, time * 2); }, time);
        } else {
            fork.state = 1;
            cb();
        }
    }
    binaryBackoff(cb, this, 1);
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
            forks[f2].acquire(function() {
                console.log("Philosopher " + id + " ready to eat");
                setTimeout(function () {
                    console.log("Philosopher " + id + " satisfied");
                    forks[f1].release();
                    forks[f2].release();

                    callback();
                }, 100 * Math.random())
            })
        })
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
        id = this.id,
        firstFork,
        secondFork;
    if (f1 % 2 == 0) {
        firstFork = f1;
        secondFork = f2;
    } else {
        firstFork = f2;
        secondFork = f1;
    }

    /**
     * It is important to pass callback as an argument
     * due to waterfall method that I want to use
     */
    var eat = function(callback) {
        forks[firstFork].acquire(function() {
            forks[secondFork].acquire(function() {
                console.log("Philosopher " + id + " ready to eat");
                setTimeout(function () {
                    console.log("Philosopher " + id + " satisfied");
                    forks[firstFork].release();
                    forks[secondFork].release();

                    callback();
                }, 100 * Math.random())
            })
        })
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
}

var Conductor = function(forks) {
    this.busy = false;
    this.forks = forks;
    return this;
}

Conductor.prototype.acquire = function(cb) {
    var binaryBackoff = function(cb, conductor, time) {
        if(conductor.busy) {
            console.log(time)
            setTimeout(function() { return binaryBackoff(cb, conductor, time * 2); }, time);
        } else {
            conductor.busy = true;
            cb();
        }
    }
    binaryBackoff(cb, this, 1);
}

Conductor.prototype.release = function() {
  this.busy = false;
}

Philosopher.prototype.startConductor = function(count, conductor) {
    var forks = this.forks,
        f1 = this.f1,
        f2 = this.f2,
        id = this.id,
        conductor = conductor;

    /**
     * It is important to pass callback as an argument
     * due to waterfall method that I want to use
     */
    var eat = function(callback) {
        conductor.acquire(function() {
            forks[f1].acquire(function() {
                forks[f2].acquire(function() {
                    console.log("Philosopher " + id + " ready to eat");
                    setTimeout(function () {
                        console.log("Philosopher " + id + " satisfied");
                        conductor.release();
                        forks[f1].release();
                        forks[f2].release();

                        callback();
                    }, 100 * Math.random())
                })
            })
        })
    }

    var tasks = []
    for(var i = 0; i < count; i++)
        tasks.push(eat)

    async.waterfall(tasks);
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

var conductor = new Conductor();

for (var i = 0; i < N; i++) {
    // philosophers[i].startNaive(10);
    // philosophers[i].startAsym(10);
    philosophers[i].startConductor(10, conductor);
}
