function printAsync(s, cb) {
   var delay = Math.floor((Math.random()*1000)+500);
   setTimeout(function() {
       console.log(s);
       if (cb) cb();
   }, delay);
}

function task1(cb, n) {
    printAsync("1", function() {
        task2(cb, n);
    });
}

function task2(cb, n) {
    printAsync("2", function() {
        task3(cb, n);
    });
}

function task3(cb, n) {
  if(n == 0)
    printAsync("3", cb);
  else
    printAsync("3", function() {
        task1(cb, n - 1);
    });
}

function loop(cb, n) {
  task1(cb, n)
}


loop(function() { console.log('done!'); }, 4);

/*
** Zadanie:
** Napisz funkcje loop(n), ktora powoduje wykonanie powyzszej
** sekwencji zadan n razy. Czyli: 1 2 3 1 2 3 1 2 3 ... done
**
*/

// loop(4);
