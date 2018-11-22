// import waterfall from 'async/waterfall';
var async = require("async");

var array = []
function fillArray(cb, n) {
  while (n > 0) {
    array.push(task1(cb, n));
    array.push(task2(cb, n));
    array.push(task3(cb, n));
    n--;
  }
}

function sayDone() { console.log('done') }

function fillArrayThenWaterfall() {
  fillArray(sayDone, 2);

  async.waterfall(array, function (err, result) {
      // result now equals 'done'
  });
}

fillArrayThenWaterfall();

function printAsync(s, cb) {
   var delay = Math.floor((Math.random()*1000)+500);
   setTimeout(function() {
       console.log(s);
       if (cb) cb();
   }, delay);
}

function task1(cb, n) {
  // console.log("1");
    printAsync("1", function() { });
}

function task2(cb, n) {
  // console.log("2");

    printAsync("2", function() { });
}

function task3(cb, n) {
  // console.log("3");

  if(n == 0)
    printAsync("3", function() { });
  else
    printAsync("3", function() {
        task1(cb, n - 1);
    });
}
