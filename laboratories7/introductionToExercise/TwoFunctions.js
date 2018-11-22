var x = 0;
setTimeout(function() { x = 1; }, 2);
setTimeout(function() { console.log(x); }, 0);
