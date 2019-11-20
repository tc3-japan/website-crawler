const express = require('express');
const path = require('path');
const config = require('../config');

app = express();

app.use(express.static(path.join(__dirname, '../dist/')))

app.get('*', function(req, res) {
    res.sendFile('index.html', { root: path.join(__dirname, '../dist/') });
});

console.log('p',config.client.port);
var port = process.env.PORT || config.client.port;

app.listen(port, () => {
    console.log(`Server is listening on port ${port}`);
});