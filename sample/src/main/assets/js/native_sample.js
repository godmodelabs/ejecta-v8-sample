// Get native module instance wrapping Androids TTS
var tts = require('tts');

var saySomething = function () {
    tts.speak("Something?", function (state) { console.log("Speech is", state); });
}

var sayMore = function () {
    tts.speak("Polly wants a cracker", function (state) { console.log("Speech is", state); });
}

var logSomething = function() {
    console.info("This has been logged from Javascript into Java");
}

// The idea here is to show how we can create cross-language exceptions
var crash = function() {
    console.log("We will now do something that will cause an Exception in JS")
    require('nonexistant-file.js');
}

// Let the console know that JS is alive
console.log('Hello world from JS!');

// Create CommonJS return object
module.exports = {
    "sayMore": sayMore,
    "saySomething": saySomething,
    "logSomething": logSomething,
    "crash": crash
};
