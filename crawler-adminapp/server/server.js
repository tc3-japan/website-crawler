const express = require('express');
const path = require('path');

let config = null;
try {
    config = require('./config');
}
catch(e) {

}

app = express();

//  Sever VUE dist files
app.use(express.static(path.join(__dirname, '../dist/')))

// For all other requests serve the index file
app.get('*', function(req, res) {
    res.sendFile('index.html', { root: path.join(__dirname, '../dist/') });
});

var port = process.env.PORT || (config ? config.port : '');

if (port) {
    app.listen(port, () => {
        console.log(`Server is listening on port ${port}`);
    });
}
else {
    console.log('Cannot start server: No port value has been set');
}