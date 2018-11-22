var async = require("async");

function printAsync(s, cb) {
   var delay = Math.floor((Math.random()*1000)+500);
   setTimeout(function() {
       console.log(s);
       if (cb) cb();
   }, delay);
}

function task1(cb) {
    printAsync("1", function() {
        task2(cb);
    });
}

function task2(cb) {
    printAsync("2", function() {
        task3(cb);
    });
}

function task3(cb) {
    printAsync("3", function() {
      console.log('done!');
    });
}


// wywolanie sekwencji zadan
// task1(function() {
//     console.log('done!');
// });

async.waterfall([
  // function(callback) {
  //   callback(null, function() {
  //     console.log('done!');
  //   });
  // },
  task1, task1, task1, task1], function (err, result) {
    // result now equals 'done'
});
