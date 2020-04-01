var http2 = require('http2');

var session = http2.connect("http://localhost:8081");

var log = console.log;

session.on('close', () => {
    console.log("Session closed!");
});

session.on('socketError', err => {
    console.log("SOCKETERROR", err);
});

session.on('error', err => {
    console.log("ERROR", err);
});

session.on('goaway', (errorCode, lastStreamID, opaqueData) => {
    console.log("GOAWAY", errorCode);
});

session.on('connect', () => {
    console.log("connected!");

    var path = '/tap/operation'
    var reqBody = '{"hello":"world"}'

    var actualMethod = 'POST';
    var req = session.request({
        ':path': path,
        ':method': actualMethod,
        'content-type': 'application/json',
        'content-length': Buffer.byteLength(reqBody, "UTF-8")
    });

    req.on('error', e => console.log('ERROR', e));

    req.on('response', (respHeaders, flags) => {
        console.log('RESPONSE', respHeaders);

        var data = '';
        req.on('data', chunk => { data += chunk; }).on('end', () => {
            console.log('DATA', data);
        });
    });

    req.end(reqBody);
});