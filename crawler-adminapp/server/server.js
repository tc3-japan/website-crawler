const express = require('express');
const path = require('path');

app = express();

app.use(express.static(path.join(__dirname, '../dist/')))

app.get('*', function(req, res) {
    res.sendFile('index.html', { root: path.join(__dirname, '../dist/') });
});

var port = process.env.PORT || 8080;

app.listen(port, () => {
    console.log(`Server is listening on port ${port}`);
});