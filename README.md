# About

This project is a sample app for the ejecta-v8 library (https://github.com/godmodelabs/ejecta-v8).

The library ports the excellent EjectaJS (https://github.com/phoboslab/Ejecta) to Android, replacing JavascriptCore with v8 in the process. Think of it as a Javascript engine with Canvas and AJAX but no DOM. If you want fast rendering of a cross-platform Canvas-based JS application this is for - if you have a web application or your JS code accesses the DOM in any way a WebView will be a better choice.

It renders this JS-based plasma rendering demo: https://godmodelabs.github.io/ejecta-v8-sample/demo.html in either ejecta-v8, a WebView or a system browser of your choice. It is _not_ meant to
disparage the browser rendering performance since a browser does many things that ejecta-v8 does not need to take care of. It is meant purely as a tool to show
if ejecta-v8 suits your needs.

Ejecta-v8-sample & ejecta-v8 are published under the MIT Open Source License (http://opensource.org/licenses/mit-license.php), as is Ejecta.

To compile the sample you need to set up the referenced ejecta-v8 submodule, and follow the steps in it's the README.md (https://github.com/godmodelabs/ejecta-v8)
